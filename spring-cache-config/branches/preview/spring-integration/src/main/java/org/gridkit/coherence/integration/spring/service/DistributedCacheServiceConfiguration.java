package org.gridkit.coherence.integration.spring.service;

import com.tangosol.net.partition.KeyAssociator;
import com.tangosol.net.partition.KeyPartitioningStrategy;
import com.tangosol.net.partition.PartitionListener;

/**
 * @author Dmitri Babaev
 */
public class DistributedCacheServiceConfiguration extends CacheServiceConfiguration {
	
	@XmlConfigProperty("thread-count")
	protected int threadCount;
	
	@XmlConfigProperty("partition-count")
	protected int partitionCount;
	
	@XmlConfigProperty("backup-count")
	protected int backupCount;
	
	@XmlConfigProperty("backup-count-after-writebehind")
	protected int backupCountAfterWritebehind;
	
	@ReflectionInjectedProperty("__m_KeyAssociator")
	protected KeyAssociator keyAssociator;
	
	@ReflectionInjectedProperty("__m_KeyPartitioningStrategy")
	protected KeyPartitioningStrategy keyPartitioningStrategy;
	
	@ReflectionInjectedProperty("__m_PartitionListener")
	protected PartitionListener partitionListener;
	
	@XmlConfigProperty("transfer-threshold")
	protected int TransferThresholdKbytes;
	
	@XmlConfigProperty("local-storage")
	protected boolean localStorage;
	
	@XmlConfigProperty("task-hung-threshold")
	protected int taskHungThresholdMillis;
	
	@XmlConfigProperty("task-timeout")
	protected int taskTimeoutMillis;
	
	@XmlConfigProperty("request-timeout")
	protected int requestTimeoutMillis;

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}
	
	public void setPartitionCount(int partitionCount) {
		this.partitionCount = partitionCount;
	}
	
	public void setBackupCount(int backupCount) {
		this.backupCount = backupCount;
	}

	public void setBackupCountAfterWritebehind(int backupCountAfterWritebehind) {
		this.backupCountAfterWritebehind = backupCountAfterWritebehind;
	}

	public void setKeyAssociator(KeyAssociator keyAssociator) {
		this.keyAssociator = keyAssociator;
	}

	public void setKeyPartitioningClass(KeyPartitioningStrategy keyPartitioningStrategy) {
		this.keyPartitioningStrategy = keyPartitioningStrategy;
	}

	public void setPartitionListener(PartitionListener partitionListener) {
		this.partitionListener = partitionListener;		
	}
	
	public void setTransferThresholdKbytes(int transferThresholdKbytes) {
		TransferThresholdKbytes = transferThresholdKbytes;
	}

	public void setLocalStorage(boolean localStorage) {
		this.localStorage = localStorage;
	}

	public void setTaskHungThresholdMillis(int taskHungThresholdMillis) {
		this.taskHungThresholdMillis = taskHungThresholdMillis;
	}

	public void setTaskTimeoutMillis(int taskTimeoutMillis) {
		this.taskTimeoutMillis = taskTimeoutMillis;
	}

	public void setRequestTimeoutMillis(int requestTimeoutMillis) {
		this.requestTimeoutMillis = requestTimeoutMillis;
	}

	public ServiceType getServiceType() {
		return ServiceType.DistributedCache;
	}
}
