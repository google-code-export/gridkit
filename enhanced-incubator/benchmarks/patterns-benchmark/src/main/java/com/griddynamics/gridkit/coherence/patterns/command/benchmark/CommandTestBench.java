package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

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
		
		TestHelper.sysout("Warming up ...");
		
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
    	setCoherenceConfig();
        TestHelper.setSysProp("tangosol.coherence.distributed.localstorage", "false");
        
        TestHelper.setSysProp("benchmark.threadCount", "4");
        TestHelper.setSysProp("benchmark.commandPerThread", "1000");
        TestHelper.setSysProp("benchmark.contextCount", "10");
        TestHelper.setSysProp("benchmark.command", "empty");
        TestHelper.setSysProp("benchmark.speedLimit", "0");

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
        
        TestHelper.sysout("Starting test ...");
        TestHelper.sysout("Thread count: %d", params.getThreadCount());
        TestHelper.sysout("Command count: %d (%d per thread)", params.getThreadCount() * params.getCommandPerThread(), params.getCommandPerThread());
        TestHelper.sysout("Context count: %d", params.getContextCount());

		CommandBenchmark commandBenchmark = new CommandBenchmark("command-benchmark");
		
		CommandBenchmarkStats benchmarkResults = commandBenchmark.execute(facade, params);
        
		System.out.println();
        TestHelper.sysout("Done");
        TestHelper.sysout("Thread count: %d", params.getThreadCount());
        TestHelper.sysout("Command count: %d (%d per thread)", params.getThreadCount() * params.getCommandPerThread(), params.getCommandPerThread());
        TestHelper.sysout("Context count: %d", params.getContextCount());
        
        TestHelper.sysout("----------------Java MS statistics");
        reportStats(benchmarkResults.javaMsStats);
        TestHelper.sysout("----------------Java NS statistics");
        reportStats(benchmarkResults.javaNsStats);
        TestHelper.sysout("----------------Coherenc MS statistics");
        reportStats(benchmarkResults.coherenceMsStats);

        //TODO add clean up
        System.exit(0);
    }
    
    public static void reportStats(CommandBenchmarkStats.TimeUnitDependStats benchmarkStats) 
	{
		TestHelper.sysout("Total time [s]:        %014.12f" , benchmarkStats.totalTime);
		TestHelper.sysout("Throughput [op/s]:     %014.12f" , benchmarkStats.throughput);
		TestHelper.sysout("Average latency [ms]:  %014.12f" , benchmarkStats.averageLatency);
		TestHelper.sysout("Latency variance [ms]: %014.12f" , benchmarkStats.latencyVariance);
		//TestHelper.sysout("Latency stddev /[ms]:   %014.12f" , scale * stdDev);
		TestHelper.sysout("Max latency [ms]:      %014.12f" , benchmarkStats.maxLatency);
		TestHelper.sysout("Min latency [ms]:      %014.12f" , benchmarkStats.minLatency);
	}
}
