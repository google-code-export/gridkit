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

import com.tangosol.net.PartitionedService;

/**
 * DataLossListener for test purpose
 * 
 * @author malekseev
 * 06.04.2011
 */
public class DataLossListenerImpl implements DataLossListener {
	
	@Override
	public void onPartitionLost(PartitionedService partitionedService, int[] lostPartitions) {
		
		System.out.print("Test listener got lost partitions list:");
		for (int p : lostPartitions) System.out.print(" " + lostPartitions[p]);
		System.out.println();
		
		System.setProperty("ListenerTest.lossDetected", "true");
		
	}

}
