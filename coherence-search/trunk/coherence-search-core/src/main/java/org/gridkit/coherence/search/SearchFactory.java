package org.gridkit.coherence.search;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import org.gridkit.coherence.search.IndexUpdateEvent.Type;

import com.tangosol.net.NamedCache;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.Filter;
import com.tangosol.util.MapIndex;
import com.tangosol.util.SimpleMapIndex;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.IndexAwareExtractor;
import com.tangosol.util.filter.IndexAwareFilter;

public class SearchFactory<I, IC, Q> {

	private PlugableSearchIndex<I, IC, Q> indexPlugin;
	private IC indexConfig;
	private ValueExtractor extractor;
	private IndexEngineConfig engineConfig = new DefaultIndexEngineConfig();
	
	private Object token;
	
	public SearchFactory(PlugableSearchIndex<I, IC, Q> plugin, IC config, ValueExtractor extractor) {
		this.indexPlugin = plugin;
		this.indexConfig = config;
		this.extractor = extractor;
		this.token = plugin.createIndexCompatibilityToken(indexConfig); 
	}
	
	public IndexEngineConfig getEngineConfig() {
		return engineConfig;
	}
	
	public void createIndex(NamedCache cache) {
		SearchIndexExtractor<I, IC, Q> extractor = createConfiguredExtractor();
		cache.addIndex(extractor, false, null);
	}
	
	private SearchIndexExtractor<I, IC, Q> createConfiguredExtractor() {
		return new SearchIndexExtractor<I, IC, Q>(indexPlugin, token, indexConfig, engineConfig, extractor);
	}

	private SearchIndexExtractor<I, IC, Q> createFilterExtractor() {
		return new SearchIndexExtractor<I, IC, Q>(indexPlugin, token, extractor);
	}

	public Filter createFilter(Q query) {
		return new QueryFilter<I, Q>(createFilterExtractor(), query);
	}
	
	static class SearchIndexEngine<I,Q> implements MapIndex, IndexInvocationContext {

		private static Timer INDEX_TIMER = new Timer("IndexFlushTimer", true);
		
		private PlugableSearchIndex<I, ?, Q> psi;
		
		private ValueExtractor attributeExtrator;
		private I coreIndex;
		private MapIndex attributeIndex;
		private TimerTask flushTask;
		
		private Map<Object, IndexUpdateEvent> pendingUpdates;
		
		private int queueSizeLimit = 0;
		private int indexingDelay = 0;
		private boolean originalValueForUpdates = true;
		
		public SearchIndexEngine(I index, ValueExtractor extractor, PlugableSearchIndex<I, ?, Q> psi) {
			this.coreIndex = index;
			this.attributeExtrator = extractor;
			this.psi = psi;
		}

		public void init(IndexEngineConfig config, Map<Object, Object> indexMap) {
			configure(config);
			if (attributeIndex != null) {
				if (indexMap.get(attributeExtrator) == null) {
					indexMap.put(attributeExtrator, indexMap);
				}
			}
		}
		
		private void configure(IndexEngineConfig config) {

			psi.configure(config);			
			if (config.isAttributeIndexEnabled()) {
				attributeIndex = new SimpleMapIndex(attributeExtrator, false, null);
			}
		
			queueSizeLimit = config.getIndexUpdateQueueSizeLimit();
			indexingDelay = config.getIndexUpdateDelay();
			originalValueForUpdates = config.isOldValueOnUpdateEnabled();
			
			if (queueSizeLimit > 0) {
				pendingUpdates = new HashMap<Object, IndexUpdateEvent>();
				if (indexingDelay > 0) {
					scheduleFlasher(indexingDelay);
				}
			}
		}
		
		private void scheduleFlasher(int indexingDelay) {
			flushTask = new TimerTask() {
				@Override
				public void run() {
					flush();
				}
			};
			INDEX_TIMER.schedule(flushTask , indexingDelay, indexingDelay);
		}

