package com.griddynamics.gridkit.coherence.patterns.benchmark;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setCoherenceConfig;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;

import com.tangosol.net.DefaultCacheServer;

public class Backend
{
	public static void main(String[] args)
	{
		// Configure coherence
		setCoherenceConfig();
		setSysProp("tangosol.coherence.distributed.localstorage", "true");
		DefaultCacheServer.main(args);
	}
}
