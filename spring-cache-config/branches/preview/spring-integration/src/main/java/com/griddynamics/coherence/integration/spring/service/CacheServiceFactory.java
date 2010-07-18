package com.griddynamics.coherence.integration.spring.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.griddynamics.coherence.integration.spring.ContextBackingMapManager;
import com.tangosol.net.CacheService;

/**
 * @author Dmitri Babaev
 */
public abstract class CacheServiceFactory extends ServiceFactory implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	public CacheService getService() {
		CacheService service = (CacheService)super.getService();
		service.setBackingMapManager(new ContextBackingMapManager(applicationContext));
		return service;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public Class<?> getObjectType() {
		return CacheService.class;
	}
}
