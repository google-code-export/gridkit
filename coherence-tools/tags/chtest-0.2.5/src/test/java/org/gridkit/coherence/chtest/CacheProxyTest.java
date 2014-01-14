package org.gridkit.coherence.chtest;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.gridkit.coherence.chtest.CacheConfig.DistributedScheme;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import com.tangosol.net.NamedCache;

public class CacheProxyTest {

	@ClassRule
	public static DisposableCohCloud cloud = new DisposableCohCloud() {

		@Override
		protected void before() throws Throwable {
			super.before();
		}		
	};
	
	@BeforeClass	
	public static void initCluster() {
		
		DistributedScheme scheme = CacheConfig.distributedSheme();
		scheme.backingMapScheme(CacheConfig.localScheme());
		
		cloud.all().useEmptyCacheConfig().mapCache("*", scheme);
		
		cloud.all().presetFastLocalCluster();
		cloud.node("storage").localStorage(true);
		cloud.node("client").localStorage(false);
		
		cloud.all().ensureCluster();
		cloud.all().getCache("1");		
	}
	
	@Test
	public void verify_put() {
		cloud.node("client").getCache("test").put("A", "A");
		cloud.node("client").getCache("test").put("A", "B");
	}

	@Test
	public void verify_putAll() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("A", "A");
		data.put("B", "B");
		data.put("C", "C");
		cloud.node("client").getCache("test").putAll(data);
	}

	@Test
	public void verify_get() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("A", "A");
		data.put("B", "B");
		data.put("C", "C");
		NamedCache cache = cloud.node("client").getCache("test");
		cache.putAll(data);
		Assert.assertEquals("A", cache.get("A"));
		Assert.assertEquals("B", cache.get("B"));
		Assert.assertEquals("C", cache.get("C"));
	}

	@Test
	public void verify_getAll() {
		Map<String, String> data = new HashMap<String, String>();
		data.put("A", "A");
		data.put("B", "B");
		data.put("C", "C");
		NamedCache cache = cloud.node("client").getCache("test");
		cache.putAll(data);
		Map<?, ?>  result = cache.getAll(data.keySet());		
		Assert.assertEquals("A", result.get("A"));
		Assert.assertEquals("B", result.get("B"));
		Assert.assertEquals("C", result.get("C"));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void verify_values() {		
		Map<String, String> data = new TreeMap<String, String>();
		data.put("A", "A");
		data.put("B", "B");
		data.put("C", "C");
		NamedCache cache = cloud.node("client").getCache("test");
		cache.clear();
		cache.putAll(data);
		Assert.assertEquals(new TreeSet(data.values()), new TreeSet(cache.values()));
	}

	@Test
	public void verify_keySet() {		
		Map<String, String> data = new HashMap<String, String>();
		data.put("A", "A");
		data.put("B", "B");
		data.put("C", "C");
		NamedCache cache = cloud.node("client").getCache("test");
		cache.clear();
		cache.putAll(data);
		Assert.assertEquals(data.keySet(), cache.keySet());
	}

	@Test
	public void verify_entrySet() {		
		Map<String, String> data = new HashMap<String, String>();
		data.put("A", "A");
		data.put("B", "B");
		data.put("C", "C");
		NamedCache cache = cloud.node("client").getCache("test");
		cache.clear();
		cache.putAll(data);
		Assert.assertEquals(data.entrySet(), cache.entrySet());
	}
	
}
