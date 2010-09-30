/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
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
package org.gridkit.coherence.integration;

import org.gridkit.coherence.utils.classloader.Isolate;
import org.junit.Test;

import com.tangosol.net.CacheFactory;

/**
 *	@author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class TwoClusters {
	
	@Test
	public void twoClusters() {
		Isolate is1 = new Isolate("1", "com.tangosol", "org.gridkit");
		Isolate is2 = new Isolate("2", "com.tangosol", "org.gridkit");
		
		is1.start();
		is2.start();
		
		is1.submit(StartCluster.class.getName());
		is2.submit(StartCluster.class.getName());
		is1.submit(StopCluster.class.getName());
		is2.submit(StopCluster.class.getName());
	}
	
	public static class StartCluster implements Runnable {
		@Override
		public void run() {
			System.out.println("CacheFactory #" + CacheFactory.class.hashCode());
			CacheFactory.ensureCluster();
		}
	}
	
	public static class StopCluster implements Runnable {
		@Override
		public void run() {
			CacheFactory.getCluster().shutdown();
		}
	}
}
