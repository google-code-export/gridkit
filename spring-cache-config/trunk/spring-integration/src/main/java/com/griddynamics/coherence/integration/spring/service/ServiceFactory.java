package com.griddynamics.coherence.integration.spring.service;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Service;
import com.tangosol.run.xml.XmlElement;

/**
 * @author Dmitri Babaev
 */
public abstract class ServiceFactory implements FactoryBean<Service>, BeanNameAware, DisposableBean {
	private String serviceName;
	private Service service;
	
	private String serializerClass;
	private List<Object> serializerInitParams;
	
	public Service getObject() throws Exception {
		if (service == null) { 
			service = getService();
			
			if (!service.isRunning())
				service.start();
		}
		
		return service;
	}
	
	protected Service getService() {
		Cluster cluster = CacheFactory.ensureCluster();
		Service service = cluster.ensureService(serviceName, getServiceType().toString());
		service.configure(generateServiceDescription());
		
		return service;
	}

	public void destroy() throws Exception {
		service.shutdown();
	}

	public abstract ServiceType getServiceType();

	protected XmlElement generateServiceDescription() {
		XmlElement config = CacheFactory.getServiceConfig(getServiceType().toString());
		
		overrideServiceProperties(config);
		
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
		return true;
	}
	
	public void setBeanName(String name) {
		serviceName = name;
	}
	
	public void setSerializerClass(String serializerClass) {
		this.serializerClass = serializerClass;
	}
	
	public void setSerializerInitParams(List<Object> serializerInitParams) {
		this.serializerInitParams = serializerInitParams;
	}
	
	private void overrideServiceProperties(XmlElement config) {
		for (Field field : this.getClass().getDeclaredFields()) {
			ServiceProperty sp = field.getAnnotation(ServiceProperty.class);
			if (sp == null) continue;
			
			Object val = null;
			try {
				val = field.get(this);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			
			if (val == null) continue;
			
			config.ensureElement(sp.value()).setString(val.toString());
		}
	}
}
