package com.griddynamics.coherence.integration.spring.config;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Dmitri Babaev
 */
public class ContextBean {
	public static final String SPRING_BEAN_PREFIX = "spring-bean:";
	
	private String id;
	
	public ContextBean(String id) {
		this.id = id;
	}
	
	@XmlElement(name="class-name")
	public String getClassName() {
		return SPRING_BEAN_PREFIX + id;
	}
}
