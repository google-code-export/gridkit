/**
 * 
 */
package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import com.tangosol.net.DefaultCacheServer;

/**
 * @author akornev
 * @since 1.0
 */
public class RunCommandBenchmarkServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Configure coherence
		System.setProperty("tangosol.pof.config", "pof-config.xml");
		System.setProperty("tangosol.coherence.cacheconfig",
				"coherence-commandpattern-pof-cache-config.xml");
		System.setProperty("tangosol.coherence.clusterport", "9001");
		System.setProperty("tangosol.coherence.distributed.localstorage", "true");
		DefaultCacheServer.main(args);
	}
}
