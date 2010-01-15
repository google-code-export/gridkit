package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.griddynamics.gridkit.coherence.patterns.benchmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.oracle.coherence.common.identifiers.Identifier;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;

public class FunctorBenchmarkDispatcher
{
	protected final Identifier[] contexts;
	
	protected final Object memorySynchronizer = new Object();
	protected CountDownLatch latch;
	protected FunctorBenchmarkStats dispatcherResult;
	protected List<List<CommandExecutionMark>> workersResult;
	
	public FunctorBenchmarkDispatcher(int contextsCount)
	{
		this.contexts = new Identifier[contextsCount];
	}
	
	public FunctorBenchmarkStats execute(PatternFacade facade,
										 Map<Member,FunctorBenchmarkWorkerParams> workers)
	{
		synchronized (memorySynchronizer)
		{
			latch            = new CountDownLatch(workers.size());
			dispatcherResult = new FunctorBenchmarkStats();
			workersResult    = new ArrayList<List<CommandExecutionMark>>();
		}
		
		for(int i=0; i < contexts.length; ++i)
		{
			contexts[i] = facade.registerContext("ctx-" + i, new SimpleContext("ctx-" + i));
		}
		
		InvocationService invocationService = facade.getInvocationService();
		
		for(Map.Entry<Member, FunctorBenchmarkWorkerParams> worker : workers.entrySet())
		{
			invocationService.execute(new FunctorBenchmarkWorker(worker.getValue(), contexts),
									  Collections.singleton(worker.getKey()),
									  new FunctorBenchmarkObserver());
		}
		
		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException("FunctorBenchmarkDispatcher has been interrupted");
		}
		
		return null;
	}
	
	class FunctorBenchmarkObserver implements InvocationObserver
	{
		@Override
		public void memberCompleted(Member member, Object oResult)
		{
			synchronized (memorySynchronizer)
			{
				workersResult.add(Arrays.asList((CommandExecutionMark[])oResult));
				dispatcherResult.setMembersCompleated(dispatcherResult.getMembersCompleated()+1);
			}
		}

		@Override
		public void memberFailed(Member member, Throwable eFailure)
		{
			synchronized (memorySynchronizer)
			{			
				dispatcherResult.setMembersFailed(dispatcherResult.getMembersFailed()+1);
			}
		}

		@Override
		public void memberLeft(Member member)
		{
			synchronized (memorySynchronizer)
			{			
				dispatcherResult.setMembersLeft(dispatcherResult.getMembersLeft()+1);
			}
		}
		
		@Override
		public void invocationCompleted()
		{
			synchronized (memorySynchronizer)
			{
				latch.countDown();
			}
		}
	}
	
	protected static FunctorBenchmarkStats calculateExecutionStatistics(FunctorBenchmarkStats dispatcherResult,
																		List<List<CommandExecutionMark>> workersResult)
	{
		return dispatcherResult;
	}
}
