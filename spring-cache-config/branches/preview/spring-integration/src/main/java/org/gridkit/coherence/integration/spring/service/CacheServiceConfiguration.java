package org.gridkit.coherence.integration.spring.service;

public abstract class CacheServiceConfiguration extends GenericServiceConfiguration {

	@XmlConfigProperty("lease-granularity")
	protected LeaseGranularity leaseGranularity;

	public void setLeaseGranularity(LeaseGranularity leaseGranularity) {
		this.leaseGranularity = leaseGranularity;
	}
}
