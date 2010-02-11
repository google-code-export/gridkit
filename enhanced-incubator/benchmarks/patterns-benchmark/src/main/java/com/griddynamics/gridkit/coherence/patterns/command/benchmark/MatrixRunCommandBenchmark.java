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

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.getOtherInvocationServiceMembers;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setCoherenceConfig;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;
import static java.lang.System.getProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.griddynamics.gridkit.coherence.patterns.benchmark.csv.CSVWriter;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;
import com.tangosol.net.Member;

public class MatrixRunCommandBenchmark
{	
	protected int stageCount;
	protected double throughputScale;
	
	protected boolean doWarmUp;
	protected CSVWriter csvWriter;
	
	protected List<CommandBenchmarkParams> firstPhaseBenchmarkParams;
	protected List<CommandBenchmarkParams> secondPhasebenchmarkParams;
	
	protected IdentityHashMap<CommandBenchmarkParams, Collection<InvocationServiceStats<CommandBenchmarkStats>>> firstPhaseResults;
	protected IdentityHashMap<CommandBenchmarkParams, Collection<InvocationServiceStats<CommandBenchmarkStats>>> secondPhaseResults;
	
	protected Set<Member> allMembers;
	protected PatternFacade facade;
	
	protected String aheader;
	protected String avalues;
	
	public List<CommandBenchmarkParams> prepareBenchmarkParams(int memberCount)
	{
		ArrayList<CommandBenchmarkParams> res = new ArrayList<CommandBenchmarkParams>();
		
		setSysProp("benchmark.command.command",          "update");
		setSysProp("benchmark.command.threadCount",      "1:2");
		setSysProp("benchmark.command.contextCount",     "10");
		
		setSysProp("benchmark.command.commandCount", "250");
		//setSysProp("benchmark.command.commandPerThread", "250");
		
		for (String command : getProperty("benchmark.command.command").split(":"))
		for (String threadCount : getProperty("benchmark.command.threadCount").split(":"))
		for (String contextCount : getProperty("benchmark.command.contextCount").split(":"))
		{
			CommandBenchmarkParams bp_ = new CommandBenchmarkParams();
			
			bp_.setCommand(command);
			bp_.setThreadCount(Integer.parseInt(threadCount));
			bp_.setContextCount(Integer.parseInt(contextCount));
			bp_.setOpsPerSec(0);
			bp_.setReportBuffer("command-benchmark");
			
			if (getProperty("benchmark.command.commandCount") != null)
			{
				for (String commandCount : getProperty("benchmark.command.commandCount").split(":"))
				{
					CommandBenchmarkParams bp = new CommandBenchmarkParams(bp_);
					
					bp.setCommandPerThread((Integer.parseInt(commandCount) / memberCount) / bp.getThreadCount());
					res.add(bp);
				}
			}
			else
			{
				for (String commandPerThread : getProperty("benchmark.command.commandPerThread").split(":"))
				{
					CommandBenchmarkParams bp = new CommandBenchmarkParams(bp_);
					
					bp.setCommandPerThread(Integer.parseInt(commandPerThread));
					res.add(bp);
				}
			}
		}
		
		return res;
	}
	
