package org.gridkit.coherence.util.vicluster;

import java.util.concurrent.Callable;

import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;
import org.gridkit.utils.vicluster.ViNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.NamedCache;

public class ViClusterSamplesTest {
	
	ViCluster cluster;
	
	@After
	public void dropCluster() {
		// It is not recommended to shutdown cluster after each test because
		// cluster startup takes few seconds.
		// Normally you would setup your application topology once and resuse it
		// in multiple tests
		if (cluster != null) {
			cluster.shutdown();
		}
	}
	
	@Test
	public void test_simple_cluster() {
		// you should add to isolate package list
		// * Coherence (com.tangosol)
		// * GridKit (org.gridkit)
		// * Your application package
		// * Some libraries should also be included to work properly (e.g. mockito)
		cluster = new ViCluster("simple_cluster", "org.gridkit", "com.tangosol");
		
		// present for in-JVM cluster
		CohHelper.enableFastLocalCluster(cluster);
		// using default config in this case
		CohHelper.cacheConfig(cluster, "/coherence-cache-config.xml");

		// Creating server node
		ViNode storage = cluster.node("storage");
		CohHelper.localstorage(storage, true);
		
		// simulating DefaultCacheServer startup
		storage.start(DefaultCacheServer.class);
		
		storage.getCluster();
		
		ViNode client = cluster.node("client");
		CohHelper.localstorage(client, false);

		final String cacheName = "distr-a";
		
		// pure magic at this point
		// instance of callable will be serialized and deserialized with classloader of "client" node
		// and executed in context of client JVM
		// you can think of it a of remote call
		// return values or raised exceptions are converted to application classloader on way back
		client.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);
				
				cache.put(0, 0);
				Assert.assertEquals(0, cache.get(0));
				
				return null;
			}
		});
	}
	
	@Test
	public void test_parameter_passing() {

		cluster = new ViCluster("test_parameter_passing", "org.gridkit");
		
		final double doubleV = 1.1d;
		
		cluster.node("node").exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {

				// final local variable from outer scope can be accessed as usual
				Assert.assertEquals(1.1d, doubleV, 0d);
				return null;
			}
		});

		final double[] doubleA = new double[]{1.1d};
		
		cluster.node("node").exec(new Callable<Void>(){
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

		cluster = new ViCluster("test_parameter_passing", "org.gridkit");
		
		cluster.node("node").exec(new Callable<Void>(){
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
