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
package org.gridkit.coherence.txlite;

/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 * @deprecated class is for internal use, kept public to support POF deserialization 
 */
public class ReadCommitedCacheAccessAdapter extends BaseCacheAccessAdapter {

	private TxSuperviser superviser;
	
	public ReadCommitedCacheAccessAdapter(TxSuperviser superviser) {
		this.superviser = superviser;
	}
	
	@Override
	protected int getVersion() {
		return superviser.getLatestCommited();
	}

	@Override
	public void afterOperation(TxCacheWrapper wrapper) {
		// do nothing
	}

	@Override
	public void beforeOperation(TxCacheWrapper wrapper) {
		// do nothing
	}
}
