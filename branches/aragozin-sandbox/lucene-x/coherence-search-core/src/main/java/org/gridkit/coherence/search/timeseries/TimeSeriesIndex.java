package org.gridkit.coherence.search.timeseries;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.gridkit.coherence.search.CohIndexHelper;

import com.tangosol.net.BackingMapContext;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.MapIndex;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.ValueExtractor;

public class TimeSeriesIndex implements MapIndex {

	private ConcurrentMap<Object, NavigableMap<Object, Object>> index = new ConcurrentHashMap<Object, NavigableMap<Object,Object>>();
	
	private ValueExtractor seriesKeyExtractor;
	private ValueExtractor timestampExtractor;
	private Comparator<?>  timestampComparator;
	
	@SuppressWarnings("unused")
	private BackingMapContext bmc;
	
	public TimeSeriesIndex(TimeSeriesExtractor timeSeriesExtractor, BackingMapContext bmc) {
		seriesKeyExtractor = timeSeriesExtractor.getSeriesKeyExtractor();
		timestampExtractor = timeSeriesExtractor.getTimestampExtractor();
		timestampComparator = timeSeriesExtractor.getTimestampComparator();
		
		this.bmc = bmc; 
	}

	@Override
	public ValueExtractor getValueExtractor() {
		throw new UnsupportedOperationException();
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
	@SuppressWarnings("rawtypes")
	public Map getIndexContents() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(Object key) {
		throw new UnsupportedOperationException();
	}
	
	public Set<Object> getSeriesKeys() {
		return index.keySet();
	}
	
	public NavigableMap<Object, Object> getSeries(Object sKey) {
		NavigableMap<Object, Object> line = getLine(sKey);
		if (line != null) {
			synchronized (line) {
				return new TreeMap<Object, Object>(line);
			}
		}
		else {
			return null;
		}
	}

	public Object get(Object sKey, Object ts) {
		SortedMap<Object, Object> line = getLine(sKey);
		if (line != null) {
			synchronized(line) {
				return line.get(ts);
			}
		}
		else {
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map.Entry<Object, Object> ceilingEntry(Object sKey, Object ts) {
		NavigableMap<Object, Object> line = getLine(sKey);
		if (line != null) {
			synchronized(line) {
				if (ts == null) {
					Map.Entry<Object, Object> e = line.lastEntry();
					return e == null ? null : new SimpleEntry(e);
				}
				else {
					Map.Entry<Object, Object> e = line.ceilingEntry(ts);
					return e == null ? null : new SimpleEntry(e);
				}
			}
		}
		else {
			return null;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map.Entry<Object, Object> floorEntry(Object sKey, Object ts) {
		NavigableMap<Object, Object> line = getLine(sKey);
		if (line != null) {
			synchronized(line) {
				if (ts == null) {
					Map.Entry<Object, Object> e = line.firstEntry(); 
					return e == null ? null : new SimpleEntry(e);
				}
				else {
					Map.Entry<Object, Object> e = line.floorEntry(ts); 
					return e == null ? null : new SimpleEntry(e);
				}
			}
		}
		else {
			return null;
		}
	}
		
	@Override
	@SuppressWarnings("rawtypes")
	public Comparator getComparator() {
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void insert(Entry entry) {
		Object key = entryKey(entry);
		Object sKey = CohIndexHelper.extractFromEntryOrValue(entry, seriesKeyExtractor);
		Object ts = CohIndexHelper.extractFromEntryOrValue(entry, timestampExtractor);
		addToIndex(sKey, ts, key);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void update(Entry entry) {
		Object key = entryKey(entry);
		
		Object sKeyN = CohIndexHelper.extractFromEntryOrValue((MapTrigger.Entry) entry, seriesKeyExtractor);
		Object tsN = CohIndexHelper.extractFromEntryOrValue((MapTrigger.Entry) entry, timestampExtractor);
		Object sKeyO = CohIndexHelper.extractFromOriginalValue((MapTrigger.Entry) entry, seriesKeyExtractor);
		Object tsO = CohIndexHelper.extractFromOriginalValue((MapTrigger.Entry) entry, timestampExtractor);
		
		if (equals(tsN, tsO, timestampComparator) && equals(sKeyN, sKeyO, null)) {
			// no change in index
			return;
		}
		else {
			removeFromIndex(sKeyO, tsO, key);
			addToIndex(sKeyN, tsN, key);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void delete(Entry entry) {
		Object key = entryKey(entry);
		Object sKey = CohIndexHelper.extractFromOriginalValue((MapTrigger.Entry) entry, seriesKeyExtractor);
		Object ts = CohIndexHelper.extractFromOriginalValue((MapTrigger.Entry) entry, timestampExtractor);
		removeFromIndex(sKey, ts, key);
	}
	
	private void addToIndex(Object sKey, Object ts, Object key) {
		while(true) {
			SortedMap<Object, Object> line = getLine(sKey);
			synchronized(line) {
				line.put(ts, key);
				if (line == getLine(sKey)) {
					return;
				}
			}					
		}
	}
	
	private void removeFromIndex(Object sKey, Object ts, Object entry) {
		while(true) {
			SortedMap<Object, Object> line = getLine(sKey);
			synchronized(line) {
				line.remove(ts);
				if (line.isEmpty()) {
					if (index.remove(sKey, line)) {
						return;
					}
				}
				else if (line == getLine(sKey)) {
					return;
				}
			}					
		}
	}

	private NavigableMap<Object, Object> getLine(Object sKey) {
		while(true) {
			NavigableMap<Object, Object> line = index.get(sKey);
			if (line == null) {
				line = new TreeMap<Object, Object>();
				index.putIfAbsent(sKey, line);
			}
			else {
				return line;
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean equals(Object a, Object b, Comparator cmp) {
		if (cmp != null) {
			return cmp.compare(a, b) == 0;
		}
		else if (a == null && b == null) {
			return true;
		}
		else if (a == null || b == null) {
			return false;
		}
		else {
			return a.equals(b);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Object entryKey(Entry entry) {
		if (entry instanceof BinaryEntry) {
			return ((BinaryEntry)entry).getBinaryKey();
		}
		else {
			return entry.getKey();
		}
	}
	
	private static class SimpleEntry<K, V> implements Map.Entry<K, V> {
		private final K key;
		private final V value;
		
		@SuppressWarnings("unused")
		public SimpleEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public SimpleEntry(Map.Entry<K, V> entry) {
			this.key = entry.getKey();
			this.value = entry.getValue();
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}
	}
}
