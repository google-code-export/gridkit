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

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.tangosol.net.Member;

public class SingleRunCommandBenchmark
{
	public static void warmUp(PatternFacade facade, Set<Member> members)
	{
		CommandBenchmarkParams benchmarkParams = new CommandBenchmarkParams();
		
        benchmarkParams.setCommand("update");
        benchmarkParams.setThreadCount(4);
        benchmarkParams.setCommandPerThread(2500);
        benchmarkParams.setOpsPerSec(10000);
        
        benchmarkParams.setReportBuffer("warmup");
        benchmarkParams.setContextCount(25);
		
		sysOut("Warming up ...");
		
		CommandBenchmarkDispatcher dispatcher = new CommandBenchmarkDispatcher(members, facade);
		
		setSysProp("benchmark.command.warmUpCount", "5");
		int warmUpCount = Integer.getInteger("benchmark.command.warmUpCount");
		
		for(int n = 1; n <= warmUpCount; ++n)
		{
			sysOut("Warming up (stage " + n + " from " + warmUpCount + ")");
			dispatcher.execute(benchmarkParams);
			LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
		}
		
		LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
	}
	
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
	}
}
