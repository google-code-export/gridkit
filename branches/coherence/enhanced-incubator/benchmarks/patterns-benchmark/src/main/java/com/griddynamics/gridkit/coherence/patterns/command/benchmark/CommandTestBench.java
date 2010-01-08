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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.Command;
import com.tangosol.net.CacheFactory;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class CommandTestBench {

	private int threadCount = 1;
	private int commandPerThread = 1000;
	private int contextCount = 1;
	
	private Identifier[] contexts;
	private SpeedLimit speedLimit;
	private Random rnd;
	private TaskType taskType;

	public static void main(String[] args) {
		new CommandTestBench().start(args);
	}
	
	public void start(String[] args) {
		// Configure coherence
		TestHelper.setSysProp("tangosol.pof.config", "benchmark-pof-config.xml");
		TestHelper.setSysProp("tangosol.coherence.cacheconfig", "benchmark-pof-cache-config.xml");
		TestHelper.setSysProp("tangosol.coherence.clusterport", "9001");
		TestHelper.setSysProp("tangosol.coherence.distributed.localstorage", "false");

		CacheFactory.getCache("warmup").clear();
		
		TestHelper.setSysProp("benchmark.threadCount", "4");
		TestHelper.setSysProp("benchmark.commandPerThread", "1000");
		TestHelper.setSysProp("benchmark.contextCount", "10");
		TestHelper.setSysProp("benchmark.command", "empty");
		TestHelper.setSysProp("benchmark.speedLimit", "0");
		
		initCommandType();		
		
		threadCount = Integer.getInteger("benchmark.threadCount");
		commandPerThread = Integer.getInteger("benchmark.commandPerThread");
		contextCount = Integer.getInteger("benchmark.contextCount");
		
		Integer limit = Integer.getInteger("benchmark.speedLimit");
		if (limit > 0) {
			speedLimit = new SpeedLimit(limit/10, limit);
		}

		final PatternFacade facade = PatternFacade.Helper.create();

		// warm up run
		TestHelper.sysout("Warming up ...");
		for(int n = 0; n != 20; ++n)
		{
			Identifier[] ctx = new Identifier[contextCount];
			for(int i = 0; i != ctx.length; ++i) {
				ctx[i] = facade.registerContext("warmup-" + i, new SimpleTestContext("warnup-" + i));
			}
			
			int taskCount = 500;
			for(int i = 0; i != taskCount; ++i) {
				Command<SimpleTestContext> task = createCommand(-1 -i, "warmup");
				facade.submit(ctx[i % ctx.length], task);
			}
	
			BenchmarkSupport.waitForBuffer("warmup", taskCount * taskType.getMarksPerTask());
			LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
			CacheFactory.getCache("warmup").clear();
		}

		LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
		
		TestHelper.sysout("Starting test ...");
		TestHelper.sysout("Thread count: %d", threadCount);
		TestHelper.sysout("Command count: %d (%d per thread)", threadCount * commandPerThread, commandPerThread);
		TestHelper.sysout("Context count: %d", contextCount);
		
		final String reportBuffer = "command-benchmark";
		CacheFactory.getCache(reportBuffer).clear();
		rnd = new Random();
		contexts = new Identifier[contextCount];

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
						if (speedLimit != null) {
							speedLimit.accure();
						}
						Command<SimpleTestContext> task = createCommand(id, reportBuffer);
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

		Map<Long, ExecMark> stats = BenchmarkSupport.waitForBuffer(reportBuffer, threadCount * commandPerThread * taskType.getMarksPerTask());

		System.out.println();
		TestHelper.sysout("Done");
		TestHelper.sysout("Thread count: %d", threadCount);
		TestHelper.sysout("Command count: %d (%d per thread)", threadCount * commandPerThread, commandPerThread);
		TestHelper.sysout("Context count: %d", contextCount);

		TestHelper.sysout("MS statistics");
		StatHelper.reportStatsMs(stats);
		TestHelper.sysout("NS statistics");
		StatHelper.reportStatsNs(stats);
		
		System.exit(0);
	}
	
	void initCommandType() {
		String cmdType = System.getProperty("benchmark.command").toLowerCase();
		if ("empty".equals(cmdType)) {
			taskType = new EmptyTaskType();
		}
		else if ("read".equals(cmdType)) {
			taskType = new ReadTaskType();
		}
		else if ("update".equals(cmdType)) {
			taskType = new UpdateTaskType();
		}
		else throw new RuntimeException("Unknown command type '" + cmdType + "'");
	}
	
	Command<SimpleTestContext> createCommand(long id, String reportBuffer) {
		return taskType.createCommand(id, reportBuffer);
	}
	
	interface TaskType {
		public Command<SimpleTestContext> createCommand(long id, String reportBuffer);
		public int getMarksPerTask();
	}
	
	class EmptyTaskType implements TaskType {

		@Override
		public Command<SimpleTestContext> createCommand(long id, String reportBuffer) {
			return new EmptyCommand(id, reportBuffer);
		}
		
		@Override
		public int getMarksPerTask() {			
			return 1;
		}
	}

	class ReadTaskType implements TaskType {
		
		@Override
		public Command<SimpleTestContext> createCommand(long id, String reportBuffer) {
			return new ReadCommand(id, reportBuffer);
		}
		
		@Override
		public int getMarksPerTask() {			
			return 1;
		}
	}
	
	class UpdateTaskType implements TaskType {
		
		@Override
		public Command<SimpleTestContext> createCommand(long id, String reportBuffer) {
			return new UpdateCommand(id, reportBuffer);
		}
		
		@Override
		public int getMarksPerTask() {			
			return 1;
		}
	}
}
