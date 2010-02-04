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

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.FunctorExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.speedlimit.SpeedLimit;
import com.oracle.coherence.common.identifiers.Identifier;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;

public class FunctorBenchmarkWorker implements Invocable, Serializable
{
	private static final long serialVersionUID = -1526686068548324036L;
	
	protected final Identifier[] contexts;
	protected final FunctorBenchmarkParams benchmarkParams;
	
	protected transient CommandExecutionMark[] workerResult;
	
	public FunctorBenchmarkWorker(FunctorBenchmarkParams benchmarkParams, Identifier[] contexts)
	{
		this.contexts        = contexts;
		this.benchmarkParams = benchmarkParams;
	}

	@Override
	public void run()
	{
		try
		{
			final ConcurrentLinkedQueue<FunctorExecutionMark> workerResult = new ConcurrentLinkedQueue<FunctorExecutionMark>();
			
			final Random rnd = new Random(System.currentTimeMillis());
			
			final PatternFacade facade = PatternFacade.DefaultFacade.getInstance();
			
			final FunctorFactory functorFactory = getFunctorFactoryByName(benchmarkParams.getFunctorType());
			
			final SpeedLimit speedLimit = SpeedLimit.SpeedLimitHelper.getSpeedLimit(benchmarkParams.getOpsPerSec());
			
			ExecutorService service = benchmarkParams.getThreadCount() == 1 ? Executors.newSingleThreadExecutor() 
																			: Executors.newFixedThreadPool(benchmarkParams.getThreadCount());
			
			List<Callable<Void>> workers = new ArrayList<Callable<Void>>(benchmarkParams.getThreadCount());
			final CyclicBarrier  barrier = new CyclicBarrier(benchmarkParams.getThreadCount());
			
			for(int j = 0; j != benchmarkParams.getThreadCount(); ++j)
			{
				final long exID = j * 10000000;
				
				Callable<Void> rn = new Callable<Void>()
				{
					@Override
					public Void call() throws Exception
					{
						try
						{
							barrier.await();
							
							for(int i = 0; i != benchmarkParams.getInvocationPerThread(); ++i)
							{
								long executionID = exID + i;
								Identifier context = contexts[rnd.nextInt(contexts.length)];
							
								speedLimit.accure();
									
								BenchmarkFunctor functor = functorFactory.createFunctor(executionID);
									
								Future<CommandExecutionMark> functorResultFuture = facade.submitFunctor(context, functor.send());
								
								FunctorExecutionMark functorResult = new FunctorExecutionMark(functorResultFuture.get());
								functorResult.returN();
								workerResult.add(functorResult);
							}
							
						}
						catch (Throwable t)
						{
							sysOut("-------- Exception on FunctorBenchmarkWorker$run$Runnable.run() --------");
							t.printStackTrace();
							System.exit(1);
						}
						
						return null;
					}
				};
				
				workers.add(rn);
			}

			service.invokeAll(workers);
			
			synchronized (benchmarkParams)
			{
				this.workerResult = workerResult.toArray(new FunctorExecutionMark[0]);
			}
		}
		catch (Throwable t)
		{
			sysOut("-------- Exception on FunctorBenchmarkWorker.run() --------");
			t.printStackTrace();
			System.exit(1);
		}
		
		System.gc();
	}
	
	@Override
	public Object getResult()
	{
		synchronized (benchmarkParams)
		{
			return workerResult;
		}
	}

	public static FunctorFactory getFunctorFactoryByName(String name)
	{
		if ("touch".equalsIgnoreCase(name))
		{
			return new FunctorFactory.TouchFactory();
		}
		else
			throw new RuntimeException("Unknown functor type '" + name + "'");
	}
	
	@Override
	public void init(InvocationService service)
	{

	}
}
