package org.gridkit.util.coherence.cohtester;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.gridkit.util.coherence.cohtester.CohCloud.CohNode;
import org.gridkit.vicluster.ViNode;
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
	public CohNode localStorage(boolean enabled) {
		CohHelper.localstorage(getDelegate(), enabled);
		return this;
	}

	@Override
	public CohNode enableFastLocalCluster() {
		CohHelper.enableFastLocalCluster(getDelegate());
		CohHelper.setJoinTimeout(getDelegate(), 50);
		CohHelper.disableTcpRing(getDelegate());
		return this;
	}
	
	@Override
	public CohNode disableTCMP() {
		CohHelper.disableTCMP(this);
		return this;
	}

	@Override
	public CohNode enableJmx() {
		CohHelper.enableJmx(this);
		return this;
	}

	@Override
	public CohNode setTCMPTimeout(long timeoutMs) {
		CohHelper.setTCMPTimeout(this, timeoutMs);
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