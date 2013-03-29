package org.gridkit.coherence.test.rwbm;

import java.io.Serializable;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.coherence.chtest.CohCloudRule;
import org.gridkit.coherence.chtest.DisposableCohCloud;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.AbstractCacheLoader;
import com.tangosol.net.cache.CacheLoader;
import com.tangosol.net.cache.LocalCache;
import com.tangosol.util.Binary;
import com.tangosol.util.MapListener;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.MapTriggerListener;

public class BinaryEntryCheck {

	@Rule
	public CohCloudRule cloud = new DisposableCohCloud();
	
	@Test
	public void test_binary_store__vanila() {
		test_binary_store("vanila-A");
	}
	
	public void test_binary_store(final String cacheName) {
		
		cloud.all().presetFastLocalCluster();
		cloud.all().cacheConfig("/binary-expiry-cache-store-cache-config.xml");
		
		CohNode storage = cloud.node("storage");
		storage.localStorage(true);
		storage.autoStartServices();
		
		storage.getCache(cacheName);
		
		CohNode client = cloud.node("client");
		client.localStorage(false);
		
		cloud.all().ensureCluster();
		
		client.exec(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				
				NamedCache cache = CacheFactory.getCache(cacheName);

				String val = (String) cache.get(1);
				
				Assert.assertEquals("1-0", val);
				
				Thread.sleep(100);

				val = (String) cache.get(1);
				Assert.assertEquals("1-1", val);
				
				val = (String) cache.get(1000);
				Assert.assertEquals("1000-2", val);
				
				Thread.sleep(5);

				val = (String) cache.get(1000);
				Assert.assertEquals("1000-2", val);
				
				return null;
			}
		});
	}
	
	
	@SuppressWarnings("serial")
	public static class NullRemovingTrigger implements MapTrigger, Serializable {
		
		public static MapListener create() {
			System.out.println("Create trigger");
			return new MapTriggerListener(new NullRemovingTrigger());
		}

		@Override
		public void process(Entry entry) {
			System.out.println("Trigger event " + entry);
			if (entry.getValue() == null) {
				entry.remove(true);
			}
		}		
	}
	
	public static class OddLoader extends AbstractCacheLoader {
		@Override
		public Object load(Object key) {
			if (key instanceof Integer) {
				if (((Integer)key).intValue() % 2 == 1) {
					return key;
				}
			}
			return null;
		}
	}
	
	@SuppressWarnings("serial")
	public static class NullRemovingLocalCache extends LocalCache {

		private static Binary NULL = new Binary(new byte[]{0});
		private static Binary POF_NULL = new Binary(new byte[]{21, 100});
		
		public NullRemovingLocalCache() {
			super();
		}

		public NullRemovingLocalCache(int cUnits, int cExpiryMillis,
				CacheLoader loader) {
			super(cUnits, cExpiryMillis, loader);
		}

		public NullRemovingLocalCache(int cUnits, int cExpiryMillis) {
			super(cUnits, cExpiryMillis);
		}

		public NullRemovingLocalCache(int cUnits) {
			super(cUnits);
		}

		@Override
		@SuppressWarnings("deprecation")
		public Object put(Object oKey, Object oValue) {
			if (NULL.equals(oValue) || POF_NULL.equals(oValue)) {
				// ignoring nulls
				return null;
			}
			return super.put(oKey, oValue);
		}

		@Override
		@SuppressWarnings("deprecation")
		public Object put(Object oKey, Object oValue, long cMillis) {
			if (NULL.equals(oValue) || POF_NULL.equals(oValue)) {
				// ignoring nulls
				return null;
			}
			return super.put(oKey, oValue, cMillis);
		}
	}
}
