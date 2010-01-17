package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import com.griddynamics.gridkit.coherence.patterns.benchmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;

public class TouchFunctor extends BenchmarkFunctor
{
	private static final long serialVersionUID = 8465156891248807008L;

	public TouchFunctor()
	{
		// For POF
	}
	
	public TouchFunctor(long executionID)
	{
		super(executionID);
	}
	
	@Override
	public CommandExecutionMark execute(ExecutionEnvironment<SimpleContext> executionEnvironment)
	{
		CommandExecutionMark res = startExecution();
		
		SimpleContext context = executionEnvironment.getContext();
		context.touch();
		executionEnvironment.setContext(context);
		
		res.finish();
		return res;
	}

}
