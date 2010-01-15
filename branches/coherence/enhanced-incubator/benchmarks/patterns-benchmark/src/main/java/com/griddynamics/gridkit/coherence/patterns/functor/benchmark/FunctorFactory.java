package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

public interface FunctorFactory
{
	public BenchmarkFunctor createFunctor(long executionID);
	
	public static class TouchFactory implements FunctorFactory
	{
		@Override
		public BenchmarkFunctor createFunctor(long executionID)
		{
			return new TouchFunctor(executionID);
		}
	}
}
