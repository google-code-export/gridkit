package com.griddynamics.coherence.test.cluster;

import java.util.HashMap;

public class TestHashMap extends HashMap {

	public TestHashMap(String val) {
		super();
		System.out.println("new TestHashMap: "+val);
	}
}
