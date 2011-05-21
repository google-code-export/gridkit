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
package org.gridkit.coherence.util.arbiter;

/**
 * Configurable fair share implementation.
 * Number of peers to share resources among is configured manually.
 * 
 * @author malekseev
 * 20.04.2011
 */
public class StaticFairShare implements FairShareCalculator {
	
	private int peerCount;
	
	/**
	 * Constructs StaticFairShare
	 * @param peerCount number of peers to share resources among
	 */
	public void setPeerCount(int peerCount) {
		this.peerCount = peerCount;
	}
	
	@Override
	public int getFairShare(int sourcesSize) {
		return (int) Math.ceil(((double) sourcesSize) / (peerCount - 1));
	}
	
}
