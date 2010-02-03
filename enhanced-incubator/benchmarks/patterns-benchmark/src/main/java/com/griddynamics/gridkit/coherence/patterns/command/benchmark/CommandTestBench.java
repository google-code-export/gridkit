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
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setCoherenceConfig;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class CommandTestBench
{
    public static void main(String[] args)
    {
        new CommandTestBench().start(args);
    }

	public static void warmUp(final PatternFacade facade)
	{
		CommandBenchmarkParams benchmarkParams = new CommandBenchmarkParams("empty", // commandType
													 							  4, // threadCount
													 						   5000, // commandPerThread,
													 						   	  4, // contextCount
													 						   	  0);// opsPerSec
		
		sysOut("Warming up ...");
		
		CommandBenchmark commandBenchmark = new CommandBenchmark("warmup");
		
		for(int n = 0; n != 20; ++n)
		{
			commandBenchmark.execute(facade, benchmarkParams);
			LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
		}
		
		LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
	}
    
    public void start(String[] args)
    {
    	setCoherenceConfig(false);
        
        setSysProp("benchmark.threadCount", "4");
        setSysProp("benchmark.commandPerThread", "1000");
        setSysProp("benchmark.contextCount", "10");
        setSysProp("benchmark.command", "empty");
        setSysProp("benchmark.speedLimit", "0");

        CommandBenchmarkParams params = new CommandBenchmarkParams
											(
												System.getProperty("benchmark.command"),
												Integer.getInteger("benchmark.threadCount"),
												Integer.getInteger("benchmark.commandPerThread"),
												Integer.getInteger("benchmark.contextCount"),
												Integer.getInteger("benchmark.speedLimit")
											);
        
        PatternFacade facade = PatternFacade.Helper.create();

        //warmUp(facade);
        
        sysOut("Starting test ...");
        sysOut("Thread count: %d", params.getThreadCount());
        sysOut("Command count: %d (%d per thread)", params.getThreadCount() * params.getCommandPerThread(), params.getCommandPerThread());
        sysOut("Context count: %d", params.getContextCount());

		CommandBenchmark commandBenchmark = new CommandBenchmark("command-benchmark");
		
		CommandBenchmarkStats benchmarkResults = commandBenchmark.execute(facade, params);
        
		System.out.println();
        sysOut("Done");
        sysOut("Thread count: %d", params.getThreadCount());
        sysOut("Command count: %d (%d per thread)", params.getThreadCount() * params.getCommandPerThread(), params.getCommandPerThread());
        sysOut("Context count: %d", params.getContextCount());
        
        sysOut("----------------Java MS statistics");
        reportStats(benchmarkResults.javaMsStats);
        sysOut("----------------Java NS statistics");
        reportStats(benchmarkResults.javaNsStats);
        sysOut("----------------Coherenc MS statistics");
        reportStats(benchmarkResults.coherenceMsStats);

        //TODO add clean up
        System.exit(0);
    }
    
    public static void reportStats(CommandBenchmarkStats.TimeUnitDependStats benchmarkStats) 
	{
		sysOut("Total time [s]:        %014.12f" , benchmarkStats.totalTime);
		sysOut("Throughput [op/s]:     %014.12f" , benchmarkStats.throughput);
		sysOut("Average latency [ms]:  %014.12f" , benchmarkStats.averageLatency);
		sysOut("Latency variance [ms]: %014.12f" , benchmarkStats.latencyVariance);
		//TestHelper.sysout("Latency stddev /[ms]:   %014.12f" , scale * stdDev);
		sysOut("Max latency [ms]:      %014.12f" , benchmarkStats.maxLatency);
		sysOut("Min latency [ms]:      %014.12f" , benchmarkStats.minLatency);
	}
}
