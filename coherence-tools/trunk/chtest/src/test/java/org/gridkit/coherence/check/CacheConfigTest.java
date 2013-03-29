/**
 * Copyright 2013 Alexey Ragozin
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
