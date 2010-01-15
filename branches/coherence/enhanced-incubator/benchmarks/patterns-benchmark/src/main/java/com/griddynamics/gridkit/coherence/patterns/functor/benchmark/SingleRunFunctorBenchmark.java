package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import com.griddynamics.gridkit.coherence.patterns.command.benchmark.TestHelper;

public class SingleRunFunctorBenchmark
{
	public static void main(String[] args)
	{
		TestHelper.setSysProp("benchmark.threadCount", "4");
	}
}
