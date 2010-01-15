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

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StatHelper
{
	static interface TimeExtractor
	{
		long getSubmitTime(ExecMark ts);
		long getExecTime(ExecMark ts);
		
		double getScale();
	}
	
	static final class JavaMsExtractor implements TimeExtractor
	{
		public long getExecTime(ExecMark ts)   { return ts.execTS.getJavaMs(); }
		public long getSubmitTime(ExecMark ts) { return ts.submitTS.getJavaMs(); }
		public double getScale()               { return 1d; }
	}
	
	static final class CoherenceMsExtractor implements TimeExtractor
	{
		public long getExecTime(ExecMark ts)   { return ts.execTS.getCoherenceMs(); }
		public long getSubmitTime(ExecMark ts) { return ts.submitTS.getCoherenceMs(); }
		public double getScale()               { return 1d; }
	}
	
	static final class JavaNsExtractor implements TimeExtractor
	{
		public long getExecTime(ExecMark ts)   { return ts.execTS.getJavaNs(); }
		public long getSubmitTime(ExecMark ts) { return ts.submitTS.getJavaNs(); }
		public double getScale()               { return 1d / TimeUnit.MILLISECONDS.toNanos(1); }
	}

	public static BenchmarkStats calculateStat(Map<Long, ExecMark> stats, TimeExtractor extractor)
	{
		if (stats == null || stats.isEmpty()) {
			TestHelper.sysout("No data");
		}
		
		long firstTime = extractor.getSubmitTime(stats.values().iterator().next());
		long lastTime  = extractor.getExecTime(stats.values().iterator().next());

		long cummulativeTime = 0;
		double cummulativeSquareDev = 0;
		long eventCount = 0;
		long avgLatency = lastTime - firstTime;
		long maxLatency = avgLatency;
		long minLatency = avgLatency;

		for (ExecMark execMark : stats.values()) {

			eventCount++;
			
			long submitTime = extractor.getSubmitTime(execMark);
			long execTime = extractor.getExecTime(execMark);
			
			if (lastTime < execTime) {
				lastTime = execTime;
			}
			
			if (firstTime > submitTime) {
				firstTime = submitTime;
			}
			
			long latency = execTime - submitTime;

			// Calc total time out
			cummulativeTime += latency;
			cummulativeSquareDev += (latency - (cummulativeTime / eventCount)) * (latency - (cummulativeTime / eventCount));

			// Get max timeout
			if (maxLatency < latency) {
				maxLatency = latency;
			}

			// Get min timeout
			if (minLatency > latency) {
				minLatency = latency;
			}
		}

		// Calc avg
		avgLatency = cummulativeTime / eventCount;
		// Calc variance
		long cummulativeVariance = 0;
		for (ExecMark execMark : stats.values()) {

			long submitTime = extractor.getSubmitTime(execMark);
			long execTime = extractor.getExecTime(execMark);
			long latency = execTime - submitTime;

			long var = Math.abs(avgLatency - latency);
			cummulativeVariance += var;
		}
		
		long avgVariance = cummulativeVariance / eventCount;
		
		double scale = extractor.getScale(); 
		
		//Calc standard deviation
		//final double stdDev = Math.sqrt(Math.abs(cummulativeSquareDev));

		BenchmarkStats benchmarkStats = new BenchmarkStats(
			(scale * (lastTime - firstTime)) / 1000, //Total Time
			(eventCount * 1000) / (scale * (lastTime - firstTime)), //Throughput
			scale * avgLatency, //Average Latency
			scale * avgVariance, //Latency Variance
			scale * maxLatency, //Max Latency
			scale * minLatency //Min Latency
		);
		
		return benchmarkStats;
	}
	
	public static BenchmarkStats calculateStatJavaMs(Map<Long, ExecMark> stats)
	{
		return calculateStat(stats, new JavaMsExtractor());
	}
	
	public static void reportStatsJavaMs(Map<Long, ExecMark> stats) 
	{
		reportStats(calculateStatJavaMs(stats));
	}
	
	public static BenchmarkStats calculateStatJavaNs(Map<Long, ExecMark> stats)
	{
		return calculateStat(stats, new JavaNsExtractor());
	}

	public static void reportStatsJavaNs(Map<Long, ExecMark> stats)
	{
		reportStats(calculateStatJavaNs(stats));
	}
	
	public static BenchmarkStats calculateStatCoherenceMs(Map<Long, ExecMark> stats)
	{
		return calculateStat(stats, new CoherenceMsExtractor());
	}
	
	public static void reportStatsCoherenceMs(Map<Long, ExecMark> stats)
	{
		reportStats(calculateStatCoherenceMs(stats));
	}
	
	public static void reportStats(BenchmarkStats benchmarkStats) 
	{
		TestHelper.sysout("Total time [s]:        %014.12f" , benchmarkStats.getTotalTime());
		TestHelper.sysout("Throughput [op/s]:     %014.12f" , benchmarkStats.getThroughput());
		TestHelper.sysout("Average latency [ms]:  %014.12f" , benchmarkStats.getAverageLatency());
		TestHelper.sysout("Latency variance [ms]: %014.12f" , benchmarkStats.getLatencyVariance());
		//TestHelper.sysout("Latency stddev /[ms]:   %014.12f" , scale * stdDev);
		TestHelper.sysout("Max latency [ms]:      %014.12f" , benchmarkStats.getMaxLatency());
		TestHelper.sysout("Min latency [ms]:      %014.12f" , benchmarkStats.getMinLatency());
	}
}
