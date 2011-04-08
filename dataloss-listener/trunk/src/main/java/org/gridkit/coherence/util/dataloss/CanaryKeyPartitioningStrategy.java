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

import com.tangosol.net.partition.DefaultKeyPartitioningStrategy;

/**
 * Coherence key partitioning strategy for "canary" cache.
 * Distributes cache keys among all scheme partitions.
 * 
 * @author malekseev
 * 05.04.2011
 */
public class CanaryKeyPartitioningStrategy extends DefaultKeyPartitioningStrategy {

	/**
	 * Returns partition number equal to the key value, assuming that key in an Integer value.
	 * Key association logic is skipped for this strategy, since no association may be applied
	 * to "canary" cache.
	 */
	@Override
	public int getKeyPartition(Object oKey) {
		int part = ((Integer) oKey).intValue(); 
		if (part < m_service.getPartitionCount()) {
			return part;
		} else {
			throw new IllegalArgumentException(
					"Trying to store partition key with value greater or equal than partitions count");
		}
	}

}
