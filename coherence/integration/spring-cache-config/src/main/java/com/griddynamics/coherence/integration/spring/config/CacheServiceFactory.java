package com.griddynamics.coherence.integration.spring.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.tangosol.net.CacheService;

/**
 * @author Dmitri Babaev
 */
public class CacheServiceFactory extends ServiceFactory implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	public CacheService getObject() throws Exception {
		CacheService service = (CacheService)super.getObject();
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
