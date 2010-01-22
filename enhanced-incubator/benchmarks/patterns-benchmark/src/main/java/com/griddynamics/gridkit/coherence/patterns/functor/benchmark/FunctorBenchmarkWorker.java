package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.griddynamics.gridkit.coherence.patterns.benchmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.FunctorExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SpeedLimit;
import com.oracle.coherence.common.identifiers.Identifier;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;

public class FunctorBenchmarkWorker implements Invocable, Serializable
{
	private static final long serialVersionUID = -1526686068548324036L;
	
	protected Identifier[] contexts;
	protected Map<Member, FunctorBenchmarkWorkerParams> paramsMap;
	
	protected transient CommandExecutionMark[] workerResult;
	
	public FunctorBenchmarkWorker(Map<Member, FunctorBenchmarkWorkerParams> paramsMap, Identifier[] contexts)
	{
		this.contexts  = contexts;
		this.paramsMap = paramsMap;
	}
	
	@Override
	public void init(InvocationService service)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void run()
	{
		try
		{
			final FunctorBenchmarkWorkerParams params = paramsMap.get(CacheFactory.getCluster().getLocalMember());
			
			final ConcurrentLinkedQueue<FunctorExecutionMark> workerResult = new ConcurrentLinkedQueue<FunctorExecutionMark>();
			
			final Random rnd = new Random(System.currentTimeMillis());
			
			final PatternFacade facade = PatternFacade.DefaultFacade.getInstance();
			
			final FunctorFactory functorFactory = getFunctorFactoryByName(params.getFunctorType());
			
			SpeedLimit sl = null;
			if (params.getOpsPerSec() > 0) 
			{
				sl = SpeedLimit.createSpeedLimit(params.getOpsPerSec());
			}
			final SpeedLimit speedLimit = sl;
			
			ExecutorService service = params.getThreadCount() == 1 ? Executors.newSingleThreadExecutor() 
																   : Executors.newFixedThreadPool(params.getThreadCount());
			
			List<Callable<Void>> workers = new ArrayList<Callable<Void>>(params.getThreadCount());
			final CyclicBarrier  barrier = new CyclicBarrier(params.getThreadCount());
			
			for(int j = 0; j != params.getThreadCount(); ++j)
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
							
							for(int i = 0; i != params.getInvocationPerThread(); ++i)
							{
								long executionID = exID + i;
								Identifier context = contexts[rnd.nextInt(contexts.length)];
							
								if (speedLimit != null)
								{
									speedLimit.accure();
								}
									
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
			
			//TODO can i do this?
			synchronized (paramsMap)
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
	}
	
	@Override
	public Object getResult()
	{
		synchronized (paramsMap)
		{
			return workerResult;
		}
	}

	public static FunctorFactory getFunctorFactoryByName(String name)
	{
		String functor = name.toLowerCase();
		
		if ("touch".equalsIgnoreCase(functor))
		{
			return new FunctorFactory.TouchFactory();
		}
		else
			throw new RuntimeException("Unknown functor type '" + functor + "'");
	}
}
