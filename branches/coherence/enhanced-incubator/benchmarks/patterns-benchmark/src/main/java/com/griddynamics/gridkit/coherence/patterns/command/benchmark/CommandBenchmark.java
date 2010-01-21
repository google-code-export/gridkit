package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.griddynamics.gridkit.coherence.patterns.benchmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SpeedLimit;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.Accamulator;
import com.oracle.coherence.common.identifiers.Identifier;
import com.tangosol.net.CacheFactory;

public class CommandBenchmark
{
	private final String reportBuffer;
	
	public CommandBenchmark(String reportBuffer)
	{
		this.reportBuffer = reportBuffer;
	}

	public CommandBenchmarkStats execute(PatternFacade facade, CommandBenchmarkParams params)
	{
		Map<Long, CommandExecutionMark> stats = executeInternal(facade, params);
		CommandBenchmarkStats res = new CommandBenchmarkStats();
		
		res.setJavaMsStats(calculateExecutionStatisticsInternal(stats, new CommandExecutionMark.JavaMsExtractor()));
		
		res.setJavaNsStats(calculateExecutionStatisticsInternal(stats, new CommandExecutionMark.JavaNsExtractor()));
		
		res.setCoherenceMsStats(calculateExecutionStatisticsInternal(stats, new CommandExecutionMark.CoherenceMsExtractor()));

		return res;
	}
	
	@SuppressWarnings("unchecked")
	private Map<Long, CommandExecutionMark> executeInternal(final PatternFacade facade, final CommandBenchmarkParams params)
	{
		Map<Long, CommandExecutionMark> res = null;
		
		try
		{
			final Random rnd = new Random(System.currentTimeMillis());
			
			final Identifier[] contexts = new Identifier[params.getContextCount()];
			
			CacheFactory.getCache(reportBuffer).clear();
			
			final CommandFactory commandFactory = getCommandFactory(params.getCommand());
			
			SpeedLimit sl = null;
			if (params.getOpsPerSec() > 0) 
			{
				sl = SpeedLimit.createSpeedLimit(params.getOpsPerSec());
			}
			final SpeedLimit speedLimit = sl;
			
			for(int i = 0; i != params.getContextCount(); ++i)
			{
				contexts[i] = facade.registerContext("ctx-" + i, new SimpleContext("ctx-" + i));
			}
			
			ExecutorService service = params.getThreadCount() == 1 ? Executors.newSingleThreadExecutor() 
									  : Executors.newFixedThreadPool(params.getThreadCount());
			
			final CountDownLatch startLatch  = new CountDownLatch(params.getThreadCount());
			
			List<Callable<Void>> workers = new ArrayList<Callable<Void>>(params.getThreadCount());
			
			for(int t = 0; t != params.getThreadCount(); ++t)
			{
				final int threadNumber = t;
				
				//TODO wrap to catch Throwable
				Callable<Void> worker = new Callable<Void>()
				{
					@Override
					public Void call() throws Exception 
					{
						startLatch.countDown();
						startLatch.await();
						
						for(int c = 0; c != params.getCommandPerThread(); ++c)
						{
							Identifier ctx = contexts[rnd.nextInt(contexts.length)];
							long id = threadNumber * 10000000 + c;
							
							if (speedLimit != null)
							{
								speedLimit.accure();
							}
	
							facade.submit(ctx, commandFactory.createCommand(id, reportBuffer).send());
						}
						
						return null;
					}
				};
				
				workers.add(worker);
			}
			
			service.invokeAll(workers);
			
			service.shutdownNow();
	
			res = (Map<Long, CommandExecutionMark>)BenchmarkSupport.waitForBuffer(reportBuffer, params.getThreadCount() * params.getCommandPerThread() * commandFactory.getMarksPerCommand());
		}
		catch (Throwable t)
		{
			sysOut("-------- Exception on CommandBenchmark.executeInternal(...) --------");
			t.printStackTrace();
			System.exit(1);
		}
		
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
	
	public static CommandBenchmarkStats.TimeUnitDependStats calculateExecutionStatisticsInternal(
					Map<Long, CommandExecutionMark> stats, CommandExecutionMark.CommandExecutionMarkTimeExtractor ex)
	{	
		Accamulator     latency = new Accamulator();
		
		Accamulator    sendTime = new Accamulator();
		Accamulator receiveTime = new Accamulator();
		
		int n = 0;
		
		for(CommandExecutionMark m : stats.values())
		{
			n++;
			
			sendTime.add(ex.getSendTime(m));
			receiveTime.add(ex.getReceiveTime(m));
			
			latency.add(ex.getReceiveTime(m) - ex.getSendTime(m));
		}
		
		CommandBenchmarkStats.TimeUnitDependStats res = new CommandBenchmarkStats.TimeUnitDependStats();
		
		res.totalTime  = (receiveTime.getMax() - sendTime.getMin()) / TimeUnit.SECONDS.toMillis(1);
		res.throughput = n / res.totalTime;
		
		res.averageLatency  = latency.getMean();
		res.latencyVariance = latency.getVariance();
		res.minLatency      = latency.getMin();
		res.maxLatency      = latency.getMax();
		
		return res;
	}
}
























