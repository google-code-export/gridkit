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
package org.gridkit.drc.coherence;


/**
 * A base interface for managing HA connections to resources in cluster.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface ResourceHandler {

	/**
	 * Notifies {@link ResourceHandler} what this JVM has acquired a lock for particular resources 
	 * and should start activities associated with that resource.
	 */
	public void connect(Object resourceId);

	/**
	 * Notifies {@link ResourceHandler} what {@link DistributedResourceCoordinator} is going to release particular resource lock 
	 * and activities associated with resource should be stopped.
	 * This method should not return, until all related activities are finished.
	 * Once this call to this method have returned, {@link DistributedResourceCoordinator} will transfer lock for this resource to another JVM.
	 */
	public void disconnect(Object resourceId);
	
	/**
	 *  This method is called if {@link DistributedResourceCoordinator} has been detached from cluster and cannot perform graceful resource
	 *  withdrawal protocol.  
	 */
	public void terminate(Object resourceId);

}
