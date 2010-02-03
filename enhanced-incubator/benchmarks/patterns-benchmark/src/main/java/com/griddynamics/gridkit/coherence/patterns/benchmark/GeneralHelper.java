package com.griddynamics.gridkit.coherence.patterns.benchmark;

import java.util.Set;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;



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
	
	public static Set<Member> getOtherInvocationServiceMembers(InvocationService is)
	{
		@SuppressWarnings("unchecked")
		Set<Member> members = is.getInfo().getServiceMembers();
		members.remove(CacheFactory.getCluster().getLocalMember());
		
		return members;
	}
	
	/*
	public static void waitForLockCacheValue(String key, Integer value)
	{
		boolean needInc = true;
		
		NamedCache lockCache = CacheFactory.getCache(Names.lockCache);
		
		Integer scv = Integer.MIN_VALUE;
		
		while (!scv.equals(value))
		{
			lockCache.lock("startCounterValue");
			
			scv = (Integer)lockCache.get("startCounterValue");
			
			if (needInc)
			{
				if (scv == null)
					scv = new Integer(1);
				else
					scv = scv + 1;
				
				needInc = false;
			}
			
			lockCache.unlock("startCounterValue");
			
			Thread.sleep(value);
		}
	}
	*/
}
