package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import com.tangosol.net.DefaultCacheServer;

public class FunctorBenchmarkBackend
{
	public static void main(String[] args)
	{
		// Configure coherence
		setSysProp("tangosol.pof.config", "benchmark-pof-config.xml");
		setSysProp("tangosol.coherence.cacheconfig", "benchmark-pof-cache-config.xml");
		setSysProp("tangosol.coherence.clusterport", "9001");
		setSysProp("tangosol.coherence.distributed.localstorage", "true");
		DefaultCacheServer.main(args);
	}
	
	private static void setSysProp(String prop, String value)
	{
		if (System.getProperty(prop) == null)
		{
			System.setProperty(prop, value);
		}
		System.out.println("[SysProp] " + prop + ": " + System.getProperty(prop));
	}
}
