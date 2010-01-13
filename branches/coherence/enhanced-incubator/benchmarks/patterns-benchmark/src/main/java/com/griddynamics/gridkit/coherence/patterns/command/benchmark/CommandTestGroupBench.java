/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class CommandTestGroupBench
{	
	public static void main(String[] args)
	{
		new CommandTestGroupBench().start(args);
	}
	
	public static List<CommandBenchmarkParams> prepareBenchmarkParams()
	{
		/*--------- Benchmark Configuration ---------*/
		int opsPerSec = 0;
		
		int[] threadCount      = {3,4}; //{1, 2, 3, 5, 10}
		int[] contextCount     = {5, 10}; //{1, 2, 4, 8, 16}
		int[] commandPerThread = {5000};
		
		String[] taskTypes = {"update"};
		/*-------------------------------------------*/
		
		int benchmarksCount = threadCount.length * contextCount.length * commandPerThread.length * taskTypes.length;
		
		List<CommandBenchmarkParams> res = new ArrayList<CommandBenchmarkParams>(benchmarksCount);
		
		for(int tc = 0; tc < threadCount.length; ++tc)
		{
			for(int cc = 0; cc < contextCount.length; ++cc)
			{
				for(int cpt = 0; cpt < commandPerThread.length; ++cpt)
				{
					for(int tt = 0; tt < taskTypes.length; ++tt)
					{
						res.add(new CommandBenchmarkParams
								(
									taskTypes[tt],
									threadCount[tc],
									commandPerThread[cpt],
									contextCount[cc],
									opsPerSec
								));
					}
				}
			}
		}
		
		return res;
	}
	
	public static Map<CommandBenchmarkParams, BenchmarkResults> executeBenchmark(PatternFacade facade, Collection<CommandBenchmarkParams> params)
	{
		Map<CommandBenchmarkParams, BenchmarkResults> res = new LinkedHashMap<CommandBenchmarkParams, BenchmarkResults>();
		
		for(CommandBenchmarkParams param : params)
		{
			TestHelper.sysout("Executing benchmark for " + param.toString());
			
			CommandBenchmark commandBenchmark = new CommandBenchmark(param, "command-benchmark");
			
			BenchmarkResults benchmarkResults = commandBenchmark.execute(facade);
			
			res.put(param, benchmarkResults);
		}
		
		return res;
	}
	
	public static void warmUp(final PatternFacade facade)
	{
		CommandBenchmarkParams benchmarkParams = new CommandBenchmarkParams("empty", // commandType
													 							  4, // threadCount
													 						   1000, // commandPerThread,
													 						   	  4, // contextCount
													 						   	  0);// opsPerSec
		
		TestHelper.sysout("Warming up ...");
		
		CommandBenchmark commandBenchmark = new CommandBenchmark(benchmarkParams, "warmup");
		
		for(int n = 0; n != 20; ++n)
		{
			commandBenchmark.execute(facade);
			LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(500));
		}
		
		LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
	}

	public static final double throughputScale = 0.7;
	
	static private Map<CommandBenchmarkParams, BenchmarkResults> makeBenchmarkExecutionStage(int start, PatternFacade facade,
																							 List<CommandBenchmarkParams> benchmarkParams,
																							 List<StatHelper.StatsCSVRow> resToCSV)
	{
		Map<CommandBenchmarkParams, BenchmarkResults> res = executeBenchmark(facade, benchmarkParams);
		
		for (Map.Entry<CommandBenchmarkParams, BenchmarkResults> r : res.entrySet())
		{
			++start;
			
			resToCSV.add(new StatHelper.StatsCSVRow(start,r.getKey(),r.getValue().javaMsResults,      TimeUnit.MILLISECONDS));
			resToCSV.add(new StatHelper.StatsCSVRow(start,r.getKey(),r.getValue().javaNsResults,      TimeUnit.NANOSECONDS));
			resToCSV.add(new StatHelper.StatsCSVRow(start,r.getKey(),r.getValue().coherenceMsResults, TimeUnit.DAYS));
		}
		
		return res;
	}
	
	public void start(String[] args)
	{
		TestHelper.setSysProp("tangosol.pof.config", "benchmark-pof-config.xml");
		TestHelper.setSysProp("tangosol.coherence.cacheconfig", "benchmark-pof-cache-config.xml");
		TestHelper.setSysProp("tangosol.coherence.clusterport", "9001");
		TestHelper.setSysProp("tangosol.coherence.distributed.localstorage", "false");
		
		final PatternFacade facade = PatternFacade.Helper.create();
		
		warmUp(facade);
		
		List<CommandBenchmarkParams> benchmarkParams = prepareBenchmarkParams();
		//TODO add coherence time
		List<StatHelper.StatsCSVRow> resToCSV = new ArrayList<StatHelper.StatsCSVRow>(9 * benchmarkParams.size());
		
		int i = 0;
		
		TestHelper.sysout("Benchmark. Stage I.");
		Map<CommandBenchmarkParams, BenchmarkResults> res1 = makeBenchmarkExecutionStage(i,facade, benchmarkParams, resToCSV);
		
		Collections.shuffle(benchmarkParams);
		
		i += benchmarkParams.size();
		
		TestHelper.sysout("Benchmark. Stage II.");
		Map<CommandBenchmarkParams, BenchmarkResults> res2 = makeBenchmarkExecutionStage(i,facade, benchmarkParams, resToCSV);
		
		//Calculating average throughput
		
		List<CommandBenchmarkParams> speedLimitBenchmarkParams = new ArrayList<CommandBenchmarkParams>(benchmarkParams.size());
		
		for (CommandBenchmarkParams p : benchmarkParams)
		{
			BenchmarkResults r1 = res1.get(p);
			BenchmarkResults r2 = res2.get(p);
			
			//TODO ask from type of type get 
			
			speedLimitBenchmarkParams.add(new CommandBenchmarkParams(p.getTaskType(),
																	 p.getThreadCount(),
																	 p.getCommandPerThread(),
																	 p.getContextCount(),
										  (int)((r1.javaMsResults.getThroughput() + r2.javaMsResults.getThroughput()) / 2 * throughputScale)));
		}
		
		i += benchmarkParams.size();
		
		TestHelper.sysout("Benchmark. Stage III.");
		Map<CommandBenchmarkParams, BenchmarkResults> res3 = makeBenchmarkExecutionStage(i, facade, speedLimitBenchmarkParams, resToCSV);
		
		Collections.shuffle(speedLimitBenchmarkParams);
		
		i += benchmarkParams.size();
		
		TestHelper.sysout("Benchmark. Stage IV.");
		Map<CommandBenchmarkParams, BenchmarkResults> res4 = makeBenchmarkExecutionStage(i, facade, speedLimitBenchmarkParams, resToCSV);
		
		StatHelper.storeResultsInCSV("./command_benchmark_result.csv", resToCSV);
	}
}
