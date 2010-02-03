/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
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
package com.griddynamics.gridkit.coherence.patterns.benchmark.stats;

import java.util.EnumMap;
import java.util.Map;

public class TimeDependStats<T>
{
	public static enum TimeMeasure {JavaMS, JavaNS, CoherenceMS};
	
	protected Map<TimeMeasure, T> results = new EnumMap<TimeMeasure, T>(TimeMeasure.class);
	
	public T getJavaMsStats()
	{
		return results.get(TimeMeasure.JavaMS);
	}
	
	public void setJavaMsStats(T javaMsStats)
	{
		results.put(TimeMeasure.JavaMS, javaMsStats);
	}
	
	public T getJavaNsStats()
	{
		return results.get(TimeMeasure.JavaNS);
	}
	
	public void setJavaNsStats(T javaNsStats)
	{
		results.put(TimeMeasure.JavaNS, javaNsStats);
	}
	
	public T getCoherenceMsStats()
	{
		return results.get(TimeMeasure.CoherenceMS);
	}
	
	public void setCoherenceMsStats(T coherenceMsStats)
	{
		results.put(TimeMeasure.CoherenceMS, coherenceMsStats);
	}
}
