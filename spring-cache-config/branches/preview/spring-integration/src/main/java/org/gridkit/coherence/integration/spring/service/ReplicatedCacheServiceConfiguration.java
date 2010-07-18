package org.gridkit.coherence.integration.spring.service;

/**
 * @author Dmitri Babaev
 */
public class ReplicatedCacheServiceConfiguration extends CacheServiceConfiguration {
	
	@XmlConfigProperty("standard-lease-milliseconds")
	protected int standartLeaseMillis;
	
	
	@XmlConfigProperty("mobile-issues")
	protected boolean mobileIssues;

	public ServiceType getServiceType() {
		return ServiceType.ReplicatedCache;
	}
	
	public void setStandartLeaseMillis(Integer standartLeaseMillis) {
		this.standartLeaseMillis = standartLeaseMillis;
	}
	
	public void setMobileIssues(Boolean mobileIssues) {
		this.mobileIssues = mobileIssues;
	}
}
