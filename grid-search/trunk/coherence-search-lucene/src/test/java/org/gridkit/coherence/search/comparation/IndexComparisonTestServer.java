package org.gridkit.coherence.search.comparation;

import java.util.concurrent.locks.LockSupport;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;

/**
 * @author Alexander Solovyov
 */

public abstract class IndexComparisonTestServer {


    public static void main(String[] args) {
    	if (System.getProperty("tangosol.coherence.wka") == null) {
    		System.setProperty("tangosol.coherence.wka", "localhost");
    	}
        System.setProperty("tangosol.coherence.cluster", "index-comparison-test");

        System.setProperty("tangosol.coherence.distributed.localstorage", "true");
        
	    System.setProperty("tangosol.pof.enabled", "true");
	    System.setProperty("tangosol.pof.config", "pof-config.xml");
        
        CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("lucene-cache-config.xml"));
        
        CacheFactory.ensureCluster();
        CacheFactory.getCache("distributed-cache");
        
        System.out.println("Server has started");
        while(true) {
        	LockSupport.park();
        }
    }
}
