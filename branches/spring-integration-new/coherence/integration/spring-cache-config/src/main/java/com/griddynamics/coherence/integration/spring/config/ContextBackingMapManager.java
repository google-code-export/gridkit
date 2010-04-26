package com.griddynamics.coherence.integration.spring.config;

import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.tangosol.net.AbstractBackingMapManager;

/**
 * @author Dmitri Babaev
 */
public class ContextBackingMapManager extends AbstractBackingMapManager {
	private ApplicationContext applicationContext;
	
	public ContextBackingMapManager(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public Map<?, ?> instantiateBackingMap(String paramString) {
		return null;
	}
}
