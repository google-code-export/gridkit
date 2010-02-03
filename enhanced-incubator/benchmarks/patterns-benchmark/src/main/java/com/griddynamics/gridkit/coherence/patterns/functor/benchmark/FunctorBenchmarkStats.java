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
package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

public class FunctorBenchmarkStats
{
	public double totalTime;
	public double throughput;
		
	public double averageSumbitLatency;
	public double sumbitLatencyVariance;
		
	public double maxSumbitLatency;
	public double minSumbitLatency;
	
	public double averageReturnLatency;
	public double returnLatencyVariance;
		
	public double maxReturnLatency;
	public double minReturnLatency;
	
	@Override
	public String toString()
	{
		return "FunctorBenchmarkStats [averageReturnLatency="
				+ averageReturnLatency + ", averageSumbitLatency="
				+ averageSumbitLatency + ", maxReturnLatency="
				+ maxReturnLatency + ", maxSumbitLatency=" + maxSumbitLatency
				+ ", minReturnLatency=" + minReturnLatency
				+ ", minSumbitLatency=" + minSumbitLatency
				+ ", returnLatencyVariance=" + returnLatencyVariance
				+ ", sumbitLatencyVariance=" + sumbitLatencyVariance
				+ ", throughput=" + throughput + ", totalTime=" + totalTime
				+ "]";
	}
}
