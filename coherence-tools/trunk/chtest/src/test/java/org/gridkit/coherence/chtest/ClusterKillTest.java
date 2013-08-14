package org.gridkit.coherence.chtest;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CacheConfig.DistributedScheme;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.vicluster.telecontrol.jvm.JvmProps;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class ClusterKillTest {

	@Rule
	public DisposableCohCloud cloud = new DisposableCohCloud();
	
	private void configureCaches(CohNode nodes) {
		// normally you would use xml like this
		//
		// nodes.cacheConfig("my-cache-config.xml");
		//
		// but here I would like to embed simple config into test
		DistributedScheme scheme = CacheConfig.distributedSheme();
		scheme.backingMapScheme(CacheConfig.localScheme());
		scheme.autoStart(true);
		nodes.useEmptyCacheConfig().mapCache("*", scheme);
	}
	
	@Test
	public void test_crash_with_3_7_1_7() throws InterruptedException {
		cloud.all().useCoherenceVersion("3.7.1.7");
		crashCluster();
	}

	@Test
	public void test_crash_with_3_7_1_8() throws InterruptedException {
		cloud.all().useCoherenceVersion("3.7.1.8");
		crashCluster();		
	}
	
    public void crashCluster() {
        
    	// quick setting for local cluster
    	cloud.all().presetFastLocalCluster();
    	// TCP ring is disabled by "fast local preset", but we need it for death detection
    	cloud.all().enableTcpRing(true);
    	
    	// Set up the JVM args
        JvmProps.at(cloud.all()).addJvmArgs("-Xmx200m", "-Xms200m", "-server");
        
        // We can run in or out of process by flipping this flag
        cloud.all().outOfProcess(true);
        // Set up some params
        cloud.all().logLevel(9);
        configureCaches(cloud.all());
        cloud.all().autoStartServices();      
        
        // Declared 3 servers
        for(int i=0;i<3;i++) { 
        	cloud.node("server"+i);
        } 
        
        cloud.node("server*").ensureCluster();
        
        // Let's put some data
        cloud.node("server0").exec(new Runnable() {
			@Override
			public void run() {
				NamedCache cache = CacheFactory.getCache("TestCache");
				for(int i = 0; i != 1000; ++i) {
					cache.put(i, i);
				}
				Assert.assertTrue("Cache size (" + cache.size() + ") should be 1000", cache.size() == 1000);
			}
		});
        
        System.out.println("Killing couple of nodes");
        cloud.nodes("server1", "server2").kill();

        // Let's put some data
        cloud.node("server0").exec(new Runnable() {
			@Override
			public void run() {
				NamedCache cache = CacheFactory.getCache("TestCache");
				// let's make sure that some data was lost				
				Assert.assertTrue("Cache size (" + cache.size() + ") should less than 1000", cache.size() < 1000);
			}
		});
    }	
}
