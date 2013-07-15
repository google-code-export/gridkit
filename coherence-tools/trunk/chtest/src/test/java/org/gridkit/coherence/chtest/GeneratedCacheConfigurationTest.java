/**
 * Copyright 2013 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.chtest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.gridkit.coherence.chtest.CacheConfig.DistributedScheme;
import org.gridkit.coherence.chtest.CacheConfig.LocalScheme;
import org.gridkit.coherence.chtest.CacheConfig.ProxyScheme;
import org.gridkit.coherence.chtest.CacheConfig.ReadWriteBackingMap;
import org.gridkit.coherence.chtest.CacheConfig.RemoteCacheScheme;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.BinaryEntryStore;
import com.tangosol.net.cache.CacheLoader;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;

public class GeneratedCacheConfigurationTest {

	@Rule
	public DisposableCohCloud cloud = new DisposableCohCloud();
	
	@Before
	public void configureCloud() {
		// needed for 12.1.2
		cloud.all().setProp("tangosol.coherence.cachefactory", DefaultConfigurableCacheFactory.class.getName());
	}
	
	@Test
	public void verify_proxy_extend_configuration() {

		String cacheName = "test-cache";
		int extendPort = 12301;
		
		cloud.all().useEmptyCacheConfig();
		
		CohNode server = cloud.node("server");
		CohNode xclient = cloud.node("xclient");
		
		server.presetFastLocalCluster();
		xclient.enableTCMP(false);

		LocalScheme serverCache = CacheConfig.localScheme();
		server.mapCache(cacheName, serverCache);
		
		ProxyScheme proxyScheme = CacheConfig.proxyScheme();
		proxyScheme.schemeName("proxy-scheme");
		proxyScheme.serviceName("ProxyService");
		proxyScheme.cacheProxyEnabled(true);
		proxyScheme.serializer("pof");
		proxyScheme.tcpAcceptorLocalAddress("127.0.0.1", extendPort);
		proxyScheme.autoStart(true);

		server.addScheme(proxyScheme);
		
		// auto start should be added after all cache config fragments
		server.autoStartServices();

		RemoteCacheScheme remoteCache = CacheConfig.remoteCacheScheme();
		remoteCache.serializer("pof");
		remoteCache.tpcInitiatorRemoteAddress("127.0.0.1", extendPort);
		
		xclient.mapCache(cacheName, remoteCache);
		
		cloud.all().touch();
		
		NamedCache scache = server.getCache(cacheName);
		NamedCache ccache = xclient.getCache(cacheName);
		
		scache.put("A", "aaa");
		
		Assert.assertEquals("aaa", scache.get("A"));
		Assert.assertEquals("aaa", ccache.get("A"));
	}

	@Test
	public void verify_distributed_read_through_configuration() {

		cloud.all().useEmptyCacheConfig();

		CohNode server = cloud.node("server");
		server.presetFastLocalCluster();		
		
		ReadWriteBackingMap rwbm1 = CacheConfig.readWriteBackmingMap();
		rwbm1.internalCacheScheme(CacheConfig.localScheme());
		rwbm1.cacheStoreScheme(TestCacheLoader.class, CacheConfig.Macro.CACHE_NAME);

		ReadWriteBackingMap rwbm2 = CacheConfig.readWriteBackmingMap();
		rwbm2.internalCacheScheme(CacheConfig.localScheme());
		rwbm2.cacheStoreScheme(TestBinaryEntryStore.class, "rwbm2", CacheConfig.Macro.MANAGER_CONTEXT);
	
		DistributedScheme ds1 = CacheConfig.distributedSheme();
		ds1.backingMapScheme(rwbm1);

		DistributedScheme ds2 = CacheConfig.distributedSheme();
		ds2.backingMapScheme(rwbm2);
		
		server.mapCache("cache1", ds1);
		server.mapCache("cache2", ds2);
		
		NamedCache cache1 = server.getCache("cache1"); 
		NamedCache cache2 = server.getCache("cache2");
		
		Assert.assertEquals("A@cache1", cache1.get("A"));
		Assert.assertEquals("111@rwbm2", cache2.get(111));
		
	}
	
	@SuppressWarnings("rawtypes")
	public static class TestCacheLoader implements CacheLoader {
		
		private String cacheName;
		
		public TestCacheLoader(String cacheName) {
			this.cacheName = cacheName;
		}

		@Override
		public Object load(Object key) {
			return key + "@" + cacheName;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Map loadAll(Collection keys) {
			Map result = new HashMap();
			for(Object k: keys) {
				result.put(k, load(k));
			}
			return result;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static class TestBinaryEntryStore implements BinaryEntryStore {
		
		private String cacheName;
		private BackingMapManagerContext context;
		
		public TestBinaryEntryStore(String cacheName, BackingMapManagerContext context) {
			this.cacheName = cacheName;
			this.context = context;
		}

		public Object load(Object key) {
			return key + "@" + cacheName;
		}

		@Override
		public void load(BinaryEntry entry) {
			Object key = context.getKeyFromInternalConverter().convert(entry.getBinaryKey());
			Object val = load(key);
			entry.updateBinaryValue((Binary) context.getValueToInternalConverter().convert(val));
		}

		@Override
		public void loadAll(Set entries) {
			for(Object e: entries) {
				load((BinaryEntry)e);
			}
		}

		@Override
		public void store(BinaryEntry arg0) {
		}

		@Override
		public void storeAll(Set arg0) {
		}

		@Override
		public void erase(BinaryEntry arg0) {
		}

		@Override
		public void eraseAll(Set arg0) {
		}
	}	
}
