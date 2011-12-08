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

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

/**
 * Some unit tests for Isolate
 * 
 * @author malekseev
 * @see Isolate
 */
public class IsolateTest {
	
	@SuppressWarnings("deprecation")
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
		
		is1.stop();
		is2.stop();
	}

	@Test
	public void propertyTest() throws Exception {
		
		// Initialize and start isolates
		Isolate is1 = new Isolate("node-1", "com.tangosol", "org.gridkit");
		Isolate is2 = new Isolate("node-2", "com.tangosol", "org.gridkit");
		is1.start();
		is2.start();

		is1.exec(new Runnable() {
			@Override
			public void run() {
				System.setProperty("local-prop", "Isolate1");				
			}
		});

		is2.exec(new Runnable() {
			@Override
			public void run() {
				System.setProperty("local-prop", "Isolate2");				
			}
		});

		is1.exec(new Runnable() {
			@Override
			public void run() {
				Assert.assertEquals("Isolate1", System.getProperty("local-prop"));				
			}
		});
		
		is2.exec(new Runnable() {
			@Override
			public void run() {
				Assert.assertEquals("Isolate2", System.getProperty("local-prop"));				
			}
		});		

		final String xxx = new String("Hallo from Isolate2");
		is2.exec(new Runnable() {
			@Override
			public void run() {
				Assert.assertEquals("Hallo from Isolate2", xxx);				
			}
		});		
		
		Assert.assertNull(System.getProperty("local-prop"));
		
		is1.stop();
		is2.stop();
	}
	
	@Test
	public void proxyTest() throws Exception {
		
		// Initialize and start isolates
		Isolate is1 = new Isolate("node-1", "com.tangosol", "org.gridkit");
		Isolate is2 = new Isolate("node-2", "com.tangosol", "org.gridkit");
		is1.start();
		is2.start();

		NamedCache cache1 = is1.export(new Callable<NamedCache>() {
			@Override
			public NamedCache call() throws Exception {
				return CacheFactory.getCache("distr-A");
			}
		});

		NamedCache cache2 = is2.export(new Callable<NamedCache>() {
			@Override
			public NamedCache call() throws Exception {
				return CacheFactory.getCache("distr-A");
			}
		});
		
		cache1.put("A", "A");
		
		Assert.assertEquals("Cache size ", 1, cache2.size());
		Assert.assertEquals("Value at 'A' ", "A", cache2.get("A"));
		
		cache2.remove("A");

		Assert.assertEquals("Cache size ", 0, cache1.size());
		Assert.assertEquals("Value at 'A' ", null, cache1.get("A"));
		
		is1.stop();
		is2.stop();
	}
	
	@SuppressWarnings("serial")
	static class ClassloaderAssert implements Serializable, Callable<String> {
		@Override
		public String call() throws Exception {
			return this.getClass().getClassLoader().getClass().getName();
		}		
	}	
	
	@Test
	public void test_stack_trace() {

		Isolate is1 = new Isolate("node-1", "com.tangosol", "org.gridkit");
		is1.start();
		
		try {
			is1.exec(new Runnable() {
				@Override
				public void run() {
					throw new IllegalArgumentException("test");
				}
			});
			Assert.assertFalse(true);
		}
		catch(IllegalArgumentException e) {
			e.printStackTrace();
			Assert.assertEquals("Stack trace lenght ", 26, e.getStackTrace().length);
		}
	}
	
	@Test
	public void test_stack_trace2() {

		Isolate is1 = new Isolate("node-1", "com.tangosol", "org.gridkit");
		is1.start();
		
		try {
			Runnable r = is1.export(new Callable<Runnable>() {
				public Runnable call() {
					return 	new Runnable() {
						@Override
						public void run() {
							throw new IllegalArgumentException("test2");
						}
					};
				}
			});

			r.run();
			
			Assert.assertFalse(true);
		}
		catch(IllegalArgumentException e) {
			e.printStackTrace();
			Assert.assertEquals("Stack trace lenght ", 26, e.getStackTrace().length);
		}
	}
	
}
