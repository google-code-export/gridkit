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
package org.gridkit.coherence.chtest;

import java.util.concurrent.Callable;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.NamedCache;

public class ViClusterSamplesTest {
	
	/**
	 * {@link DisposableCohCloud} implements JUnit's {@link MethodRule}
	 * and will shutdown cloud after each test.
	 * <br/>
	 * 
	 * By default, {@link CohCloud} will isolate all application classes.
	 * Though, you can adjust these rules. 
	 */
	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	@Before
	public void configureCloud() {
		// Here we can configure our default test
		// topology used in all test.
		
		// Use present for single host Coherence cluster
		cloud.node("**").presetFastLocalCluster();

		// use default config file from coherence.jar
		cloud.node("**").cacheConfig("/coherence-cache-config.xml");
		
		// set 'tangosol.coherence.distributed.localstorage'
		// based on role of node
		cloud.node("**.storage.**").localStorage(true);
		cloud.node("**.client.**").localStorage(false);
		
		cloud.all().setTCMPTimeout(15000);
		
		// configure storage node to use DefaultServer class for initialization
		cloud.node("**.storage.**").autoStartServices();
	};
	
	@Test
	public void test_simple_cluster() {

		// Declare one node with storage role
		cloud.node("storage");

		// Declare two client nodes with data client role
		CohNode client1 = cloud.node("client.1");
		CohNode client2 = cloud.node("client.2");
		
		// Ensure cluster on all declared nodes
		cloud.node("**").ensureCluster();
		

		final String cacheName = "distr-a";
		
		// pure magic at this point
		// instance of callable will be serialized and deserialized with classloader of "client" node
		// and executed in context of client JVM
		// you can think of it a of remote call
		// return values or raised exceptions are converted to application classloader on way back
		client1.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				// final local variable can be accessed
				NamedCache cache = CacheFactory.getCache(cacheName);
			
				// asserts can be used here
				// exception will be transfered back to caller
				cache.put(0, 0);
				Assert.assertEquals(0, cache.get(0));
				
				return null;
			}
		});

		// Now we can check if second client could see data in cache
		client2.exec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				NamedCache cache = CacheFactory.getCache(cacheName);
				Assert.assertEquals(0, cache.get(0));
				return null;
			}
		});
		
		// We also can do simple cache operation with cache using proxy
		Assert.assertEquals(0, client2.getCache(cacheName).get(0));
	}
	
	@Test
	public void test_parameter_passing() {

		// For this test, we do not need Coherence cluster
		// let's use client node and skip cluster startup
		CohNode client = cloud.node("client");
		
		final double doubleV = 1.1d;
		
		client.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {

				// final local variable from outer scope can be accessed as usual
				Assert.assertEquals(1.1d, doubleV, 0d);
				return null;
			}
		});

		final double[] doubleA = new double[]{1.1d};
		
		client.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				Assert.assertEquals(1.1d, doubleA[0], 0d);
				
				// this will not be visible to caller, 
				// code inside of isolate is working with copy of array				
				doubleA[0] = 2.2d;
				return null;
			}
		});

		// array is outer scope there not changed
		Assert.assertEquals(1.1d, doubleA[0], 0d);
	}	

	void doSomething() {		
	}
	
	@Test(expected=NullPointerException.class)
	public void test_outter_methods_unaccessible() {

		CohNode client = cloud.node("client");
		
		client.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				// this will cause NPE
				// instance to outer class were not passed to isolate
				// this limitation is intentional
				doSomething();

				return null;
			}
		});
	}	
	
	@Test
	public void kill_coherence_node() throws InterruptedException {
		cloud.node("storage.1");
		cloud.node("storage.2");
		cloud.node("storage.3");

		cloud.node("**").getCache("distr-a");
		
		Thread.sleep(5000);

		NamedCache cache = cloud.node("storage.2").getCache("distr-a");
		for(int i = 0; i != 1000; ++i) {
			cache.put(i, i);
		}
		
		cloud.node("storage.1").exec(new Runnable() {
			@Override
			public void run() {
				System.out.println("Going to kill cluster");
				Cluster cluster = CacheFactory.getCluster();
				cluster.stop();
				System.out.println("Killing cluster node");
			}
		});
		
		Thread.sleep(5000);
		
		int n = cloud.node("storage.2").exec(new Callable<Integer>() {
			@Override
			public Integer call() {
				NamedCache cache = CacheFactory.getCache("distr-a");
				int n = 0;
				for(int i = 0; i != 1000; ++i) {
					n += cache.get(i) == null ? 0 : 1;
				}
				return n;
			}
		});
		
		Assert.assertEquals(1000, n);
	}
}
