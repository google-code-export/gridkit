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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.griddynamics.gridkit.coherence.patterns.benchmark.Dispatcher;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.Accamulator;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;
import com.griddynamics.gridkit.coherence.patterns.command.benchmark.commands.CommandFactory;
import com.oracle.coherence.common.identifiers.Identifier;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.Member;

public class CommandBenchmarkDispatcher extends Dispatcher<CommandExecutionMark,
														   InvocationServiceStats<CommandBenchmarkStats>,
														   CommandBenchmarkParams>
{
	protected PatternFacade facade;
	
	protected Invocable invocableWorker;
	
	public CommandBenchmarkDispatcher(Set<Member> members, PatternFacade facade)
	{
		super(members, facade.getInvocationService());
		this.facade = facade;
	}
	
	@Override
	protected void prepare(CommandBenchmarkParams benchmarkParams) throws Exception
	{
		CacheFactory.getCache(benchmarkParams.getReportBuffer()).clear();
		
		Identifier[] contexts = new Identifier[benchmarkParams.getContextCount()];
		
		for(int i = 0; i != benchmarkParams.getContextCount(); ++i)
		{
			contexts[i] = facade.registerContext("ctx-" + i, new SimpleContext("ctx-" + i));
		}
		
		Map<Integer, Integer> workerIDs = new HashMap<Integer, Integer>();
		
		int i = 0;
		for (Member m : members)
		{
			workerIDs.put(m.getId(), i++);
		}
		
		invocableWorker = new CommandBenchmarkWorker(benchmarkParams, contexts, workerIDs);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void after(CommandBenchmarkParams benchmarkParams) throws Exception
	{
		long reportBufferSize = members.size() * getCommandsPerWorker(benchmarkParams);
		
		Map<Long, CommandExecutionMark> r = (Map<Long, CommandExecutionMark>)BenchmarkSupport.waitForBuffer(benchmarkParams.getReportBuffer(), 
																											reportBufferSize);
		
		workersResult.add(r.values());
		
		CacheFactory.getCache(benchmarkParams.getReportBuffer()).clear();
	}
	
	@Override
	protected void calculateExecutionStatistics()
	{
		dispatcherResult.setJavaMsStats(calculateExecutionStatisticsInternal(new CommandExecutionMark.JavaMsExtractor()));

		dispatcherResult.setJavaNsStats(calculateExecutionStatisticsInternal(new CommandExecutionMark.JavaNsExtractor()));
		
		dispatcherResult.setCoherenceMsStats(calculateExecutionStatisticsInternal(new CommandExecutionMark.CoherenceMsExtractor()));
		
		dispatcherResult.setExecutionMarksProcessed(getDispatcherResultSise());
	}
	
	public  CommandBenchmarkStats calculateExecutionStatisticsInternal(CommandExecutionMark.CommandExecutionMarkTimeExtractor ex)
	{	
		Accamulator     latency = new Accamulator();
		
		Accamulator    sendTime = new Accamulator();
		Accamulator receiveTime = new Accamulator();
	
		int n = 0;
		
		for (Collection<CommandExecutionMark> l : workersResult)
		{
			for(CommandExecutionMark m : l)
			{
				n++;
			
				sendTime.add(ex.getSendTime(m));
				receiveTime.add(ex.getReceiveTime(m));
				
				latency.add(ex.getReceiveTime(m) - ex.getSendTime(m));
			}
		}
		
		CommandBenchmarkStats res = new CommandBenchmarkStats();
		
		res.totalTime  = (receiveTime.getMax() - sendTime.getMin()) / TimeUnit.SECONDS.toMillis(1);
		res.throughput = n / res.totalTime;
		
		res.averageLatency  = latency.getMean();
		res.latencyVariance = latency.getVariance();
		res.minLatency      = latency.getMin();
		res.maxLatency      = latency.getMax();
		
		return res;
	}

	public static CommandFactory getCommandFactory(String name)
	{
		if ("empty".equalsIgnoreCase(name))
		{
			return new CommandFactory.EmptyCommandFactory();
		}
		else if ("read".equalsIgnoreCase(name))
		{
			return new CommandFactory.ReadCommandFactory();
		}
		else if ("update".equalsIgnoreCase(name))
		{
			return new CommandFactory.UpdateCommandFactory();
		}
		else
			throw new RuntimeException("Unknown command type '" + name + "'");
	}
	
	public static long getCommandsPerWorkerThead(CommandBenchmarkParams benchmarkParams)
	{
		return ((long)benchmarkParams.getCommandPerThread()) * getCommandFactory(benchmarkParams.getCommand()).getMarksPerCommand();
	}
	
	public static long getCommandsPerWorker(CommandBenchmarkParams benchmarkParams)
	{
		return getCommandsPerWorkerThead(benchmarkParams) * benchmarkParams.getThreadCount();
	}
	
	@Override
	protected Invocable getInvocableWorker()
	{
		return invocableWorker;
	}
	
	@Override
	protected InvocationServiceStats<CommandBenchmarkStats> createDispatcherResult()
	{
		return new InvocationServiceStats<CommandBenchmarkStats>();
	}
	
	@Override
	protected List<Collection<CommandExecutionMark>> createWorkersResult()
	{
		return new ArrayList<Collection<CommandExecutionMark>>();
	}
}
