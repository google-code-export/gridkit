package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.CommandExecutionMark;
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
		startExecution();
		
		SimpleContext context = executionEnvironment.getContext();
		context.touch();
		executionEnvironment.setContext(context);
		
		return finishExecution();
	}

}
