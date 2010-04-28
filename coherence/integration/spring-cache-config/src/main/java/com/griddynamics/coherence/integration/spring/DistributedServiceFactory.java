package com.griddynamics.coherence.integration.spring;

import com.tangosol.run.xml.XmlElement;

/**
 * @author Dmitri Babaev
 */
public class DistributedServiceFactory extends CacheServiceFactory {
	private Integer threadCount;

	protected XmlElement generateServiceDescription() {
		XmlElement res = super.generateServiceDescription();
		
		if (threadCount != null)
			res.getElement("thread-count").setInt(threadCount);
		
		return res;
	}
	
	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
}
