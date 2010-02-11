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

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setCoherenceConfig;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.griddynamics.gridkit.coherence.patterns.benchmark.csv.CSVHelper;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;

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
		
		int[] threadCount      = {5};//{1, 2, 5, 10, 20};
		int[] contextCount     = {100};//{5, 10, 25, 50, 100};
		int[] commandPerThread = {5000};//{10000};
		
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
						CommandBenchmarkParams benchmarkParams = new CommandBenchmarkParams();

				        benchmarkParams.setCommand(taskTypes[tt]);
				        benchmarkParams.setThreadCount(threadCount[tc]);
				        benchmarkParams.setCommandPerThread(commandPerThread[cpt]);
				        benchmarkParams.setOpsPerSec(opsPerSec);
				        
				        benchmarkParams.setReportBuffer("command-benchmark");
				        benchmarkParams.setContextCount(contextCount[cc]);
				        
				        res.add(benchmarkParams);
					}
				}
			}
		}
		
		return res;
	}
	
	public static Map<CommandBenchmarkParams, InvocationServiceStats<CommandBenchmarkStats>> executeBenchmark(PatternFacade facade, Collection<CommandBenchmarkParams> params)
	{
		Map<CommandBenchmarkParams, InvocationServiceStats<CommandBenchmarkStats>> res = new LinkedHashMap<CommandBenchmarkParams, InvocationServiceStats<CommandBenchmarkStats>>();
		
		for(CommandBenchmarkParams param : params)
		{
			sysOut("Executing benchmark for " + param.toString());
			
			CommandBenchmark commandBenchmark = new CommandBenchmark();
			
			InvocationServiceStats<CommandBenchmarkStats> benchmarkResults = commandBenchmark.execute(facade, param);
			
			res.put(param, benchmarkResults);
		}
		
		return res;
	}

	public static final double throughputScale = 0.7;
	
	static private Map<CommandBenchmarkParams, InvocationServiceStats<CommandBenchmarkStats>> makeBenchmarkExecutionStage(int start, PatternFacade facade,
																							 List<CommandBenchmarkParams> benchmarkParams,
																							 List<CSVHelper.StatsCSVRow> resToCSV)
	{
		Map<CommandBenchmarkParams, InvocationServiceStats<CommandBenchmarkStats>> res = executeBenchmark(facade, benchmarkParams);
		
		for (Map.Entry<CommandBenchmarkParams, InvocationServiceStats<CommandBenchmarkStats>> r : res.entrySet())
		{
			++start;
			
			resToCSV.add(new CSVHelper.StatsCSVRow(start,r.getKey(),r.getValue().getJavaMsStats(), CSVHelper.StatsCSVRow.TimeMeasuringType.JavaMS));
			resToCSV.add(new CSVHelper.StatsCSVRow(start,r.getKey(),r.getValue().getJavaNsStats(), CSVHelper.StatsCSVRow.TimeMeasuringType.JavaNS));
			resToCSV.add(new CSVHelper.StatsCSVRow(start,r.getKey(),r.getValue().getCoherenceMsStats(), CSVHelper.StatsCSVRow.TimeMeasuringType.CoherenceMS));
		}
		
		return res;
	}
	
	public void start(String[] args)
	{
		setCoherenceConfig(false);
		
		setSysProp("benchmark.command-pattern.gc-in-worker",     "true");
		setSysProp("benchmark.command-pattern.gc-in-dispatcher", "true");
		
		setSysProp("outfile", "out_file_" + System.currentTimeMillis() + ".csv");
		String outfile = System.getProperty("outfile");
		
		final PatternFacade facade = PatternFacade.DefaultFacade.getInstance();
		
		//CommandTestBench.warmUp(facade);
		
		List<CommandBenchmarkParams> benchmarkParams = prepareBenchmarkParams();
		//TODO add coherence time
		List<CSVHelper.StatsCSVRow> resToCSV = new ArrayList<CSVHelper.StatsCSVRow>(9 * benchmarkParams.size());
		
		int i = 0;
		
		sysOut("Benchmark. Stage I.");
		Map<CommandBenchmarkParams, InvocationServiceStats<CommandBenchmarkStats>> res1 = makeBenchmarkExecutionStage(i,facade, benchmarkParams, resToCSV);
		
		Collections.shuffle(benchmarkParams);
		i += benchmarkParams.size();
		
		sysOut("Benchmark. Stage II.");
		Map<CommandBenchmarkParams, InvocationServiceStats<CommandBenchmarkStats>> res2 = makeBenchmarkExecutionStage(i,facade, benchmarkParams, resToCSV);
		
		Collections.shuffle(benchmarkParams);
		i += benchmarkParams.size();
		
		sysOut("Benchmark. Stage III.");
		Map<CommandBenchmarkParams, InvocationServiceStats<CommandBenchmarkStats>> res3 = makeBenchmarkExecutionStage(i,facade, benchmarkParams, resToCSV);
		
		//Calculating average throughput
		
		List<CommandBenchmarkParams> speedLimitBenchmarkParams = new ArrayList<CommandBenchmarkParams>(benchmarkParams.size());
		
		for (CommandBenchmarkParams p : benchmarkParams)
		{
			InvocationServiceStats<CommandBenchmarkStats> r1 = res1.get(p);
			InvocationServiceStats<CommandBenchmarkStats> r2 = res2.get(p);
			InvocationServiceStats<CommandBenchmarkStats> r3 = res3.get(p);
			
			int opsPerSec = (int)((r1.getCoherenceMsStats().throughput + r2.getCoherenceMsStats().throughput + r3.getCoherenceMsStats().throughput) / 3 * throughputScale);
			
			//TODO ask from type of type get 
			CommandBenchmarkParams slbp = new CommandBenchmarkParams();
			
	        slbp.setCommand(p.getCommand());
	        slbp.setThreadCount(p.getThreadCount());
	        slbp.setCommandPerThread(p.getCommandPerThread());
	        slbp.setOpsPerSec(opsPerSec);
	        
	        slbp.setReportBuffer("command-benchmark");
	        slbp.setContextCount(p.getContextCount());
	        
	        speedLimitBenchmarkParams.add(slbp);
		}
		
		i += benchmarkParams.size();
		
		sysOut("Benchmark. Stage IV.");
		/* res4 = */ makeBenchmarkExecutionStage(i, facade, speedLimitBenchmarkParams, resToCSV);
		
		Collections.shuffle(speedLimitBenchmarkParams);
		i += benchmarkParams.size();
		
		sysOut("Benchmark. Stage V.");
		/* res5 = */ makeBenchmarkExecutionStage(i, facade, speedLimitBenchmarkParams, resToCSV);
		
		Collections.shuffle(speedLimitBenchmarkParams);
		i += benchmarkParams.size();
		
		sysOut("Benchmark. Stage VI.");
		/* res6 = */ makeBenchmarkExecutionStage(i, facade, speedLimitBenchmarkParams, resToCSV);
		
		CSVHelper.storeResultsInCSV(outfile, resToCSV);
		
		System.exit(0);
	}
}
