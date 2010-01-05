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

public class StatHelper {

	public static void reportStatsMs(Map<Long, ExecMark> stats) {
		if (stats == null || stats.isEmpty()) {
			System.out.println("No data");
		}
		
		long firstTime = stats.values().iterator().next().submitMs;
		long lastTime = stats.values().iterator().next().execMs;

		long cummulativeTime = 0;
		long cummulativeSquareTime = 0;
		long eventCount = 0;
		long avgLatency = lastTime - firstTime;
		long maxLatency = avgLatency;
		long minLatency = avgLatency;

		for (ExecMark execMark : stats.values()) {

			eventCount++;
			
			long submitTime = execMark.submitMs;
			long execTime = execMark.execMs;
			
			if (lastTime < execTime) {
				lastTime = execTime;
			}
			
			if (firstTime > submitTime) {
				firstTime = submitTime;
			}
			
			long latency = execTime - submitTime;

			// Calc total time out
			cummulativeTime += latency;
			cummulativeSquareTime += latency * latency;

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

			long submitTime = execMark.submitMs;
			long execTime = execMark.execMs;
			long latency = execTime - submitTime;

			long var = Math.abs(avgLatency - latency);
			cummulativeVariance += var;
		}
		
		long avgVariance = cummulativeVariance / eventCount;
		
		double scale = 1d; 
		
		// Calc standard deviation
		final double stdDev = Math.sqrt(Math.abs(cummulativeSquareTime - cummulativeTime * cummulativeTime));

		System.out.println("Total time: " + (scale * (lastTime - firstTime)) / 1000 + "s");
		System.out.println("Throughput: " + (eventCount * 1000) / (scale * (lastTime - firstTime)) + "op/s");
		System.out.println("Average latency: " + scale * avgLatency + "ms");
		System.out.println("Latency variance: " + scale * avgVariance + "ms");
		System.out.println("Latency stddev: " + scale * stdDev + "ms");
		System.out.println("Max latency: " + scale * maxLatency + "ms");
		System.out.println("Min latency: " + scale * minLatency + "ms");
	}	
}
