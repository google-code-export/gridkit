package com.griddynamics.coherence.integration.spring;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Service;
import com.tangosol.run.xml.XmlElement;

/**
 * @author Dmitri Babaev
 */
public class ServiceFactory implements FactoryBean<Service>, BeanNameAware {
	private String serviceName;
	private ServiceType serviceType;
	
	public Service getObject() throws Exception {
		Cluster cluster = com.tangosol.net.CacheFactory.ensureCluster();
		Service service = cluster.ensureService(serviceName, serviceType.toString());
		service.configure(generateServiceDescription());
		return service;
	}
	
	protected XmlElement generateServiceDescription() {
		return CacheFactory.getServiceConfig(serviceType.toString());
	}
	
	public Class<?> getObjectType() {
		return Service.class;
	}
	
	public boolean isSingleton() {
		return false;
	}
	
	public void setBeanName(String name) {
		serviceName = name;
	}
	
	@Required
	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}
}
