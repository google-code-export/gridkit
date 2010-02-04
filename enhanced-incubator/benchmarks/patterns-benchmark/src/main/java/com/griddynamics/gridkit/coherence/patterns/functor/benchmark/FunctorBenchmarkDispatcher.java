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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.griddynamics.gridkit.coherence.patterns.benchmark.Dispatcher;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.FunctorExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.Accamulator;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;
import com.oracle.coherence.common.identifiers.Identifier;
import com.tangosol.net.Invocable;
import com.tangosol.net.Member;

public class FunctorBenchmarkDispatcher extends Dispatcher<FunctorExecutionMark,
														   InvocationServiceStats<FunctorBenchmarkStats>,
														   FunctorBenchmarkParams>
{
	protected final PatternFacade facade;
	
	protected Invocable invocableWorker;
	
	public FunctorBenchmarkDispatcher(Set<Member> members, PatternFacade facade)
	{
		super(members,facade.getInvocationService());
		this.facade = facade;
	}
	
	@Override
	protected void prepare(FunctorBenchmarkParams benchmarkParams) throws Exception
	{
		Identifier[] contexts = new Identifier[benchmarkParams.getContextsCount()];
		
		for(int i=0; i < contexts.length; ++i)
		{
			contexts[i] = facade.registerContext("ctx-" + i, new SimpleContext("ctx-" + i));
		}
		
		invocableWorker = new FunctorBenchmarkWorker(benchmarkParams, contexts);
	}

	protected void calculateExecutionStatistics()
	{
		dispatcherResult.setJavaMsStats(calculateExecutionStatisticsInternal(new FunctorExecutionMark.JavaMsExtractor()));

		dispatcherResult.setJavaNsStats(calculateExecutionStatisticsInternal(new FunctorExecutionMark.JavaNsExtractor()));
		
		dispatcherResult.setCoherenceMsStats(calculateExecutionStatisticsInternal(new FunctorExecutionMark.CoherenceMsExtractor()));
		
		dispatcherResult.setExecutionMarksProcessed(getDispatcherResultSise());
	}
	
	protected FunctorBenchmarkStats calculateExecutionStatisticsInternal(FunctorExecutionMark.FunctorExecutionMarkTimeExtractor te)
	{	
		Accamulator  startTime = new Accamulator();
		Accamulator returnTime = new Accamulator();
		
		Accamulator sumbitLatency = new Accamulator();
		Accamulator returnLatency = new Accamulator();
		
		int n = 0;
		
		for (Collection<FunctorExecutionMark> l : workersResult)
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
		
		FunctorBenchmarkStats res = new FunctorBenchmarkStats();
		
		res.totalTime  = (returnTime.getMax() - startTime.getMin()) / TimeUnit.SECONDS.toMillis(1);
		res.throughput = n / res.totalTime;
		
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

	@Override
	protected Invocable getInvocableWorker()
	{
		return invocableWorker;
	}
	
	@Override
	protected InvocationServiceStats<FunctorBenchmarkStats> createDispatcherResult()
	{
		return new InvocationServiceStats<FunctorBenchmarkStats>();
	}

	@Override
	protected List<Collection<FunctorExecutionMark>> createWorkersResult()
	{
		return new ArrayList<Collection<FunctorExecutionMark>>();
	}
}
