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

import java.util.Map;
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

	private static final int THREAD_COUNT = 10;
	private static final int COMMAND_COUNT = 5;

	/**
	 * Customize coherence and run benchmark
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Configure coherence
		System.setProperty("tangosol.pof.config", "pof-config.xml");
		System.setProperty("tangosol.coherence.cacheconfig", "coherence-commandpattern-cache-config.xml");
		
		DefaultContextConfiguration contextConfiguration = new DefaultContextConfiguration(
				ManagementStrategy.COLOCATED);

		ContextsManager contextsManager = DefaultContextsManager.getInstance();
		BenchmarkContext context = new BenchmarkContext();
		String contextName = "ContextNo" + System.currentTimeMillis();

		Identifier contextIdentifier = contextsManager.registerContext(
				contextName, context, contextConfiguration);

		CommandSubmitter commandSubmitter = DefaultCommandSubmitter
				.getInstance();

		commandSubmitter.submitCommand(contextIdentifier, new BenchmarkCommand());

		Executor executor = Executors.newFixedThreadPool(THREAD_COUNT);

		for (int i = 0; i < THREAD_COUNT; i++) {
			executor.execute(new BenchmarkCommandSubmitter(commandSubmitter,
					contextIdentifier, COMMAND_COUNT));
		}

		// Sleep some time when command execute.
		try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Get context
		BenchmarkContext con = (BenchmarkContext) contextsManager
				.getContext(contextIdentifier);
		// Get start time map and end time map
		Map<Long, Long> startTimes = con.getStartTimes();
		Map<Long, Long> endTimes = con.getEndTimes();
		
		// Out benchmark result
		for (long commandId : startTimes.keySet()) {

			if ((startTimes.get(commandId) != null)
					&& (endTimes.get(commandId) != null)) {
				
				long timeout = endTimes.get(commandId)
						- startTimes.get(commandId);

				System.out.println("Command with id = " + commandId + " Work "
						+ timeout);

			}
		}

	}

}
