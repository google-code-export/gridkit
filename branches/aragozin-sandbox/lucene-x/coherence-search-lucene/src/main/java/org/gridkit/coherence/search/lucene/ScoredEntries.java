package org.gridkit.coherence.search.lucene;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

public interface ScoredEntries<K, V> {

	public float highScore();
	
	public int totalHits();
	
	public int size();
	
	public void fetchValues();
	
	public List<K> keys();

	public List<V> values();
	
	public List<ScoredEntry<K, V>> entries();
	
	public Map<K, V> asOrderedMap();
	
}
