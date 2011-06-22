/**
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

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Periodic jobs are executed using {@link ScheduledThreadPoolExecutor}.
 * Only jobs for active (owned by local cluster node) resources are subject
 * for scheduling. 
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface ScheduledResourceJob {

	public Object getResourceId();

	public SchedulingPolicy getSchedulingPolicy();
	
	/**
	 * Notifies that job object is going to be scheduled. Job may choose to do some initialization. 
	 * This method is called from DRC control thread, so it should complete reasonably fast.
	 */
	public void connect();
	
	/**
	 * Invokes job in thread pool either by schedule (bySchedule == true) or right after connection (bySchedule = false).
	 */
	public void execute(boolean bySchedule);
	
	/**
	 * Notifies job object that current node has withdrawn ownership of resource.
	 * May be used to release expensive resources (e.g. database connections).
	 * This method is called from DRC control thread, so it should complete reasonably fast. 
	 */
	public void disconnect();
}
