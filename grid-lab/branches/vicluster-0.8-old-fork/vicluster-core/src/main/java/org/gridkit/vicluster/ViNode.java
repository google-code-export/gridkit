/**
 * Copyright 2012 Alexey Ragozin
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
package org.gridkit.vicluster;

import java.util.Collection;
import java.util.Set;


/**
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 *
 */
public interface ViNode extends ViExecutor, ViUserPropEnabled, ViSysPropEnabled {

	public void label(String label);
	
	public Set<String> labels();
	
	/**
	 * If object is an aggregate will return list of actual node.
	 */
	public Collection<ViNode> unfold();
	
	/**
	 * Trigger cloud manager to start node initialization in background (if not initialized yet).
	 */
	public void touch();

	/**
	 * Wait until asynchronous node initialization is complete.
	 */	
	public void ensure();
	
	public void shutdown();
	
}
