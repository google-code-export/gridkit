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

import java.util.concurrent.TimeUnit;

/**
 * This interfaces defines flexible scheduling policy. It may be as simple as "run in fixed interval" or much more complicated.

 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface SchedulingPolicy {
	
	public long getTimeForNextSchedule(TimeUnit tu);
	
	public void taskStarted();
	
	public void taskFinished();
	
}
