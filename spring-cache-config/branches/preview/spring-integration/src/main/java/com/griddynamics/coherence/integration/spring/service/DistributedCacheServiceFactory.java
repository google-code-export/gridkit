package com.griddynamics.coherence.integration.spring.service;

/**
 * @author Dmitri Babaev
 */
public class DistributedCacheServiceFactory extends CacheServiceFactory {
	@ServiceProperty("thread-count")
	protected Integer threadCount;
	
	@ServiceProperty("partition-count")
	protected Integer partitionCount;
	
	@ServiceProperty("backup-count")
	protected Integer backupCount;
	
	@ServiceProperty("backup-count-after-writebehind")
	protected Integer backupCountAfterWritebehind;
	
	@ServiceProperty("key-associator")
	protected String keyAssociatorClass;
	
	@ServiceProperty("key-partitioning")
	protected String keyPartitioningClass;
	
	@ServiceProperty("partition-listener")
	protected String partitionListenerClass;
	
	@ServiceProperty("lease-granularity")
	protected LeaseGranularity leaseGranularity;
	
	@ServiceProperty("transfer-threshold")
	protected Integer TransferThresholdKbytes;
	
	@ServiceProperty("local-storage")
	protected Boolean localStorage;
	
	@ServiceProperty("task-hung-threshold")
	protected Integer taskHungThresholdMillis;
	
	@ServiceProperty("task-timeout")
	protected Integer taskTimeoutMillis;
	
	@ServiceProperty("request-timeout")
	protected Integer requestTimeoutMillis;

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
	
	public void setPartitionCount(Integer partitionCount) {
		this.partitionCount = partitionCount;
	}
	
	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}

	public void setBackupCount(Integer backupCount) {
		this.backupCount = backupCount;
	}

	public void setBackupCountAfterWritebehind(Integer backupCountAfterWritebehind) {
		this.backupCountAfterWritebehind = backupCountAfterWritebehind;
	}

	public void setKeyAssociatorClass(String keyAssociatorClass) {
		this.keyAssociatorClass = keyAssociatorClass;
	}

	public void setKeyPartitioningClass(String keyPartitioningClass) {
		this.keyPartitioningClass = keyPartitioningClass;
	}

	public void setPartitionListenerClass(String partitionListenerClass) {
		this.partitionListenerClass = partitionListenerClass;
	}

	public void setLeaseGranularity(LeaseGranularity leaseGranularity) {
		this.leaseGranularity = leaseGranularity;
	}

	public void setTransferThresholdKbytes(Integer transferThresholdKbytes) {
		TransferThresholdKbytes = transferThresholdKbytes;
	}

	public void setLocalStorage(Boolean localStorage) {
		this.localStorage = localStorage;
	}

	public void setTaskHungThresholdMillis(Integer taskHungThresholdMillis) {
		this.taskHungThresholdMillis = taskHungThresholdMillis;
	}

	public void setTaskTimeoutMillis(Integer taskTimeoutMillis) {
		this.taskTimeoutMillis = taskTimeoutMillis;
	}

	public void setRequestTimeoutMillis(Integer requestTimeoutMillis) {
		this.requestTimeoutMillis = requestTimeoutMillis;
	}

	public ServiceType getServiceType() {
		return ServiceType.DistributedCache;
	}
}
