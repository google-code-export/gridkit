package com.griddynamics.gridkit.coherence.patterns.benchmark;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;

public abstract class Dispatcher<M extends MessageExecutionMark,
								 S extends InvocationServiceStats<?>>
{
	protected final Object memorySynchronizer = new Object();
	
	protected S              dispatcherResult;
	protected List<List<M>>  workersResult;
	protected CountDownLatch latch;
	
	protected final Set<Member>       members;
	protected final InvocationService invocationService;
	
	protected abstract S             createDispatcherResult();
	protected abstract List<List<M>> createWorkersResult();
	
	protected abstract Invocable getInvocableWorker();
	
	protected abstract void prepare() throws Exception;
	
	public Dispatcher(Set<Member> members, InvocationService invocationService)
	{
		super();
		this.members           = members;
		this.invocationService = invocationService;
	}
	
	public S execute()
	{
		try
		{
			prepare();
			
			synchronized (memorySynchronizer)
			{
				latch            = new CountDownLatch(members.size());
				dispatcherResult = createDispatcherResult();
				workersResult    = createWorkersResult();
			}
			
			invocationService.execute(getInvocableWorker(), members, new BenchmarkObserver());

			latch.await();

			//Guaranteed by latch.await()
			calculateExecutionStatistics();
		}
		catch (Throwable t)
		{
			sysOut("-------- Exception on Dispatcher.execute() --------");
			t.printStackTrace();
			System.exit(1);
		}
		
		return dispatcherResult;
	}
	
	protected abstract void calculateExecutionStatistics();
	
	protected int getDispatcherResultSise()
	{
		int n = 0;
		
		for (List<M> l : workersResult)
		{
			n += l.size();
		}
		
		return n;
	}
	
	class BenchmarkObserver implements InvocationObserver
	{
		@SuppressWarnings("unchecked")
		@Override
		public void memberCompleted(Member member, Object oResult)
		{
			synchronized (memorySynchronizer)
			{
				workersResult.add(Arrays.asList((M[])oResult));
				dispatcherResult.setMembersCompleated(dispatcherResult.getMembersCompleated()+1);
				latch.countDown();
			}
		}

		@Override
		public void memberFailed(Member member, Throwable eFailure)
		{
			synchronized (memorySynchronizer)
			{			
				dispatcherResult.setMembersFailed(dispatcherResult.getMembersFailed()+1);
				latch.countDown();
			}
		}

		@Override
		public void memberLeft(Member member)
		{
			synchronized (memorySynchronizer)
			{			
				dispatcherResult.setMembersLeft(dispatcherResult.getMembersLeft()+1);
				latch.countDown();
			}
		}
		
		@Override
		public void invocationCompleted()
		{

		}
	}
}
