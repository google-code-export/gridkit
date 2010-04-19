package com.griddynamics.coherence.integration.spring.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.tangosol.io.Serializer;

/**
 * @author Dmitri Babaev
 */
@XmlType(name="distributed-scheme")
public class DistributedScheme extends NearScheme {
	private String serviceName;
	private Serializer serializer;
	
	public DistributedScheme() {
	}
	
	@XmlElement(name="service-name")
	public String getServiceName() {
		return serviceName;
	}
	
	@XmlElement(name="serializer")
	public ContextBean getSerializerBean() {
		return new ContextBean("");
	}
}
