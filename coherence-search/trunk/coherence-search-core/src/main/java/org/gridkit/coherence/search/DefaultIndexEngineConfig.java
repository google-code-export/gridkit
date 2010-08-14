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

package org.gridkit.coherence.search;

import java.io.Serializable;

/**
 * Default implementation of {@link IndexEngineConfig}. 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class DefaultIndexEngineConfig implements IndexEngineConfig, Serializable {

	private static final long serialVersionUID = 20100814L;
	
	private int indexUpdateQueueSizeLimit = 1024;
	private int indexUpdateDelay = 5000;
	private boolean attributeIndexEnabled = false;
	private boolean oldValueOnUpdateEnabled = false;
	
	public int getIndexUpdateQueueSizeLimit() {
		return indexUpdateQueueSizeLimit;
	}
	
	public void setIndexUpdateQueueSizeLimit(int indexUpdateQueueSizeLimit) {
		this.indexUpdateQueueSizeLimit = indexUpdateQueueSizeLimit;
	}
	
	public int getIndexUpdateDelay() {
		return indexUpdateDelay;
	}
	
	public void setIndexUpdateDelay(int indexUpdateDelay) {
		this.indexUpdateDelay = indexUpdateDelay;
	}
	
	public boolean isAttributeIndexEnabled() {
		return attributeIndexEnabled;
	}
	
	public void setAttributeIndexEnabled(boolean attributeIndexEnabled) {
		this.attributeIndexEnabled = attributeIndexEnabled;
	}
	
	public boolean isOldValueOnUpdateEnabled() {
		return oldValueOnUpdateEnabled;
	}

	public void setOldValueOnUpdateEnabled(boolean oldValueOnUpdateEnabled) {
		this.oldValueOnUpdateEnabled = oldValueOnUpdateEnabled;
	}
}
