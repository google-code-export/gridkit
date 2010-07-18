package org.gridkit.coherence.integration.spring.service;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Service;

/**
 * @author Dmitri Babaev
 */
public abstract class ServiceFactory implements BeanNameAware, DisposableBean {
	
	private String serviceName;
	private String beanName;
	private ServiceConfiguration config;
	
	private Service service;
	
	
	public synchronized Service getObject() throws Exception {
		if (service == null) { 
			service = getService();
			
			if (!service.isRunning())
				service.start();
		}
		
		return service;
	}

	public void setBeanName(String name) {
		serviceName = name;
	}		

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setConfig(ServiceConfiguration config) {
		this.config = config;
	}

	protected Service getService() {
		serviceName = serviceName == null ? beanName : serviceName;
		Cluster cluster = CacheFactory.ensureCluster();
		Service service = cluster.ensureService(serviceName, getServiceType().toString());
		synchronized(cluster) {
			if (!service.isRunning()) {
				service.configure(config.getXmlConfiguration());
				config.postConfigure(service);		
			}
		}
		return service;
	}

	public void destroy() throws Exception {
		service.shutdown();
	}

	public abstract ServiceType getServiceType();
	
	public Class<?> getObjectType() {
		return Service.class;
	}
	
	public boolean isSingleton() {
		return true;
	}	
}