		public void tearDown(Map<?, ?> indexMap) {
			if (attributeIndex == indexMap.get(attributeExtrator)) {
				indexMap.remove(attributeExtrator);
			}
			if (flushTask != null) {
				flushTask.cancel();
			}
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void insert(Entry entry) {
			Object key = getKeyFromEntry(entry);
			Object value = getValuefromEntry(entry);
			IndexUpdateEvent event = new IndexUpdateEvent(key, value, null, Type.INSERT);
			if (attributeIndex != null) {
				attributeIndex.delete(entry);
			}
			enqueue(event);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void update(Entry entry) {
			Object key = getKeyFromEntry(entry);
			Object value = getValuefromEntry(entry);
			Object oldValue = getOriginalValueFromEntry(entry);
			IndexUpdateEvent event = new IndexUpdateEvent(key, value, oldValue, Type.UPDATE);
			if (attributeIndex != null) {
				attributeIndex.delete(entry);
			}
			enqueue(event);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void delete(Entry entry) {
			Object key = getKeyFromEntry(entry);
			Object oldValue = getOriginalValueFromEntry(entry);
			IndexUpdateEvent event = new IndexUpdateEvent(key, null, oldValue, Type.DELETE);
			if (attributeIndex != null) {
				attributeIndex.delete(entry);
			}
			enqueue(event);
		}

		@SuppressWarnings("unchecked")
		private Object getKeyFromEntry(Entry entry) {
			if (entry instanceof BinaryEntry) {
				return ((BinaryEntry)entry).getBinaryKey();
			}
			else {
				return entry.getKey(); 
			}
		}

		@SuppressWarnings("unchecked")
		private Object getValuefromEntry(Entry entry) {
			// TODO extractor helper
			return attributeExtrator.extract(entry.getValue());
		}

		@SuppressWarnings("unchecked")
		private Object getOriginalValueFromEntry(Entry entry) {
			if (!originalValueForUpdates) {
				return null;
			}
			else {
				if (entry instanceof BinaryEntry) {
					return attributeExtrator.extract(((BinaryEntry)entry).getOriginalValue());
				}
				else {
					if (attributeIndex != null) {
						return attributeIndex.get(getKeyFromEntry(entry));
					}
					else {
						throw new RuntimeException("Forward attribute index required");
					}
				}
			}
		}

		private void enqueue(IndexUpdateEvent event) {
			if (queueSizeLimit == 0) {
				psi.updateIndexEntries(coreIndex, Collections.singletonMap(event.getKey(), event), this);
			}
			else {
				synchronized (this) {
					IndexUpdateEvent old = pendingUpdates.get(event.getKey());
					if (old != null) {
						old.merge(event);
					}
					else {
						pendingUpdates.put(event.getKey(), event);
					}
				}
				if (pendingUpdates.size() >= queueSizeLimit) {
					flush();
				}
			}
		}
		
		private synchronized void flush() {
			if (pendingUpdates.size() > 0) {
				psi.updateIndexEntries(coreIndex, pendingUpdates, this);
				pendingUpdates.clear();
			}
		}

		@Override
		public Object get(Object key) {
			return NO_VALUE;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Comparator getComparator() {
			return null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Map getIndexContents() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ValueExtractor getValueExtractor() {
			return attributeExtrator;
		}

		@Override
		public boolean isOrdered() {
			return false;
		}

		@Override
		public boolean isPartial() {
			return false;
		}

		@Override
		public Binary ensureBinaryKey(Object key) {
			if (key instanceof Binary) {
				return (Binary) key;
			}
			else {
				// TODO proper serializer support
				return ExternalizableHelper.toBinary(key);
			}
		}

		@Override
		public Object ensureObjectKey(Object key) {
			if (key instanceof Binary) {
				return ExternalizableHelper.fromBinary((Binary) key);
			}
			else {
				// TODO proper serializer support
				return key;
			}
		}

		@Override
		public Object getRawAttribute(Object key) {
			if (attributeIndex != null) {
				// TODO ensure compatible key format
				return attributeIndex.get(key);
			}
			throw new UnsupportedOperationException("Raw attribute index is not configured");
		}
		
		public Filter applyIndex(QueryFilter<I, Q> filter, Set<Object> keys) {
			flush();
			boolean dirty = psi.applyIndex(coreIndex, filter.getQuery(), keys, this);
			if (dirty) {
				return filter;
			}
			else {
				return null;
			}
		}

		public int calculateEffectiveness(QueryFilter<I, Q> filter, Set<Object> keys) {
			flush();
			return psi.calculateEffectiveness(coreIndex, filter.getQuery(), keys, this);
		}
	}
	
	public static class SearchIndexExtractor<I, IC, Q> implements IndexAwareExtractor {

		private PlugableSearchIndex<I, IC, Q> psi;
		private Object token;
		private IC indexConfiguration;
		private IndexEngineConfig engineConfig;
		private ValueExtractor extractor;
		
		public SearchIndexExtractor() {
			// for remoting
		}
		
		public SearchIndexExtractor(PlugableSearchIndex<I, IC, Q> psi, Object token, IC indexConfiguration,	IndexEngineConfig engineConfig, ValueExtractor extractor) {
			this.psi = psi;
			this.token = token;
			this.indexConfiguration = indexConfiguration;
			this.engineConfig = engineConfig;
			this.extractor = extractor;
		}

		public SearchIndexExtractor(PlugableSearchIndex<I, IC, Q> psi, Object token, ValueExtractor extractor) {
			this.psi = psi;
			this.token = token;
			this.extractor = extractor;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((extractor == null) ? 0 : extractor.hashCode());
			result = prime * result + ((psi == null) ? 0 : psi.hashCode());
			result = prime * result + ((token == null) ? 0 : token.hashCode());
			return result;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SearchIndexExtractor other = (SearchIndexExtractor) obj;
			if (extractor == null) {
				if (other.extractor != null)
					return false;
			} else if (!extractor.equals(other.extractor))
				return false;
			if (psi == null) {
				if (other.psi != null)
					return false;
			} else if (!psi.equals(other.psi))
				return false;
			if (token == null) {
				if (other.token != null)
					return false;
			} else if (!token.equals(other.token))
				return false;
			return true;
		}

		@Override
		@SuppressWarnings("unchecked")
		public MapIndex createIndex(boolean sorted, Comparator comparator, Map indexMap) {
			SearchIndexEngine<I, Q> engine = new SearchIndexEngine<I, Q>(psi.createIndexInstance(indexConfiguration), extractor, psi);
			engine.init(engineConfig, indexMap);
			indexMap.put(this, engine);
			return engine;
		}

		@Override
		@SuppressWarnings("unchecked")
		public MapIndex destroyIndex(Map indexMap) {
			SearchIndexEngine<I, Q> engine = (SearchIndexEngine<I, Q>) indexMap.get(this);
			if (engine != null) {
				indexMap.remove(this);
				engine.tearDown(indexMap);
			}
			return engine;
		}

		@Override
		public Object extract(Object object) {
			return extractor.extract(object);
		}

		public PlugableSearchIndex<I, IC, Q> getPSI() {
			return psi;
		}
		
	}
	
	public static class QueryFilter<I, Q> implements IndexAwareFilter {

		private SearchIndexExtractor<I, ?, Q> extractor;
		private Q query;
		
		public QueryFilter() {
			// for remoting
		}
		
		public QueryFilter(SearchIndexExtractor<I, ?, Q> extractor, Q query) {
			this.extractor = extractor;
			this.query = query;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((extractor == null) ? 0 : extractor.hashCode());
			result = prime * result + ((query == null) ? 0 : query.hashCode());
			return result;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			QueryFilter other = (QueryFilter) obj;
			if (extractor == null) {
				if (other.extractor != null)
					return false;
			} else if (!extractor.equals(other.extractor))
				return false;
			if (query == null) {
				if (other.query != null)
					return false;
			} else if (!query.equals(other.query))
				return false;
			return true;
		}

		public Q getQuery() {
			return query;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public Filter applyIndex(Map indexes, Set keys) {
			MapIndex index = (MapIndex) indexes.get(extractor);
			if (index != null) {
				SearchIndexEngine engine = (SearchIndexEngine) index;
				return engine.applyIndex(this, keys);
			}
			else {
				return this;
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public int calculateEffectiveness(Map indexes, Set keys) {
			MapIndex index = (MapIndex) indexes.get(extractor);
			if (index != null) {
				SearchIndexEngine engine = (SearchIndexEngine) index;
				return engine.calculateEffectiveness(this, keys);
			}
			else {
				// 100x just for no reason
				return 100 * keys.size();
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean evaluateEntry(Entry entry) {
			return evaluate(entry.getValue());
		}

		@Override
		public boolean evaluate(Object object) {
			return extractor.getPSI().evaluate(query, object);
		}
	}
}
