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

		DefaultContextConfiguration contextConfiguration = new DefaultContextConfiguration(
				ManagementStrategy.DISTRIBUTED);

		ContextsManager contextsManager = DefaultContextsManager.getInstance();
		BenchmarkContext context = new BenchmarkContext();
		String contextName = "ContextNo" + System.currentTimeMillis();

		Identifier contextIdentifier = contextsManager.registerContext(
				contextName, context, contextConfiguration);

		CommandSubmitter commandSubmitter = DefaultCommandSubmitter
				.getInstance();

		Executor executor = Executors.newFixedThreadPool(THREAD_COUNT);

		for (int i = 0; i < THREAD_COUNT; i++) {
			executor.execute(new BenchmarkCommandSubmitter(commandSubmitter,
					contextIdentifier, COMMAND_COUNT));
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
		} while (commandTimes.size() < (THREAD_COUNT * COMMAND_COUNT));

		// Out benchmark result
		long firstTime = 0;
		long lastTime = 0;
		for (BenchmarkCommandTime commandTime : commandTimes) {

			// If first command
			if (firstTime == 0) {
				firstTime = commandTime.getPushTime();
			}

			long timeout = (commandTime.getEndTime() - commandTime
					.getPushTime()) / 1000000;

			// Get last command
			lastTime = commandTime.getEndTime();

			System.out.println("Command with id = "
					+ commandTime.getCommandId() + " Work " + timeout);

		}

		System.out.println("For all queue time out "
				+ ((lastTime - firstTime) / 1000000));
	}
}