	public void run(String[] args)
	{
		setSysProp("benchmark.command.callerCount", "1:2");
		
		List<Integer> callerCountList = new ArrayList<Integer>();
		for(String callerCount : getProperty("benchmark.command.callerCount").split(":"))
			callerCountList.add(Integer.parseInt(callerCount));
		
		setSysProp("benchmark.output.file", "matrixRunCommandBenchmark.csv");
		csvWriter = new CSVWriter(getProperty("benchmark.output.file"));
		
		setSysProp("benchmark.output.aheader", "");
		setSysProp("benchmark.output.avalues", "");
		
		aheader = getProperty("benchmark.output.aheader");
		avalues = getProperty("benchmark.output.avalues");
		
		setSysProp("benchmark.command.doWarmUp", "false");
		doWarmUp = Boolean.getBoolean("benchmark.command.doWarmUp");
		
		setSysProp("benchmark.command.stageCount", "2");
		stageCount = Integer.getInteger("benchmark.command.stageCount");
		
		setSysProp("benchmark.command.throughputScale", "0.7");
		throughputScale = Double.parseDouble(getProperty("benchmark.command.throughputScale"));
		
		setCoherenceConfig(false);
		facade     = PatternFacade.DefaultFacade.getInstance();
		allMembers = getOtherInvocationServiceMembers(facade.getInvocationService());
		
		if (allMembers.size() < Collections.max(callerCountList))
			throw new IllegalStateException("Not enought members to start benchmark (" + allMembers.size() + " from " + Collections.max(callerCountList) +")");
		
		csvWriter.writeRow(aheader + ";CallersCount;TimeUnit;" + CommandBenchmarkParams.getCSVHeader() + ";" + CommandBenchmarkStats.getCSVHeader());
		
		if (doWarmUp)
			SingleRunCommandBenchmark.warmUp(facade, allMembers);
		
		Random rnd = new Random(System.currentTimeMillis());
		
		for (Integer callerCount : callerCountList)
		{
			firstPhaseBenchmarkParams  = prepareBenchmarkParams(callerCount);
			secondPhasebenchmarkParams = new ArrayList<CommandBenchmarkParams>(firstPhaseBenchmarkParams.size());
			
			firstPhaseResults = new IdentityHashMap<CommandBenchmarkParams, Collection<InvocationServiceStats<CommandBenchmarkStats>>>();
			for (CommandBenchmarkParams bp : firstPhaseBenchmarkParams)
				firstPhaseResults.put(bp, new ArrayList<InvocationServiceStats<CommandBenchmarkStats>>(stageCount));
			
			for(int s = 0; s < stageCount; ++s)
			{
				sysOut("Executing first phase, stage " + s + ", members count " + callerCount);
				Collections.shuffle(firstPhaseBenchmarkParams, rnd);
				executeStage(callerCount, firstPhaseBenchmarkParams, firstPhaseResults);
			}
			
			calculateSecondPhasebenchmarkParams(callerCount);
			
			for(int s = 0; s < stageCount; ++s)
			{
				sysOut("Executing second phase, stage " + s + ", members count " + callerCount);
				Collections.shuffle(secondPhasebenchmarkParams, rnd);
				executeStage(callerCount, secondPhasebenchmarkParams, secondPhaseResults);
			}
		}
		
		csvWriter.close();
	}
	
	public void executeStage(int memberCount,
							 List<CommandBenchmarkParams> params,
							 IdentityHashMap<CommandBenchmarkParams,
							 Collection<InvocationServiceStats<CommandBenchmarkStats>>> results)
	{
		Set<Member> members = new HashSet<Member>();
		
		int mc = memberCount;
		for(Member m : allMembers)
			if (mc-- > 0)
				members.add(m);
			else
				break;
		
		for(CommandBenchmarkParams benchmarkParams : params)
		{
			sysOut("\tExecuting benchmark with params " + benchmarkParams.toString());
			
			CommandBenchmarkDispatcher dispatcher = new CommandBenchmarkDispatcher(members, facade);
			
			InvocationServiceStats<CommandBenchmarkStats> res = dispatcher.execute(benchmarkParams);
			
			results.get(benchmarkParams).add(res);
			
			csvWriter.writeRow(avalues + ";" + memberCount + ";JavaMS;" + benchmarkParams.toCSVRow() + ";" + res.getJavaMsStats().toCSVRow());
			csvWriter.writeRow(avalues + ";" + memberCount + ";JavaNS;" + benchmarkParams.toCSVRow() + ";" + res.getJavaNsStats().toCSVRow());
			csvWriter.writeRow(avalues + ";" + memberCount + ";CoherenceMS;" + benchmarkParams.toCSVRow() + ";" + res.getCoherenceMsStats().toCSVRow());
		}
	}
	
	public void calculateSecondPhasebenchmarkParams(int memberCount)
	{
		secondPhasebenchmarkParams.clear();
		
		for (CommandBenchmarkParams bp : firstPhaseBenchmarkParams)
		{
			Collection<InvocationServiceStats<CommandBenchmarkStats>> res = firstPhaseResults.get(bp);
			
			double th = 0.0d;
			for (InvocationServiceStats<CommandBenchmarkStats> st : res)
			{
				th += st.getCoherenceMsStats().throughput;
			}
			th = ((th / stageCount) / memberCount) * throughputScale; //TODO may be different calculation
			
			CommandBenchmarkParams new_bp = new CommandBenchmarkParams(bp);
			
			new_bp.setOpsPerSec((int)th);
			secondPhasebenchmarkParams.add(new_bp);
		}
		
		secondPhaseResults = new IdentityHashMap<CommandBenchmarkParams, Collection<InvocationServiceStats<CommandBenchmarkStats>>>();
		for (CommandBenchmarkParams bp : secondPhasebenchmarkParams)
			secondPhaseResults.put(bp, new ArrayList<InvocationServiceStats<CommandBenchmarkStats>>(stageCount));
	}
	
	public static void main(String[] args)
	{
		(new MatrixRunCommandBenchmark()).run(args);
	}
}
