package org.gridkit.coherence.search.comparation;

import org.junit.Ignore;

import com.tangosol.net.CacheFactory;

@Ignore
public class TestStorageNode {

	static void sysProp(String propName, String value) {
		if (System.getProperty(propName) == null) {
			System.setProperty(propName, value);
		}
		System.out.println("sysProp: " + propName + ": " + System.getProperty(propName));
	}
	
	public static void main(String[] args) throws InterruptedException {

		sysProp("tangosol.pof.enabled", "true");
	    sysProp("tangosol.pof.config", "pof-config.xml");
	    sysProp("tangosol.coherence.cacheconfig", "index-test-cache-config.xml");
	    sysProp("tangosol.coherence.distributed.localstorage", "true");
		
		CacheFactory.getCache("objects");
		
		System.out.println("Done");
		
		while(true) {
			Thread.sleep(300);
		}
	}
	
}
