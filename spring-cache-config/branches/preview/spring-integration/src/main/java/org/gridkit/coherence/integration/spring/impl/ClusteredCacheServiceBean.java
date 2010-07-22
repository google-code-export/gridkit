package org.gridkit.coherence.integration.spring.impl;

import java.util.Map;
import java.util.concurrent.Callable;

import org.gridkit.coherence.integration.spring.ClusteredCacheDefinition;
import org.gridkit.coherence.integration.spring.ClusteredCacheService;
import org.gridkit.coherence.integration.spring.service.CacheServiceConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.tangosol.net.AbstractBackingMapManager;
import com.tangosol.net.BackingMapManager;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheService;
import com.tangosol.net.Cluster;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;

public class ClusteredCacheServiceBean implements ClusteredCacheService, InitializingBean, BeanNameAware, ApplicationContextAware, DisposableBean {

	private String serviceName;
	private String beanName;

	private CacheServiceConfiguration configuration;
	private boolean autostart = false;
	
	private ApplicationContext appContext;
	
	private CacheService service;
	
	private final BackingMapManager bmm = new BackendManager();
	
	private static ThreadUnlockHelper threadHelper = new ThreadUnlockHelper();
	
	public void setBeanName(String name) {
		serviceName = name;
	}		

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	@Required
	public void setConfiguration(CacheServiceConfiguration config) {
		this.configuration = config;
	}
	
	public void setAutostart(boolean autostart) {
		this.autostart = autostart;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.appContext = appContext;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (autostart) {
			ensureStarted();
		}
	}

	@Override
	public void destroy() throws Exception {
		// TODO destroying service
	}

	@Override
	public NamedCache ensureCache(final String name) {
		ensureStarted();
		return threadHelper.modalExecute(new Callable<NamedCache>() {
			@Override
			public NamedCache call() throws Exception {
				return service.ensureCache(name, null);
			}
		});
	}

	@Override
	public void destroyCahce(NamedCache cache) {
		service.destroyCache(cache);
		
	}

	public CacheService getCoherenceService() {
		ensureStarted();
		return service;
	}
	
	private synchronized void ensureStarted() {
		if (this.service == null) {
			serviceName = serviceName == null ? beanName : serviceName;
			final Cluster cluster = CacheFactory.ensureCluster();
			final Service service = cluster.ensureService(serviceName, configuration.getServiceType().toString());
			synchronized(cluster) {
				if (!service.isRunning()) {
					service.configure(configuration.getXmlConfiguration());
					configuration.postConfigure(service);						
					((CacheService)service).setBackingMapManager(bmm);					
				}
			}
			if (!service.isRunning()) {
				threadHelper.modalExecute(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						synchronized(cluster) {
							if (!service.isRunning()) {
								service.start();
							}
							return null;
						}   
					}
				});
			}
			if (((CacheService)service).getBackingMapManager() != bmm) {
				throw new IllegalArgumentException("Service name conflict. Service [" + serviceName + "] is owned by other service bean");
			}
			this.service = (CacheService) service;
		}
	}

	private class BackendManager extends AbstractBackingMapManager {

		@Override
		@SuppressWarnings("unchecked")
		public Map instantiateBackingMap(final String cacheName) {
			return threadHelper.safeExecute(new Callable<Map>() {
				@Override
				public Map call() throws Exception {
					ClusteredCacheDefinition cd = (ClusteredCacheDefinition) appContext.getBean(cacheName);
					return cd.getBackendInstance(appContext, getContext());
				}				
			});
		}
	}
}
