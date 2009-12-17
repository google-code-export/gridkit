/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
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

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.CommandSubmitter;
import com.oracle.coherence.patterns.command.ContextsManager;
import com.oracle.coherence.patterns.command.DefaultCommandSubmitter;
import com.oracle.coherence.patterns.command.DefaultContextConfiguration;
import com.oracle.coherence.patterns.command.DefaultContextsManager;
import com.oracle.coherence.patterns.command.ContextConfiguration.ManagementStrategy;

/**
 * Main test. Submit commands from another threads.
 * 
 * @author akornev@griddynamics.com
 * @since 1.0
 */
public class RunCommandBenchmark {

	private static final int THREAD_COUNT = 5;
	private static final int COMMAND_COUNT = 100;
	private static final String THREAD_COUNT_PARAM = "thread.count";
	private static final String COMMAND_COUNT_PARAM = "command.count";

	/**
	 * Customize coherence and run benchmark
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Configure coherence
		System.setProperty("tangosol.pof.config", "pof-config.xml");
		System.setProperty("tangosol.coherence.cacheconfig",
				"coherence-commandpattern-cache-config.xml");

		// Get params from system properties
		int threadCount = 0;
		if (System.getProperty(THREAD_COUNT_PARAM) != null) {
			threadCount = Integer.parseInt(System
					.getProperty(THREAD_COUNT_PARAM));
		} else {
			threadCount = THREAD_COUNT;
		}

		int commandCount = 0;
		if (System.getProperty(COMMAND_COUNT_PARAM) != null) {
			commandCount = Integer.parseInt(System
					.getProperty(COMMAND_COUNT_PARAM));

		} else {
			commandCount = COMMAND_COUNT;
		}

		DefaultContextConfiguration contextConfiguration = new DefaultContextConfiguration(
				ManagementStrategy.DISTRIBUTED);

		ContextsManager contextsManager = DefaultContextsManager.getInstance();
		BenchmarkContext context = new BenchmarkContext();
		String contextName = "ContextNo" + System.currentTimeMillis();

		Identifier contextIdentifier = contextsManager.registerContext(
				contextName, context, contextConfiguration);

		CommandSubmitter commandSubmitter = DefaultCommandSubmitter
				.getInstance();

		Executor executor = Executors.newFixedThreadPool(threadCount);

		for (int i = 0; i < threadCount; i++) {
			executor.execute(new BenchmarkCommandSubmitter(commandSubmitter,
					contextIdentifier, commandCount));
		}

		// Sleep some time when command executing.
		List<BenchmarkCommandTime> commandTimes = null;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Get context
			BenchmarkContext con = (BenchmarkContext) contextsManager
					.getContext(contextIdentifier);

			commandTimes = con.getCommandTimes();
		} while (commandTimes.size() < (threadCount * commandCount));

		outStatistics(commandTimes);

	}

	/**
	 * Out command statistics.
	 * 
	 * @param commandTimes
	 */
	public static void outStatistics(
			final List<BenchmarkCommandTime> commandTimes) {

		if ((commandTimes != null) && (!commandTimes.isEmpty())) {
			// Get first result
			final long firstTime = commandTimes.get(0).getPushTime();
			// Get end result
			final long lastTime = commandTimes.get(commandTimes.size() - 1)
					.getEndTime();

			long totalTimeOut = 0;
			long avgTimeout = 0;
			long maxTimeout = 0;
			long minTimeout = Long.MAX_VALUE;

			for (BenchmarkCommandTime commandTime : commandTimes) {

				long timeout = (commandTime.getEndTime() - commandTime
						.getPushTime()) / 1000000;

				// Calc total time out
				totalTimeOut += timeout;

				// Get max timeout
				if (maxTimeout < timeout) {
					maxTimeout = timeout;
				}

				// Get min timeout
				if (minTimeout > timeout) {
					minTimeout = timeout;
				}

				System.out.println("Command with id = "
						+ commandTime.getCommandId() + " Work " + timeout);
			}

			// Calc avg
			avgTimeout = totalTimeOut / commandTimes.size();
			// Calc variance
			long variance = 0;
			for (BenchmarkCommandTime commandTime : commandTimes) {

				long timeout = (commandTime.getEndTime() - commandTime
						.getPushTime()) / 1000000;

				long var = (avgTimeout - timeout);
				var *= var;

				variance += var / commandTimes.size();
			}
			// Calc standard deviation
			final double stdDev = Math.sqrt(variance);

			System.out.println("For all queue time out "
					+ ((lastTime - firstTime) / 1000000));

			final double secondCount = ((double) (lastTime - firstTime)) / 1000000000;

			System.out.println("Throughput op/sec: "
					+ (commandTimes.size() / secondCount));

			System.out.println("AVG timeout: " + avgTimeout);

			System.out.println("Standard Deviation: " + stdDev);

			System.out.println("Max timeout: " + maxTimeout);

			System.out.println("Min timeout: " + minTimeout);
		}
	}
}
