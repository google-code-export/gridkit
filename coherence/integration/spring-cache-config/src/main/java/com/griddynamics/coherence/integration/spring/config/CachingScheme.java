package com.griddynamics.coherence.integration.spring.config;

/**
 * @author Dmitri Babaev
 */
public class CachingScheme {
	private String name;
	
	public CachingScheme() {
	}
	
	public CachingScheme(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
