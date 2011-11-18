package org.gridkit.coherence.search.lucene.util;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gridkit.coherence.search.lucene.ScoredEntries;
import org.gridkit.coherence.search.lucene.ScoredEntry;

import com.tangosol.net.NamedCache;

public class TopEntriesContainer<K, V> implements ScoredEntries<K, V> {

	private NamedCache cache;
	private int totalHits;
	private float maxScore;
	
	private ScoredTuple[] entries;
	private int size;
	
	private transient List<K> keyView;
	private transient List<V> valueView;
	private transient Map<K, V> mapView;
	private transient List<ScoredEntry<K, V>> entryView;
	
	@SuppressWarnings("unchecked")
	public TopEntriesContainer(NamedCache cache, int capacity) {
		this.cache = cache;
		this.entries = (ScoredTuple[]) new TopEntriesContainer.ScoredTuple[capacity];		
	}
	
	public void append(K key, float score) {
		if (size >= entries.length) {
			throw new ArrayIndexOutOfBoundsException("Capacity limit is reached");
		}
		else {
			ScoredTuple tuple = new ScoredTuple(key, score);
			entries[size++] = tuple;
		}
	}
	
	public void setTotalHits(int hits) {
		this.totalHits = hits;
	}
	
	public void setMaxScore(float maxScore) {
		this.maxScore = maxScore;
	}

	@Override
	public float highScore() {
		return maxScore;
	}

	@Override
	public int totalHits() {
		return totalHits;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void fetchValues() {
		Map map = cache.getAll(keys());
		for(int i = 0; i != size; ++i) {
			entries[i].loadValue(map);
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public List<K> keys() {
		if (keyView == null) {
			keyView = new KeyView();
		}
		return keyView;
	}


	@Override
	public List<V> values() {
		if (valueView == null) {
			valueView = new ValueView();
		}
		return valueView;
	}

	@Override
	public List<ScoredEntry<K, V>> entries() {
		if (entryView == null) {
			entryView = new EntryView();
		}
		return entryView;
	}

	@Override
	public Map<K, V> asOrderedMap() {
		if (mapView == null) {
			mapView = new MapView();
		}
		return mapView;
	}

	private class ScoredTuple implements ScoredEntry<K, V> {
		
		private K key;
		private float score;
		private boolean loaded;
		private V value;
		
		public ScoredTuple(K key, float score) {
			this.key = key;
			this.score = score;
			this.loaded = false;			
		}
		
		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public float getScore() {
			return score;
		}
		
		@Override
		public K getKey() {
			return key;
		}
		
		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public V getValue() {
			if (!loaded) {
				loadValue((Map)cache);
			}
			return value;
		}
		
		public void loadValue(Map<K, V> map) {
			if (!loaded) {
				value = map.get(key);
				loaded = true;
			}
		}
	}
	
	private abstract class AbstractListView<X> extends AbstractList<X> {

		protected abstract X convert(ScoredTuple tuple);
		
		@Override
		public X get(int index) {
			if (index < 0 || index >= size) {
				throw new IndexOutOfBoundsException("Invalid index " + index + " size is " + size());
			}
			return convert(entries[index]);
		}

		@Override
		public int size() {
			return TopEntriesContainer.this.size();
		}		
	}
	
	private class KeyView extends AbstractListView<K> {
		@Override
		protected K convert(ScoredTuple tuple) {
			return tuple.getKey();
		}
	}

	private class EntryView extends AbstractListView<ScoredEntry<K, V>> implements Set<ScoredEntry<K, V>> {
		@Override
		protected ScoredEntry<K, V> convert(ScoredTuple tuple) {
			return tuple;
		}
	}

	private class ValueView extends AbstractListView<V> {
		@Override
		protected V convert(ScoredTuple tuple) {
			return tuple.getValue();
		}
	}
	
	private class MapView extends AbstractMap<K, V> {

		private Map<K, ScoredTuple> index = new HashMap<K, ScoredTuple>();
		
		public MapView() {
			for(int i = 0; i != size(); ++i) {
				ScoredTuple t = entries[i];
				index.put(t.getKey(), t);
			}
		}
		
		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Set<Entry<K, V>> entrySet() {
			return (Set)TopEntriesContainer.this.entries();
		}

		@Override
		public boolean containsKey(Object key) {
			return index.containsKey(key);
		}

		@Override
		public V get(Object key) {
			ScoredTuple t = index.get(key);
			if (t == null) {
				return null;
			}
			else {
				return t.getValue();
			}
		}

		@Override
		public int size() {
			return TopEntriesContainer.this.size();
		}
	}
}
