package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.griddynamics.gridkit.coherence.patterns.command.benchmark.TaskType;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.Command;
import com.tangosol.net.CacheFactory;

public class CommandBenchmark
{
	private final String reportBuffer;
	private final CommandBenchmarkParams params;
	
	public CommandBenchmark(CommandBenchmarkParams params, String reportBuffer)
	{
		this.params       = params;
		this.reportBuffer = reportBuffer;
	}

	public BenchmarkResults execute(PatternFacade facade)
	{
		Map<Long, ExecMark> res = executeInternal(facade);
		
		return new BenchmarkResults(StatHelper.calculateStatJavaMs(res),
									StatHelper.calculateStatJavaNs(res),
									StatHelper.calculateStatCoherenceMs(res));
	}
	
	private Map<Long, ExecMark> executeInternal(final PatternFacade facade)
	{
		Random rnd = new Random();
		
		Identifier[] contexts = new Identifier[params.getContextCount()];
		
		CacheFactory.getCache(reportBuffer).clear();
		
		final TaskType taskType = getTaskTypeByName(params.getTaskType());
		
		SpeedLimit sl = null;
		if (params.getOpsPerSec() > 0) 
		{
			//TODO ask about precision
			sl = SpeedLimit.createSpeedLimit(params.getOpsPerSec() / 10, params.getOpsPerSec());
		}
		final SpeedLimit speedLimit = sl;
		
		for(int i = 0; i != params.getContextCount(); ++i)
		{
			contexts[i] = facade.registerContext("ctx-" + i, new SimpleTestContext("ctx-" + i));
		}
		
		ExecutorService service = params.getThreadCount() == 1 ? Executors.newSingleThreadExecutor() 
								  : Executors.newFixedThreadPool(params.getThreadCount());
		
		for(int i = 0; i != params.getCommandPerThread(); ++i)
		{
			for(int j = 0; j != params.getThreadCount(); ++j)
			{
				final Identifier ctx = contexts[rnd.nextInt(contexts.length)];
				final long id = j * 10000000 + i;
				
				Runnable rn = new Runnable()
				{
					@Override
					public void run()
					{
						if (speedLimit != null)
						{
							speedLimit.accure();
						}
						
						Command<SimpleTestContext> task = taskType.createCommand(id, reportBuffer);
						facade.submit(ctx, task);
					}
				};
				
				service.submit(rn);
			}
		}

		@SuppressWarnings("unchecked")
		Map<Long, ExecMark> stats = BenchmarkSupport.waitForBuffer(reportBuffer, params.getThreadCount() * params.getCommandPerThread() * taskType.getMarksPerTask());
		
		return stats;
	}
	
	public static TaskType getTaskTypeByName(String name)
	{
		String cmdType = name.toLowerCase();
		
		if ("empty".equals(cmdType))
		{
			return new TaskType.EmptyTaskType();
		}
		else if ("read".equals(cmdType))
		{
			return new TaskType.ReadTaskType();
		}
		else if ("update".equals(cmdType))
		{
			return new TaskType.UpdateTaskType();
		}
		else
			throw new RuntimeException("Unknown command type '" + cmdType + "'");
	}
}
