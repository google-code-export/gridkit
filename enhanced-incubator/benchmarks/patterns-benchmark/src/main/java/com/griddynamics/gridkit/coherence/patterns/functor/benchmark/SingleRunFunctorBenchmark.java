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

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.getOtherInvocationServiceMembers;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setCoherenceConfig;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;
import com.tangosol.net.Member;

public class SingleRunFunctorBenchmark
{
	public static void warmUp(PatternFacade facade, Set<Member> members)
	{	
		FunctorBenchmarkParams benchmarkParams = new FunctorBenchmarkParams();
		
        benchmarkParams.setThreadCount(10);
        benchmarkParams.setFunctorType("touch");
        benchmarkParams.setOpsPerSec(0);
        benchmarkParams.setInvocationPerThread(250);
        
        benchmarkParams.setContextsCount(100);
		
		FunctorBenchmarkDispatcher dispatcher = new FunctorBenchmarkDispatcher(members, facade);
		
		sysOut("Starting warm up ...");
		for (int i = 1; i <= 5; ++i)
		{
			InvocationServiceStats<FunctorBenchmarkStats> res = dispatcher.execute(benchmarkParams);
			sysOut("Run " + i + ": " + res.toString());
			LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
		}
		sysOut("Warm up ended");
	}
	
	//TODO configure invocation service timeout
	public static void main(String[] args)
	{
		setCoherenceConfig();
		setSysProp("tangosol.coherence.distributed.localstorage", "false");	
		
		setSysProp("benchmark.functor.threadCount", "5");
		setSysProp("benchmark.functor.functor", "touch");
		setSysProp("benchmark.functor.opsPerSec", "1000");
        setSysProp("benchmark.functor.invocationPerThread", "250");
        
        setSysProp("benchmark.functor.contextCount", "100");
        
        FunctorBenchmarkParams benchmarkParams = new FunctorBenchmarkParams();
        
        benchmarkParams.setThreadCount(Integer.getInteger("benchmark.functor.threadCount"));
        benchmarkParams.setFunctorType(System.getProperty("benchmark.functor.functor"));
        benchmarkParams.setOpsPerSec(Integer.getInteger("benchmark.functor.opsPerSec"));
        benchmarkParams.setInvocationPerThread(Integer.getInteger("benchmark.functor.invocationPerThread"));
        
        benchmarkParams.setContextsCount(Integer.getInteger("benchmark.functor.contextCount"));
        
        PatternFacade facade = PatternFacade.DefaultFacade.getInstance();
		
        Set<Member> members = getOtherInvocationServiceMembers(facade.getInvocationService());
		
		warmUp(facade, members);
		
		FunctorBenchmarkDispatcher dispatcher = new FunctorBenchmarkDispatcher(members, facade);
		
		sysOut("Starting benchmark up ...");
		InvocationServiceStats<FunctorBenchmarkStats> res = dispatcher.execute(benchmarkParams);
		
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println(res.toString());
		System.out.print("Java MS stats");
		System.out.println(res.getJavaMsStats().toString());
		System.out.print("Java NS stats");
		System.out.println(res.getJavaNsStats().toString());
		System.out.print("Coherence MS stats");
		System.out.println(res.getCoherenceMsStats().toString());
		System.out.println("--------------------------------------------------------------------------------");
	}
}
