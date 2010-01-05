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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.Command;
import com.tangosol.net.CacheFactory;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class EmptyCommandBenchmark {

	private static int threadCount = 1;
	private static int commandPerThread = 1000;
	private static int contextCount = 1;

	public static void main(String[] args) {
		new EmptyCommandBenchmark().start(args);
	}
	
	public void start(String[] args) {
		// Configure coherence
		TestHelper.setSysProp("tangosol.pof.config", "benchmark-pof-config.xml");
		TestHelper.setSysProp("tangosol.coherence.cacheconfig", "benchmark-pof-cache-config.xml");
		TestHelper.setSysProp("tangosol.coherence.clusterport", "9001");
		TestHelper.setSysProp("tangosol.coherence.distributed.localstorage", "false");
		
		TestHelper.setSysProp("benchmark.threadCount", "1");
		TestHelper.setSysProp("benchmark.commandPerThread", "1000");
		TestHelper.setSysProp("benchmark.contextCount", "1");
		
		CacheFactory.getCache("warmup");
		
		threadCount = Integer.getInteger("benchmark.threadCount");
		commandPerThread = Integer.getInteger("benchmark.commandPerThread");
		contextCount = Integer.getInteger("benchmark.contextCount");

		final PatternFacade facade = PatternFacade.Helper.create();

		// warm up run
		TestHelper.sysout("Warming up ...");
		{
			Identifier ctx = facade.registerContext("warmup", new SimpleTestContext("warnup"));
			
			int taskCount = 100;
			for(int i = 0; i != taskCount; ++i) {
				Command<SimpleTestContext> task = new EmptyCommand(-1 -i, "warmup");
				facade.submit(ctx, task);
			}
	
			BenchmarkSupport.waitForBuffer("warmup", taskCount);
		}
		
		
		TestHelper.sysout("Starting test ...");
		TestHelper.sysout("Thread count: %d", threadCount);
		TestHelper.sysout("Command count: %d (%d per thread)", threadCount * commandPerThread, commandPerThread);
		TestHelper.sysout("Context count: %d", contextCount);
		
		final String reportBuffer = "command-benchmark";
		CacheFactory.getCache(reportBuffer).clear();
		final Random rnd = new Random();
		final Identifier[] contexts = new Identifier[contextCount];

		for(int i = 0; i != contextCount; ++i) {
			contexts[i] = facade.registerContext("ctx-" + i, new SimpleTestContext("ctx-" + i));
		}
		
		ExecutorService service = threadCount == 1 ? null : Executors.newFixedThreadPool(threadCount);
		
		for(int i = 0; i != commandPerThread; ++i) {
			for(int j = 0; j != threadCount; ++j) {
				final Identifier ctx = contexts[rnd.nextInt(contexts.length)];
				final long id = j * 10000000 + i;
				
				Runnable rn = new Runnable() {
					@Override
					public void run() {
						Command<SimpleTestContext> task = new EmptyCommand(id, reportBuffer);
						facade.submit(ctx, task);
					}
				};
				
				if (service == null) {
					rn.run();
				}
				else {
					service.submit(rn);
				}
			}
		}

		Map<Long, ExecMark> stats = BenchmarkSupport.waitForBuffer(reportBuffer, threadCount * commandPerThread);

		System.out.println();
		TestHelper.sysout("Done");
		TestHelper.sysout("Thread count: %d", threadCount);
		TestHelper.sysout("Command count: %d (%d per thread)", threadCount * commandPerThread, commandPerThread);
		TestHelper.sysout("Context count: %d", contextCount);

		StatHelper.reportStatsMs(stats);
	}
}
