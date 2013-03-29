package org.gridkit.coherence.check;

import org.gridkit.coherence.chtest.CacheConfig;
import org.gridkit.coherence.chtest.CacheConfig.EvictionPolicy;
import org.gridkit.coherence.chtest.CacheConfig.LocalScheme;
import org.junit.Test;

public class CacheConfigTest {

	@Test
	public void local_scheme_test() {
		
		LocalScheme scheme = CacheConfig.localScheme();
		
		scheme.highUnits(100);
		scheme.lowUnits(90);
		scheme.evictionPolicy(EvictionPolicy.HYBRID);
		
		System.out.println(scheme.toString());
		
		scheme = CacheConfig.localScheme();
		scheme.highUnits(100);
		scheme.evictionPolicy(CacheConfig.intantiate(Policy.class, CacheConfig.Macro.CACHE_NAME));

		System.out.println(scheme.toString());		
	}
	
	public static class Policy {		
		public Policy(String cacheName) {			
		}
	}	
}
