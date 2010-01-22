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

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setCoherenceConfig;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
		
		int[] threadCount      = {1, 2, 5, 10, 20};
		int[] contextCount     = {5, 10, 25, 50, 100};
		int[] commandPerThread = {10000};
		
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
	
	public static Map<CommandBenchmarkParams, CommandBenchmarkStats> executeBenchmark(PatternFacade facade, Collection<CommandBenchmarkParams> params)
	{
		Map<CommandBenchmarkParams, CommandBenchmarkStats> res = new LinkedHashMap<CommandBenchmarkParams, CommandBenchmarkStats>();
		
		for(CommandBenchmarkParams param : params)
		{
			sysOut("Executing benchmark for " + param.toString());
			
			CommandBenchmark commandBenchmark = new CommandBenchmark("command-benchmark");
			
			CommandBenchmarkStats benchmarkResults = commandBenchmark.execute(facade, param);
			
			res.put(param, benchmarkResults);
		}
		
		return res;
	}

	public static final double throughputScale = 0.7;
	
	static private Map<CommandBenchmarkParams, CommandBenchmarkStats> makeBenchmarkExecutionStage(int start, PatternFacade facade,
																							 List<CommandBenchmarkParams> benchmarkParams,
																							 List<CSVHelper.StatsCSVRow> resToCSV)
	{
		Map<CommandBenchmarkParams, CommandBenchmarkStats> res = executeBenchmark(facade, benchmarkParams);
		
		for (Map.Entry<CommandBenchmarkParams, CommandBenchmarkStats> r : res.entrySet())
		{
			++start;
			
			resToCSV.add(new CSVHelper.StatsCSVRow(start,r.getKey(),r.getValue().javaMsStats, CSVHelper.StatsCSVRow.TimeMeasuringType.JavaMS));
			resToCSV.add(new CSVHelper.StatsCSVRow(start,r.getKey(),r.getValue().javaNsStats, CSVHelper.StatsCSVRow.TimeMeasuringType.JavaNS));
			resToCSV.add(new CSVHelper.StatsCSVRow(start,r.getKey(),r.getValue().coherenceMsStats, CSVHelper.StatsCSVRow.TimeMeasuringType.CoherenceMS));
		}
		
		return res;
	}
	
	public void start(String[] args)
	{
		setCoherenceConfig();
		setSysProp("tangosol.coherence.distributed.localstorage", "false");
		
		setSysProp("outfile", "out" + System.currentTimeMillis());
		String outfile = System.getProperty("outfile");
		
		final PatternFacade facade = PatternFacade.Helper.create();
		
		CommandTestBench.warmUp(facade);
		
		List<CommandBenchmarkParams> benchmarkParams = prepareBenchmarkParams();
		//TODO add coherence time
		List<CSVHelper.StatsCSVRow> resToCSV = new ArrayList<CSVHelper.StatsCSVRow>(9 * benchmarkParams.size());
		
		int i = 0;
		
		sysOut("Benchmark. Stage I.");
		Map<CommandBenchmarkParams, CommandBenchmarkStats> res1 = makeBenchmarkExecutionStage(i,facade, benchmarkParams, resToCSV);
		
		Collections.shuffle(benchmarkParams);
		i += benchmarkParams.size();
		
		sysOut("Benchmark. Stage II.");
		Map<CommandBenchmarkParams, CommandBenchmarkStats> res2 = makeBenchmarkExecutionStage(i,facade, benchmarkParams, resToCSV);
		
		Collections.shuffle(benchmarkParams);
		i += benchmarkParams.size();
		
		sysOut("Benchmark. Stage III.");
		Map<CommandBenchmarkParams, CommandBenchmarkStats> res3 = makeBenchmarkExecutionStage(i,facade, benchmarkParams, resToCSV);
		
		//Calculating average throughput
		
		List<CommandBenchmarkParams> speedLimitBenchmarkParams = new ArrayList<CommandBenchmarkParams>(benchmarkParams.size());
		
		for (CommandBenchmarkParams p : benchmarkParams)
		{
			CommandBenchmarkStats r1 = res1.get(p);
			CommandBenchmarkStats r2 = res2.get(p);
			CommandBenchmarkStats r3 = res3.get(p);
			
			int opsPerSec = (int)((r1.coherenceMsStats.throughput + r2.coherenceMsStats.throughput + r3.coherenceMsStats.throughput) / 3 * throughputScale);
			
			//TODO ask from type of type get 
			
			speedLimitBenchmarkParams.add(new CommandBenchmarkParams(p.getCommand(),
																	 p.getThreadCount(),
																	 p.getCommandPerThread(),
																	 p.getContextCount(),
																	 opsPerSec));
		}
		
		i += benchmarkParams.size();
		
		sysOut("Benchmark. Stage IV.");
		/* res4 = */ makeBenchmarkExecutionStage(i, facade, speedLimitBenchmarkParams, resToCSV);
		
		Collections.shuffle(speedLimitBenchmarkParams);
		i += benchmarkParams.size();
		
		sysOut("Benchmark. Stage V.");
		/* res5 = */  makeBenchmarkExecutionStage(i, facade, speedLimitBenchmarkParams, resToCSV);
		
		Collections.shuffle(speedLimitBenchmarkParams);
		i += benchmarkParams.size();
		
		sysOut("Benchmark. Stage VI.");
		/* res6 = */ makeBenchmarkExecutionStage(i, facade, speedLimitBenchmarkParams, resToCSV);
		
		CSVHelper.storeResultsInCSV(outfile, resToCSV);
		
		System.exit(0);
	}
}
