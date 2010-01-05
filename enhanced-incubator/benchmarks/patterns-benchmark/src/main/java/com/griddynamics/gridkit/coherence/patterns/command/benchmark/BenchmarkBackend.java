/**
 * 
 */
package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import com.tangosol.net.DefaultCacheServer;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class BenchmarkBackend {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Configure coherence
		setSysProp("tangosol.pof.config", "benchmark-pof-config.xml");
		setSysProp("tangosol.coherence.cacheconfig", "benchmark-pof-cache-config.xml");
		setSysProp("tangosol.coherence.clusterport", "9001");
		setSysProp("tangosol.coherence.distributed.localstorage", "true");
		DefaultCacheServer.main(args);
	}
	
	private static void setSysProp(String prop, String value) {
		if (System.getProperty(prop) == null) {
			System.setProperty(prop, value);
		}
		System.out.println("[SysProp] " + prop + ": " + System.getProperty(prop));
	}
}
