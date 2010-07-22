package org.gridkit.coherence.integration.spring;

import java.util.Map;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.NamedCache;

public class ClusteredCacheDefinition implements InitializingBean, BeanNameAware, MapProvider {

	private String cacheName;
	
	private NamedCacheDecorator frontTier;
	private ClusteredCacheService clusteredService;
	private String backendBeanId;
	private Object backendBean;
	
	
	@Override
	public void setBeanName(String name) {
		this.cacheName = name;
	}
	
	public void setFrontTier(NamedCacheDecorator frontTier) {
		this.frontTier = frontTier;
	}
	
	@Required
	public void setService(ClusteredCacheService service) {
		this.clusteredService = service;
	}
	
	@Required
	public void setBackTier(Object back) {
		if (back instanceof String) {
			backendBeanId = (String) back;
		}
		else {
			backendBean = (Map<?, ?>) back;
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO validation
		if (backendBean != null) {
			validateBackendBean();
		}
	}

	@SuppressWarnings("unchecked")
	private void validateBackendBean() {
		if (backendBean instanceof Map) {
			return;
		}
		if (backendBean instanceof MapProvider) {
			return;
		}
		if (backendBean instanceof BackningMapProvider) {
			return;
		}
		throw new IllegalArgumentException("Invalid type for backendBean " + backendBean.getClass().getName() + ", should be Map, MapProvider or BackingMapProvider");
	}

	@Override
	public Map<?, ?> getMap() {
		return getCache();
	}

	public NamedCache getCache() {
		try {
			// TODO JMX monitoring support
			NamedCache cache = clusteredService.ensureCache(cacheName);
			if (frontTier != null) {
				cache = frontTier.wrapCache(cache);
			}
			return cache;
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	public Map<?, ?> getBackendInstance(ApplicationContext appCtx, BackingMapManagerContext cacheCtx) {
		if (backendBean != null) {
			return resolveMap(backendBean, cacheCtx);
		}
		else {
			return resolveMap(appCtx.getBean(backendBeanId), cacheCtx);
		}
	}

	private Map<?, ?> resolveMap(Object bean, BackingMapManagerContext cacheCtx) {
		return BackningMapProvider.Helper.getMapFromBean(bean, cacheCtx, Map.class);
	}
}
