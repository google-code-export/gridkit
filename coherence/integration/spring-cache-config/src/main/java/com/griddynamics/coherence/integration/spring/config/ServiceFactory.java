package com.griddynamics.coherence.integration.spring.config;

import org.springframework.beans.factory.FactoryBean;

import com.tangosol.net.Cluster;
import com.tangosol.net.Service;
import com.tangosol.run.xml.XmlElement;

/**
 * @author Dmitri Babaev
 */
public class ServiceFactory implements FactoryBean<Service> {
	private String serviceName;
	private String serviceType;
	
	public Service getObject() throws Exception {
		Cluster cluster = com.tangosol.net.CacheFactory.getCluster();
		Service service = cluster.ensureService(serviceName, serviceType);
		service.configure(generateServiceDescription());
		return service;
	}
	
	protected XmlElement generateServiceDescription() {
		throw new UnsupportedOperationException();
	}
	
	public Class<?> getObjectType() {
		return Service.class;
	}
	
	public boolean isSingleton() {
		return false;
	}
}
