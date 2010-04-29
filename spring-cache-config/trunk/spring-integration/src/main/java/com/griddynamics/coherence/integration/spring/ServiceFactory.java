package com.griddynamics.coherence.integration.spring;

import java.util.List;

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
	private String serializerClass;
	private List<Object> serializerInitParams;
	
	public Service getObject() throws Exception {
		Cluster cluster = com.tangosol.net.CacheFactory.ensureCluster();
		Service service = cluster.ensureService(serviceName, serviceType.toString());
		service.configure(generateServiceDescription());
		return service;
	}
	
	protected XmlElement generateServiceDescription() {
		XmlElement config = CacheFactory.getServiceConfig(serviceType.toString());
		
		if (serializerClass != null) {
			XmlElement serializerEl = config.ensureElement("serializer");
			serializerEl.addElement("class-name").setString(serializerClass);
			
			if (!serializerInitParams.isEmpty()) {
				XmlElement paramsEl = serializerEl.addElement("init-params");
				for (Object param : serializerInitParams) {
					XmlElement paramEl = paramsEl.addElement("init-param");
					paramEl.addElement("param-type").setString(param.getClass().getName());
					paramEl.addElement("param-value").setString(param.toString());
				}
			}
		}
		
		return config;
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
	
	public void setSerializerClass(String serializerClass) {
		this.serializerClass = serializerClass;
	}
	
	public void setSerializerInitParams(List<Object> serializerInitParams) {
		this.serializerInitParams = serializerInitParams;
	}
}
