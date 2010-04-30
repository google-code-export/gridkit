package com.griddynamics.coherence.integration.spring.service;

/**
 * @author Dmitri Babaev
 */
public class ReplicatedCacheServiceFactory extends CacheServiceFactory {
	@ServiceProperty("standard-lease-milliseconds")
	protected Integer standartLeaseMillis;
	
	@ServiceProperty("lease-granularity")
	protected LeaseGranularity leaseGranularity;
	
	@ServiceProperty("mobile-issues")
	protected Boolean mobileIssues;

	public ServiceType getServiceType() {
		return ServiceType.ReplicatedCache;
	}
	
	public void setLeaseGranularity(LeaseGranularity leaseGranularity) {
		this.leaseGranularity = leaseGranularity;
	}
	
	public void setStandartLeaseMillis(Integer standartLeaseMillis) {
		this.standartLeaseMillis = standartLeaseMillis;
	}
	
	public void setMobileIssues(Boolean mobileIssues) {
		this.mobileIssues = mobileIssues;
	}
}
