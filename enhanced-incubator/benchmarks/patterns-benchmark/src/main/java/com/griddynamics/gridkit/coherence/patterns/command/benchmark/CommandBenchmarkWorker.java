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

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.io.Serializable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.griddynamics.gridkit.coherence.patterns.benchmark.Dispatcher;
import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.speedlimit.SpeedLimit;
import com.griddynamics.gridkit.coherence.patterns.command.benchmark.commands.CommandFactory;
import com.oracle.coherence.common.identifiers.Identifier;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;

public class CommandBenchmarkWorker implements Invocable, Serializable
{
	private static final long serialVersionUID = 4861617501489064620L;

	protected CommandBenchmarkParams benchmarkParams;
	protected Identifier[] contexts;
	protected Map<Integer, Integer> workerIDs;
	
	public CommandBenchmarkWorker(CommandBenchmarkParams benchmarkParams, Identifier[] contexts, Map<Integer, Integer> workerIDs)
	{
		this.contexts        = contexts;
		this.workerIDs       = workerIDs;
		this.benchmarkParams = benchmarkParams;
	}

	@Override
	public void run()
	{
		try
		{
			final Random rnd = new Random(System.currentTimeMillis());
			
			final CommandFactory commandFactory = CommandBenchmarkDispatcher.getCommandFactory(benchmarkParams.getCommand());
			
			final SpeedLimit speedLimit = SpeedLimit.SpeedLimitHelper.getSpeedLimit(benchmarkParams.getOpsPerSec());
			
			final PatternFacade facade = PatternFacade.DefaultFacade.getInstance();
			
			final int workerID = workerIDs.get(CacheFactory.getCluster().getLocalMember().getId());
			
			ExecutorService service = Executors.newFixedThreadPool(benchmarkParams.getThreadCount());
			
			final CyclicBarrier startBarrier = new CyclicBarrier(benchmarkParams.getThreadCount());
			final CountDownLatch finishLatch = new CountDownLatch(benchmarkParams.getThreadCount());
			
			for(int t = 0; t != benchmarkParams.getThreadCount(); ++t)
			{
				final int threadNumber = t;
				
				Callable<Void> worker = new Callable<Void>()
				{
					@Override
					public Void call() throws Exception 
					{
						try
						{
							startBarrier.await();
							
							long commandsPerWorker       = CommandBenchmarkDispatcher.getCommandsPerWorker(benchmarkParams);
							long commandsPerWorkerThread = CommandBenchmarkDispatcher.getCommandsPerWorkerThead(benchmarkParams);
							
							long offset = workerID * commandsPerWorker + threadNumber * commandsPerWorkerThread;
							
							for(int c = 0; c != benchmarkParams.getCommandPerThread(); ++c)
							{
								Identifier ctx = contexts[rnd.nextInt(contexts.length)];
								long id = offset + c * commandFactory.getMarksPerCommand();
								
								speedLimit.accure();
		
								facade.submit(ctx, commandFactory.createCommand(id, benchmarkParams.getReportBuffer()).send());
							}
							
							finishLatch.countDown();
						}
						catch (Throwable t)
						{
							sysOut("-------- Exception on CommandBenchmark.worker.call() --------");
							t.printStackTrace();
							System.exit(1);
						}
						
						return null;
					}
				};
				
				service.submit(worker);
			}
			
			finishLatch.await();
			service.shutdown();
		}
		catch (Throwable t)
		{
			sysOut("-------- Exception on CommandBenchmark.executeInternal(...) --------");
			t.printStackTrace();
			System.exit(1);
		}
		
		if (Dispatcher.gcInWorker)
			System.gc();
	}
	
	@Override
	public Object getResult()
	{
		return (new CommandExecutionMark[0]);
	}
	
	@Override
	public void init(InvocationService paramInvocationService)
	{
		
	}
}
