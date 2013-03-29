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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.gridkit.coherence.chtest.CacheConfig.CacheScheme;
import org.gridkit.coherence.chtest.CacheConfig.ServiceScheme;
import org.gridkit.coherence.chtest.CohCloud.CohNode;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViProps;
import org.gridkit.vicluster.isolate.IsolateProps;
import org.gridkit.zerormi.util.RemoteExporter;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheService;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;

class NodeWrapper extends ViNode.Delegate implements CohNode {
	
	public NodeWrapper(ViNode delegate) {
		super(delegate);
	}

	@Override
	public CohNode cacheConfig(String file) {
		CohHelper.cacheConfig(getDelegate(), file);
		return this;
	}
	
	@Override
	public CohNode useEmptyCacheConfig() {
		return cacheConfig("empty-cache-config.xml");
	}

	@Override
	public CohNode mapCache(String cachePattern, String schemeName) {
		CohHelper.applyCacheConfigFragment(getDelegate(), CacheConfig.mapCache(cachePattern, schemeName));
		return this;
	}

	@Override
	public CohNode mapCache(String cachePattern, CacheScheme scheme) {
		CohHelper.applyCacheConfigFragment(getDelegate(), CacheConfig.mapCache(cachePattern, scheme));
		return this;
	}

	@Override
	public CohNode addScheme(CacheScheme scheme) {
		CohHelper.applyCacheConfigFragment(getDelegate(), CacheConfig.addScheme(scheme));
		return this;
	}

	@Override
	public CohNode addScheme(ServiceScheme scheme) {
		CohHelper.applyCacheConfigFragment(getDelegate(), CacheConfig.addScheme(scheme));
		return this;
	}

	@Override
	public CohNode pofConfig(String file) {
		CohHelper.pofConfig(getDelegate(), file);
		return this;
	}

	@Override
	public CohNode pofEnabled(boolean enabled) {
		CohHelper.pofEnabled(getDelegate(), enabled);
		return this;
	}

	@Override
	public CohNode localStorage(boolean enabled) {
		CohHelper.localstorage(getDelegate(), enabled);
		return this;
	}

	@Override
	public CohNode presetFastLocalCluster() {
		CohHelper.enableFastLocalCluster(getDelegate());
		CohHelper.setJoinTimeout(getDelegate(), 50);
		CohHelper.enableTcpRing(getDelegate(), false);
		return this;
	}
	
	@Override
	public CohNode enableTcpRing(boolean enable) {
		CohHelper.enableTcpRing(getDelegate(), enable);
		return this;
	}
	
	@Override
	public CohNode enableTCMP(boolean enable) {
		CohHelper.enableTCMP(this, enable);
		return this;
	}

	@Override
	public CohNode enableJmx(boolean enable) {
		CohHelper.enableJmx(this, enable);
		return this;
	}

	@Override
	public CohNode outOfProcess(boolean oop) {
		if (oop) {
			ViProps.at(this).setLocalType();
		}
		else {
			ViProps.at(this).setIsolateType();
		}
		return this;
	}

	@Override
	public CohNode setTCMPTimeout(long timeoutMs) {
		CohHelper.setTCMPTimeout(this, timeoutMs);
		return this;
	}

	@Override
	public CohNode useCoherenceVersion(String version) {
		CohHelper.configureCoherenceVersion(this, version);
		return this;
	}

	@Override
	public CohNode autoStartCluster() {
		Runnable starter = new Runnable() {
			@Override
			public void run() {
				CacheFactory.ensureCluster();
			}
		};
		addStartupHook("coherence-cluster-autostart", starter, false);
		return this;		
	}
	
	@Override
	public CohNode autoStartServices() {
		Runnable starter = new Runnable() {
			@Override
			public void run() {
				DefaultCacheServer.start();
			}
		};
		addStartupHook("coherence-service-autostart", starter, false);
		return this;
	}
	
	@Override
	public CohNode gracefulShutdown(boolean graceful) {
		if (graceful) {
			addShutdownHook("coherence-graceful-shutdown", new ClusterShutdown(graceful), true);
		}
		return this;
	}

	@Override
	public void startCacheServer() {
		exec(new Runnable() {
			@Override
			public void run() {
				DefaultCacheServer.start();
			}
		});
	}

	@Override
	public void ensureCluster() {
		exec(new Runnable() {
			@Override
			public void run() {
				CacheFactory.ensureCluster();
			}
		});
	}

	@Override
	public void shutdownCluster() {
		exec(new Runnable() {
			@Override
			public void run() {
				CacheFactory.shutdown();
			}
		});		
	}

	@Override
	public NamedCache getCache(final String cacheName) {
		return exec(new Callable<NamedCache>() {
			@Override
			public NamedCache call() throws Exception {
				NamedCache cache = CacheFactory.getCache(cacheName);
				return RemoteExporter.export(cache, NamedCache.class);
			}
		});
	}

	@Override
	public String getServiceNameForCache(final String string) {
		return exec(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return CacheFactory.getCache(string).getCacheService().getInfo().getServiceName();
			}
		});
	}

	@Override
	public Service ensureService(final String serviceName) {
		return exec(new Callable<Service>() {
			@Override
			public Service call() throws Exception {
				List<Class<?>> list = new ArrayList<Class<?>>();
				list.add(Service.class);
				Service service = CacheFactory.getService(serviceName);
				if (service instanceof CacheService) {
					list.add(CacheService.class);
				}
				if (service instanceof DistributedCacheService) {
					list.add(DistributedCacheService.class);
				}
				if (service instanceof InvocationService) {
					list.add(InvocationService.class);					
				}
				return (Service)RemoteExporter.export(service, list);
			}
		});
	}
	
	@Override
	public CohNode shareClass(Class<?> type) {
		IsolateProps.at(this).shareClass(type);
		return this;
	}

	@Override
	public CohNode shareClass(String className) {
		IsolateProps.at(this).shareClass(className);
		return this;
	}

	@Override
	public CohNode sharePackage(String packageName) {
		IsolateProps.at(this).sharePackage(packageName);
		return this;
	}

	@Override
	public void dumpCacheConfig() {
		getDelegate().exec(new Runnable() {
			@Override
			public void run() {
				System.out.println(CacheFactory.getConfigurableCacheFactory().getConfig());
			}
		});		
	}

	@SuppressWarnings("serial")
	private static class ClusterShutdown implements Runnable, Serializable {

		final boolean graceful;
		
		public ClusterShutdown(boolean graceful) {
			this.graceful = graceful;
		}

		@Override
		public void run() {
			if (graceful) {
				System.err.println("Coherence node shutdown");
				CacheFactory.shutdown();
			}
		}
	}
}