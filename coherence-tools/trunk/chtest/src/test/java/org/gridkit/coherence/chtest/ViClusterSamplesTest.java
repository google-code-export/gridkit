package org.gridkit.coherence.chtest;

import java.util.concurrent.Callable;

import org.gridkit.coherence.chtest.CohCloud;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import com.tangosol.net.CacheFactory;
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
	public CohCloudRule cloud = new DisposableCohCloud() {{
		// Here we can configure our default test
		// topology used in all test.
		
		// Use present for single host Coherence cluster
		node("**").fastLocalClusterPreset();

		// use default config file from coherence.jar
		node("**").cacheConfig("/coherence-cache-config.xml");
		
		// set 'tangosol.coherence.distributed.localstorage'
		// based on role of node
		node("**.storage.**").localStorage(true);
		node("**.client.**").localStorage(false);
		
		// configure storage node to use DefaultServer class for initialization
		node("**.storage.**").autoStartServices();
	}};
	
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
}
