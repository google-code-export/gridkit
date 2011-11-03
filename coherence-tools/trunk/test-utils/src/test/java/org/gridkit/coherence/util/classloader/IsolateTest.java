/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
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
package org.gridkit.coherence.util.classloader;

import java.io.Serializable;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Some unit tests for Isolate
 * 
 * @author malekseev
 * @see Isolate
 */
public class IsolateTest {
	
	@Test
	public void twoNodes() throws Exception {
		
		// Initialize and start isolates
		Isolate is1 = new Isolate("node-1", "com.tangosol", "org.gridkit");
		Isolate is2 = new Isolate("node-2", "com.tangosol", "org.gridkit");
		is1.start();
		is2.start();
		
		// Start Coherence nodes within isolates
		is1.submit(NodeActions.Start.class, "test-cache-config.xml");
		is2.submit(NodeActions.Start.class, "test-cache-config.xml");
		
		// Simulate second node crash
		Thread.sleep(3 * 1000L);
		is2.submit(NodeActions.Crash.class, 1);
		Thread.sleep(3 * 1000L);

		Assert.assertFalse(new ClassloaderAssert().call().contains("Isolate"));
		Assert.assertTrue(is1.exec(new ClassloaderAssert()).contains("Isolate"));
		
		// Stop first node
		is1.submit(NodeActions.Stop.class);
		
	}
	
	@SuppressWarnings("serial")
	static class ClassloaderAssert implements Serializable, Callable<String> {
		@Override
		public String call() throws Exception {
			return this.getClass().getClassLoader().getClass().getName();
		}		
	}	
}
