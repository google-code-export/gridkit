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
package com.griddynamics.gridkit.coherence.patterns.benchmark;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.MessageExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;

public abstract class Dispatcher<M extends MessageExecutionMark,
								 S extends InvocationServiceStats<?>,
								 P extends BenchmarkParams>
{
	protected final Object memorySynchronizer = new Object();
	
	protected S              dispatcherResult;
	protected List<Collection<M>>  workersResult;
	protected CountDownLatch latch;
	
	protected final Set<Member>       members;
	protected final InvocationService invocationService;
	
	protected abstract S                   createDispatcherResult();
	protected abstract List<Collection<M>> createWorkersResult();
	
	protected abstract Invocable getInvocableWorker();
	
	protected void prepare(P benchmarkParams) throws Exception {};
	protected void after(P benchmarkParams) throws Exception {};
	
	public Dispatcher(Set<Member> members, InvocationService invocationService)
	{
		super();
		this.members           = members;
		this.invocationService = invocationService;
	}
	
	public S execute(P benchmarkParams)
	{
		try
		{
			prepare(benchmarkParams);
			
			synchronized (memorySynchronizer)
			{
				latch            = new CountDownLatch(members.size());
				dispatcherResult = createDispatcherResult();
				workersResult    = createWorkersResult();
			}
			
			invocationService.execute(getInvocableWorker(), members, new BenchmarkObserver());

			latch.await();

			after(benchmarkParams);
			
			//Guaranteed by latch.await()
			calculateExecutionStatistics();
		}
		catch (Throwable t)
		{
			sysOut("-------- Exception on Dispatcher.execute() --------");
			t.printStackTrace();
			System.exit(1);
		}
		
		workersResult = null;
		System.gc();
		return dispatcherResult;
	}
	
	protected abstract void calculateExecutionStatistics();
	
	protected int getDispatcherResultSise()
	{
		int n = 0;
		
		for (Collection<M> l : workersResult)
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
