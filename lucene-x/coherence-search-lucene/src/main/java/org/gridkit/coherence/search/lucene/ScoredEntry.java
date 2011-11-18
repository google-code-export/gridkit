package org.gridkit.coherence.search.lucene;

import java.util.Map;

public interface ScoredEntry<K, V> extends Map.Entry<K, V> {

	public float getScore();
	
	public K getKey();
	
	public V getValue();
	
}
