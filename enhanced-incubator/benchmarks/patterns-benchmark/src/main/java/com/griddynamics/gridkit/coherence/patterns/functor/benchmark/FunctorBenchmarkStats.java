package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

public class FunctorBenchmarkStats
{
	public double totalTime;
	public double throughput;
		
	public double averageSumbitLatency;
	public double sumbitLatencyVariance;
		
	public double maxSumbitLatency;
	public double minSumbitLatency;
	
	public double averageReturnLatency;
	public double returnLatencyVariance;
		
	public double maxReturnLatency;
	public double minReturnLatency;
	
	@Override
	public String toString()
	{
		return "FunctorBenchmarkStats [averageReturnLatency="
				+ averageReturnLatency + ", averageSumbitLatency="
				+ averageSumbitLatency + ", maxReturnLatency="
				+ maxReturnLatency + ", maxSumbitLatency=" + maxSumbitLatency
				+ ", minReturnLatency=" + minReturnLatency
				+ ", minSumbitLatency=" + minSumbitLatency
				+ ", returnLatencyVariance=" + returnLatencyVariance
				+ ", sumbitLatencyVariance=" + sumbitLatencyVariance
				+ ", throughput=" + throughput + ", totalTime=" + totalTime
				+ "]";
	}
}
