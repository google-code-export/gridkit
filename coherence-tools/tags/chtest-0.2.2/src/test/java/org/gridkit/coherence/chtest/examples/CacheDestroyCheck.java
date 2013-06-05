package org.gridkit.coherence.chtest.examples;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.gridkit.coherence.chtest.CacheConfig;
import org.gridkit.coherence.chtest.CacheConfig.DistributedScheme;
import org.gridkit.coherence.chtest.CacheConfig.ReadWriteBackingMap;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheStore;

/**
 * Setup to verify destruction of write behind thread then cache is destroyed
 */
public class CacheDestroyCheck {

	
	@Rule
	public DisposableCohCloud cloud = new DisposableCohCloud();
	
	@Before
	public void init() {
		
		CohNode all = cloud.all();
		all.presetFastLocalCluster();
		all.outOfProcess(true);
//		all.logLevel(2);
		
		ReadWriteBackingMap rwbm = CacheConfig.readWriteBackmingMap();
		rwbm.writeDelay("1s");
		rwbm.cacheStoreScheme(NullStore.class);
		rwbm.internalCacheScheme(CacheConfig.localScheme());
		
		DistributedScheme cacheScheme = CacheConfig.distributedSheme();
		cacheScheme.backingMapScheme(rwbm);
		
		all.mapCache("*", cacheScheme);
	}
	
	@Test
	public void verify_destruction() throws InterruptedException {

		cloud.node("storage")
		.localStorage(true);
		
		NamedCache a = cloud.node("storage").getCache("a");
		NamedCache b = cloud.node("storage").getCache("b");
		NamedCache c = cloud.node("storage").getCache("c");
		NamedCache d = cloud.node("storage").getCache("d");
		NamedCache e = cloud.node("storage").getCache("e");
		
		a.put("A", "A");
		b.put("A", "A");
		c.put("A", "A");
		d.put("A", "A");
		e.put("A", "A");
		
		Thread.sleep(3000);
		
		a.destroy();
		b.destroy();
		c.destroy();
		
		Thread.sleep(3000);		
	}

	@SuppressWarnings("rawtypes")
	public static class NullStore implements CacheStore {

		@Override
		public Object load(Object arg0) {
			return null;
		}

		@Override
		public Map loadAll(Collection arg0) {
			return Collections.EMPTY_MAP;
		}

		@Override
		public void erase(Object arg0) {
		}

		@Override
		public void eraseAll(Collection arg0) {
		}

		@Override
		public void store(Object arg0, Object arg1) {
		}

		@Override
		public void storeAll(Map arg0) {
		}
	}
}
