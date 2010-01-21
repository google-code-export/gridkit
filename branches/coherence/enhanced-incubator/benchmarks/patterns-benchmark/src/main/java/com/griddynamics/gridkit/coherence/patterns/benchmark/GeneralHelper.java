package com.griddynamics.gridkit.coherence.patterns.benchmark;

public class GeneralHelper
{
	public static void setSysProp(String prop, String value)
	{
		if (System.getProperty(prop) == null)
		{
			System.setProperty(prop, value);
		}
		sysOut("[SysProp] " + prop + ": " + System.getProperty(prop));
	}
	
	public static void sysOut(String text, Object... args)
	{
		System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", System.currentTimeMillis()) + String.format(text, args));
	}
	
	public static void setCoherenceConfig()
	{
		// Configure coherence
		setSysProp("tangosol.pof.config"           , "benchmark-pof-config.xml");
		setSysProp("tangosol.coherence.cacheconfig", "benchmark-pof-cache-config.xml");
		setSysProp("tangosol.coherence.clusterport", "9001");
	}
}
