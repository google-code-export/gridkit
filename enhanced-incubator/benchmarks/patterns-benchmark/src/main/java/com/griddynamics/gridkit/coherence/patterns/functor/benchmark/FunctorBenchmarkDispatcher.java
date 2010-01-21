package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.griddynamics.gridkit.coherence.patterns.benchmark.FunctorExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.Accamulator;
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
	protected List<List<FunctorExecutionMark>> workersResult;
	
	public FunctorBenchmarkDispatcher(int contextsCount)
	{
		this.contexts = new Identifier[contextsCount];
	}
	
	public FunctorBenchmarkStats execute(PatternFacade facade,
										 Map<Member,FunctorBenchmarkWorkerParams> workers)
	{
		try
		{
			synchronized (memorySynchronizer)
			{
				latch            = new CountDownLatch(workers.size());
				dispatcherResult = new FunctorBenchmarkStats();
				workersResult    = new ArrayList<List<FunctorExecutionMark>>();
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
			
			//Guaranteed by latch.await()
			calculateExecutionStatistics();
		}
		catch (Throwable t)
		{
			sysOut("-------- Exception on FunctorBenchmarkStats.execute(...) --------");
			t.printStackTrace();
			System.exit(1);
		}
		
		return dispatcherResult;
	}
	
	class FunctorBenchmarkObserver implements InvocationObserver
	{
		@Override
		public void memberCompleted(Member member, Object oResult)
		{
			synchronized (memorySynchronizer)
			{
				workersResult.add(Arrays.asList((FunctorExecutionMark[])oResult));
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

	protected void calculateExecutionStatistics()
	{
		dispatcherResult.setJavaMsStats(calculateExecutionStatisticsInternal(new FunctorExecutionMark.JavaMsExtractor()));

		dispatcherResult.setJavaNsStats(calculateExecutionStatisticsInternal(new FunctorExecutionMark.JavaNsExtractor()));
		
		dispatcherResult.setCoherenceMsStats(calculateExecutionStatisticsInternal(new FunctorExecutionMark.CoherenceMsExtractor()));
	}
	
	protected FunctorBenchmarkStats.TimeUnitDependStats calculateExecutionStatisticsInternal
			   (FunctorExecutionMark.FunctorExecutionMarkTimeExtractor te)
	{	
		Accamulator  startTime = new Accamulator();
		Accamulator returnTime = new Accamulator();
		
		Accamulator sumbitLatency = new Accamulator();
		Accamulator returnLatency = new Accamulator();
		
		int n = 0;
		
		for (List<FunctorExecutionMark> l : workersResult)
		{
			for(FunctorExecutionMark m : l)
			{
				n++;
				
				startTime.add(te.getSendTime(m));
				returnTime.add(te.getReturnTime(m));
				
				sumbitLatency.add(te.getReceiveTime(m) - te.getSendTime(m));
				returnLatency.add(te.getReturnTime(m) - te.getSendTime(m));
			}
		}
		
		FunctorBenchmarkStats.TimeUnitDependStats res = new FunctorBenchmarkStats.TimeUnitDependStats();
		
		res.totalTime  = returnTime.getMax() - startTime.getMin();
		res.throughput = n / (res.totalTime / TimeUnit.SECONDS.toMillis(1));
		
		res.averageSumbitLatency = sumbitLatency.getMean();
		res.averageReturnLatency = returnLatency.getMean();
		
		res.sumbitLatencyVariance = sumbitLatency.getVariance();
		res.returnLatencyVariance = returnLatency.getVariance();
		
		res.maxSumbitLatency = sumbitLatency.getMax();
		res.maxReturnLatency = returnLatency.getMax();
		
		res.minSumbitLatency = sumbitLatency.getMin();
		res.minReturnLatency = returnLatency.getMin();
		
		return res;
	}
}
