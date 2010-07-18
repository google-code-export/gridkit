package org.gridkit.coherence.integration.spring.cache;

/**
 * @author Dmitri Babaev
 */
public enum CacheEvictionType {
	HYBRID (0),
	LRU (1),
	LFU (2);
	
	private final int type;
	
	CacheEvictionType(int type) {
		this.type = type;
	}
	
	public int type() {
		return type;
	}
}
