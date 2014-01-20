package org.gridkit.coherence.check;

import org.gridkit.coherence.chtest.CacheConfig;
import org.gridkit.coherence.chtest.CacheConfig.DistributedScheme;
import org.gridkit.coherence.chtest.CacheConfig.ProxyScheme;
import org.gridkit.coherence.chtest.CacheConfig.RemoteCacheScheme;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.partition.PartitionSet;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.PartitionedFilter;

/**
 * I'm experiencing problems with partitioned filter. 
 * Tried to reproduce, but unsuccessful so far.
 *
 */
public class PartitionedFilterCrashCheck {

	
	public DisposableCohCloud cloud = new DisposableCohCloud();
	
	@Test
	public void verify_partitioned_filter_3_7_1_3() throws InterruptedException {
		cloud.all().useCoherenceVersion("3.7.1.3");
		testPartitionedFilter(false, true);
	}
	
	
	public void testPartitionedFilter(final boolean extend, final boolean useIndex) throws InterruptedException {
		
		DistributedScheme scheme = CacheConfig.distributedSheme();
		scheme.backingMapScheme(CacheConfig.localScheme());
		scheme.partitionCount(10);
		
		ProxyScheme proxy = CacheConfig.proxyScheme();
		proxy.serviceName("ExtendProxy");
		proxy.cacheProxyEnabled(true);
		proxy.tcpAcceptorLocalAddress("127.0.0.1", 12300);
		
		RemoteCacheScheme remoteCache = CacheConfig.remoteCacheScheme();
		remoteCache.tpcInitiatorRemoteAddress("127.0.0.1", 12300);
		
		cloud.node("cluster.**").mapCache("test", scheme);
		cloud.node("cluster.server*").localStorage(true);
		cloud.node("cluster.client").addScheme(proxy);
		cloud.node("cluster.client").localStorage(false);
		
		cloud.nodes("cluster.server1", "cluster.server2");
		cloud.node("cluster.**").getCache("test");
		cloud.node("cluster.client").ensureService("ExtendProxy");
		
		NamedCache cache = cloud.node("cluster.client").getCache("test");
		
		for(int i = 0; i != 100; ++i) {
			cache.put(i, i > 50 ? "0" : "1");
		}
		
		Thread.sleep(300);

		if (useIndex) {
			cache.addIndex(new ReflectionExtractor("toString"), false, null);
		}
		
		if (extend) {
			cloud.node("xclient").mapCache("test", remoteCache);
			cloud.node("xclient").enableTCMP(false);
		}
		
		CohNode testClient = extend ?cloud.node("xclient") : cloud.node("cluster.client");
		
		testClient.exec(new Runnable() {
			@Override
			public void run() {
				NamedCache cache = CacheFactory.getCache("test");
				Filter f = new EqualsFilter("toString", "0");
				PartitionSet ps = new PartitionSet(10);
				ps.add(1);
				ps.add(2);
				ps.add(3);
				int size = cache.keySet(new PartitionedFilter(f, ps)).size();
//				int size = cache.keySet(f).size();
				System.out.println("Size: " + size);
				
			}
		});
		
	}
	
}
