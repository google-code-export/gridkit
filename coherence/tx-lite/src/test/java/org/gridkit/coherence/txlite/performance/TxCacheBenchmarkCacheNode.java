package org.gridkit.coherence.txlite.performance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class TxCacheBenchmarkCacheNode {
	
	static void println() {
	    System.out.println();
	}
	
	static void println(String text) {
	    System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", new Date()) + text);
	}
	
	public static void main(String[] args) {

//        System.setProperty("tangosol.coherence.edition", "SE");

	    System.setProperty("tangosol.pof.enabled", "false");
//	    System.setProperty("tangosol.pof.config", "capacity-benchmark-pof-config.xml");
	    System.setProperty("tangosol.coherence.cacheconfig", "tx-lite-test-cache-config.xml");
	    System.setProperty("tangosol.coherence.distributed.localstorage", "true");
	    
		try {
			final NamedCache cacheT = CacheFactory.getCache("t-objects");
//			final NamedCache cacheTF = CacheFactory.getCache("tf-objects");
			final NamedCache cacheTX = CacheFactory.getCache("tx-objects");
//			cache.addMapListener(new MapTriggerListener(new PrintMapTrigger()));

			println("Cache node has started");
			println("Objects in t-* cache: " + cacheT.size());
//			println("Objects in tf-* cache: " + cacheTF.size());
			println("Objects in tx-* cache: " + cacheTX.size());
			
			while(true) {
			    String cmd = new BufferedReader(new InputStreamReader(System.in)).readLine();
				if ("EXIT".equals(cmd.toUpperCase())) {
				    CacheFactory.getCluster().shutdown();
				}
				else {
				    System.out.println("Unknown command " + cmd);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
