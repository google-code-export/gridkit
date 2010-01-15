package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.griddynamics.gridkit.coherence.patterns.benchmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.command.benchmark.SpeedLimit;
import com.oracle.coherence.common.identifiers.Identifier;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;

public class FunctorBenchmarkWorker implements Invocable, Serializable
{
	private static final long serialVersionUID = -1526686068548324036L;
	
	protected Identifier[]                 contexts;
	protected FunctorBenchmarkWorkerParams params;
	
	protected transient CommandExecutionMark[] workerResult;
	
	public FunctorBenchmarkWorker(FunctorBenchmarkWorkerParams params, Identifier[] contexts)
	{
		this.contexts = contexts;
		this.params   = params;
	}
	
	@Override
	public void init(InvocationService service)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void run()
	{
		final ConcurrentLinkedQueue<CommandExecutionMark> workerResult = new ConcurrentLinkedQueue<CommandExecutionMark>();
		
		final Random rnd = new Random(System.currentTimeMillis());
		
		//TODO implement
		final PatternFacade facade = null;
		
		final FunctorFactory functorFactory = getFunctorFactoryByName(params.getFunctorType());
		
		SpeedLimit sl = null;
		if (params.getOpsPerSec() > 0) 
		{
			//TODO ask about precision
			sl = SpeedLimit.createSpeedLimit(params.getOpsPerSec() / 10, params.getOpsPerSec());
		}
		final SpeedLimit speedLimit = sl;
		
		ExecutorService service = params.getThreadCount() == 1 ? Executors.newSingleThreadExecutor() 
															   : Executors.newFixedThreadPool(params.getThreadCount());
		
		for(int i = 0; i != params.getCommandPerThread(); ++i)
		{
			for(int j = 0; j != params.getThreadCount(); ++j)
			{
				final long executionID   = j * 10000000 + i;
				final Identifier context = contexts[rnd.nextInt(contexts.length)];
				
				Runnable rn = new Runnable()
				{
					@Override
					public void run()
					{
						if (speedLimit != null)
						{
							speedLimit.accure();
						}
						
						BenchmarkFunctor functor = functorFactory.createFunctor(executionID);
						
						Future<CommandExecutionMark> functorResultFuture = facade.submitFunctor(context, functor.submit());
						
						CommandExecutionMark functorResult = null;
						try
						{
							functorResult = functorResultFuture.get();
						}
						catch (Exception e)
						{
							throw new RuntimeException("Exception when getting result from the functorResultFuture");
						}
						
						workerResult.add(functorResult);
					}
				};
				
				service.submit(rn);
			}
		}
		
		//TODO can i do this?
		synchronized (this)
		{
			this.workerResult = workerResult.toArray(new CommandExecutionMark[0]);
		}
	}
	
	@Override
	public Object getResult()
	{
		synchronized (this)
		{
			return workerResult;
		}
	}

	public static FunctorFactory getFunctorFactoryByName(String name)
	{
		String functor = name.toLowerCase();
		
		if ("touch".equals(functor))
		{
			return new FunctorFactory.TouchFactory();
		}
		else
			throw new RuntimeException("Unknown functor type '" + functor + "'");
	}
}
