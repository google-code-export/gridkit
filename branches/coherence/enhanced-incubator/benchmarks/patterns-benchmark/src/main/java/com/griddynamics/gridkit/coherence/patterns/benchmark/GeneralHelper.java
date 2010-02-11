/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	
	public static void setCoherenceConfig(boolean localstorage)
	{
		// Configure coherence
		setSysProp("tangosol.pof.config"           , "benchmark-pof-config.xml");
		setSysProp("tangosol.coherence.cacheconfig", "benchmark-pof-cache-config.xml");
		setSysProp("tangosol.coherence.clusterport", "9001");
		
		setSysProp("benchmark.backend.start-ic", "true");
		
		if (localstorage)
			setSysProp("tangosol.coherence.distributed.localstorage", "true");
		else
			setSysProp("tangosol.coherence.distributed.localstorage", "false");
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
