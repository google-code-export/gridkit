package com.griddynamics.coherence.integration.spring;

/**
 * @author Dmitri Babaev
 */
public enum InvalidationStrategy {
	auto (3),
	all (2),
	present (1),
	none (0);
	
	private final int type;
	
	private InvalidationStrategy(int type) {
		this.type = type;
	}
	
	public int type() {
		return type;
	}
}
