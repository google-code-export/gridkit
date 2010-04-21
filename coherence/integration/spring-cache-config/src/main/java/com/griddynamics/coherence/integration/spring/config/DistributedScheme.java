package com.griddynamics.coherence.integration.spring.config;

/**
 * @author Dmitri Babaev
 */
public class DistributedScheme extends CacheScheme {
	private String serializerId;
	private String backingMapId;
	private String listenerId;
	
	private Integer threadCount;
	
	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}
	
	public void setBackingMapId(String backingMapId) {
		this.backingMapId = backingMapId;
	}
	
	public void setListenerId(String listenerId) {
		this.listenerId = listenerId;
	}
	
	public void setSerializerId(String serializerId) {
		this.serializerId = serializerId;
	}
	
	public Integer getThreadCount() {
		return threadCount;
	}
	
	public String getBackingMapId() {
		return backingMapId;
	}
	
	public String getListenerId() {
		return listenerId;
	}
	
	public String getSerializerId() {
		return serializerId;
	}
}
