package org.gridkit.util.coherence.cohtester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.gridkit.util.coherence.cohtester.CohCloud.CohNode;
import org.gridkit.vicluster.ViNode;
import org.gridkit.zerormi.util.DynamicExporter;

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
	public CohNode localStorage(boolean enabled) {
		CohHelper.localstorage(getDelegate(), enabled);
		return this;
	}

	@Override
	public CohNode enableFastLocalCluster() {
		CohHelper.enableFastLocalCluster(getDelegate());
		CohHelper.setJoinTimeout(getDelegate(), 100);
		CohHelper.disableTcpRing(getDelegate());
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
	public NamedCache getCache(final String cacheName) {
		return exec(new Callable<NamedCache>() {
			@Override
			public NamedCache call() throws Exception {
				NamedCache cache = CacheFactory.getCache(cacheName);
				return DynamicExporter.export(cache, NamedCache.class);
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
				return (Service)DynamicExporter.export(service, list);
			}
		});
	}
	
	
}