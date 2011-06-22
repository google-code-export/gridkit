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
 * This is a static factory for various {@link SchedulingPolicy}(ies).
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class SchedulingPolicies {

	/**
	 * Creates simple periodic scheduling policy.
	 */
	public static SchedulingPolicy newPeriodicTimeTablePolicy(long interval, TimeUnit tu) {
		return new PeriodicTimeTablePolicy(interval, tu);
	}
	
	
	private static class PeriodicTimeTablePolicy implements SchedulingPolicy {

		TimeUnit tu;
		long period;
		long anchorTime = -1;
		
		public PeriodicTimeTablePolicy(long period, TimeUnit tu) {
			this.period = period;
			this.tu = tu;
		}

		@Override
		public long getTimeForNextSchedule(TimeUnit tu) {
			if (anchorTime == -1) {
				anchorTime = currentTime(); // very is a small chance to hit concurrent data corruption on 32 bit architecture here 
			}
			long since = currentTime() - anchorTime;
			since %= period;

			// if task execution time close to scheduling period, it may miss its next time table slot
			
			return period - since;
		}

		private long currentTime() {
			if (tu == TimeUnit.NANOSECONDS) {
				return System.nanoTime();
			}
			else {
				return TimeUnit.MILLISECONDS.convert(System.currentTimeMillis(), tu);
			}
		}
		
		@Override
		public void taskStarted() {
		}

		@Override
		public void taskFinished() {
		}
	}
}
