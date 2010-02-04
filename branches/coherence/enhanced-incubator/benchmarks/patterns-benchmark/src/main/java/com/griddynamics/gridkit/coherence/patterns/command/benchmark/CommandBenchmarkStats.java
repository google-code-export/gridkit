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
package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import com.griddynamics.gridkit.coherence.patterns.benchmark.csv.CSVHelper;

public class CommandBenchmarkStats
{
	public double totalTime;
		
	public double throughput;
		
	public double averageLatency;
	public double latencyVariance;
		
	public double maxLatency;
	public double minLatency;

	public String toCSVRow() 
	{
		return CSVHelper.formatDoubleToCSV(throughput) + ";" + 
			   CSVHelper.formatDoubleToCSV(totalTime) + ";" + 
			   CSVHelper.formatDoubleToCSV(averageLatency) + ";" +
			   CSVHelper.formatDoubleToCSV(latencyVariance) + ";" +
			   CSVHelper.formatDoubleToCSV(maxLatency) + ";" +
			   CSVHelper.formatDoubleToCSV(minLatency);
	}

	public static String getCSVHeader()
	{
		return "Throughput;TotalTime;AverageLatency;LatencyVariance;MaxLatency;MinLatency";
	}
		
	@Override
	public String toString()
	{
		return "BenchmarkStats [averageLatency=" + averageLatency
				+ ", latencyVariance=" + latencyVariance + ", maxLatency="
				+ maxLatency + ", minLatency=" + minLatency + ", throughput="
				+ throughput + ", totalTime=" + totalTime + "]";
	}
}
