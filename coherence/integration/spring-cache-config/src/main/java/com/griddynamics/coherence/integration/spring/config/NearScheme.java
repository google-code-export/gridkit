package com.griddynamics.coherence.integration.spring.config;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Dmitri Babaev
 */
@XmlType(name="near-scheme")
public class NearScheme extends CachingScheme {
	
	public NearScheme() {
	}

	public NearScheme(String name) {
		super(name);
	}

}
