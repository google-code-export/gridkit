package org.gridkit.coherence.store;

import org.gridkit.utils.vicluster.CohHelper;
import org.gridkit.utils.vicluster.ViCluster;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

public class ReplicationCheckApp {
	public static void main(String[] args) {
	    ViCluster cluster = new ViCluster("replicationCheck", "com.tangosol", "org.gridkit");
        CohHelper.enableFastLocalCluster(cluster);
        CohHelper.enableJmx(cluster);
        
        cluster.setProp("org.gridkit.batch-store-uploader.partition-count", "1");
        cluster.setProp("tangosol.coherence.cacheconfig", "batch-uploader-test-cache-config.xml");
        
        CohHelper.localstorage(cluster.node("client"), false);
        
        String cacheService = cluster.node("client").getServiceNameForCache("store-cache"); 
        
        cluster.node("storage1").getService(cacheService);
        
        cluster.node("client").exec(new Runnable() {
            @Override
            public void run() {
                NamedCache cache = CacheFactory.getCache("store-cache");
                BatchStoreUploader uploader = new BatchStoreUploader(cache);
                uploader.put("1", "test");
                uploader.put("2", "test2");
                uploader.flush();
            }
        });

        
        cluster.node("storage2").getService(cacheService);
        
        CohHelper.jmxWaitForStatusHA(cluster.node("storage2"), cacheService, "NODE-SAFE");

        cluster.node("storage1").kill();
        
        NamedCache cache = cluster.node("client").getCache("store-cache");
        System.out.println(cache.get("1"));
        System.out.println(cache.get("2"));
        
        cluster.shutdown();
	}
}
