/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gridkit.coherence.integration.spring.service;

import com.tangosol.net.partition.KeyAssociator;
import com.tangosol.net.partition.KeyPartitioningStrategy;
import com.tangosol.net.partition.PartitionListener;

/**
 * @author Dmitri Babaev
 */
public class DistributedCacheServiceConfiguration extends CacheServiceConfiguration {
	
	@XmlConfigProperty("thread-count")
	protected Integer threadCount;
	
	@XmlConfigProperty("partition-count")
	protected Integer partitionCount;
	
	@XmlConfigProperty("backup-count")
	protected Integer backupCount;
	
	@XmlConfigProperty("backup-count-after-writebehind")
	protected Integer backupCountAfterWritebehind;
	
	@ReflectionInjectedProperty("__m_KeyAssociator")
	protected KeyAssociator keyAssociator;
	
	@ReflectionInjectedProperty("__m_KeyPartitioningStrategy")
	protected KeyPartitioningStrategy keyPartitioningStrategy;
	
	@ReflectionInjectedProperty("__m_PartitionListener")
	protected PartitionListener partitionListener;
	
	@XmlConfigProperty("transfer-threshold")
	protected Integer TransferThresholdKbytes;
	
	@XmlConfigProperty("local-storage")
	protected Boolean localStorage;
	
	@XmlConfigProperty("task-hung-threshold")
	protected Integer taskHungThresholdMillis;
	
	@XmlConfigProperty("task-timeout")
	protected Integer taskTimeoutMillis;
	
	@XmlConfigProperty("request-timeout")
	protected Integer requestTimeoutMillis;

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
