/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
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
package org.gridkit.coherence.util.dataloss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tangosol.net.partition.PartitionEvent;
import com.tangosol.net.partition.PartitionListener;

/**
 * Coherence partition listener for "canary" cache.
 * Configures and holds its own data loss monitor instance and submits consistency check events 
 * when partitions are moved or lost.
 * 
 * @author malekseev
 * 05.04.2011
 */
public class PartitionLossListener implements PartitionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(PartitionLossListener.class);
	
	public static final String CACHE_NAME = "canary-cache";
	
	private final DataLossMonitor dataLossMonitor;
	
	public PartitionLossListener(String dataLossListenerClass) {
		try {
			Class<?> listenerClass = Class.forName(dataLossListenerClass);
			if (!DataLossListener.class.isAssignableFrom(listenerClass)) {
				throw new IllegalArgumentException(
						"Listener class [" + 
						dataLossListenerClass + 
						"] does not implement " +
						DataLossListener.class.getCanonicalName());
			}
			this.dataLossMonitor = new DataLossMonitor(CACHE_NAME, (DataLossListener) listenerClass.newInstance());
		} catch (Exception e) {
			logger.error("Exception occured during partition listener initialization", e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Requests consistency check after particular transition events
	 */
	@Override
	public void onPartitionEvent(PartitionEvent partitionEvent) {
		final int eventType = partitionEvent.getId();
		
		switch (eventType) {
		case PartitionEvent.PARTITION_LOST: // the one we really need
		case PartitionEvent.PARTITION_TRANSMIT_ROLLBACK: // just in case
		case PartitionEvent.PARTITION_TRANSMIT_COMMIT: // just in case
			
			dataLossMonitor.checkConsistency();
		}
	}
	
}
