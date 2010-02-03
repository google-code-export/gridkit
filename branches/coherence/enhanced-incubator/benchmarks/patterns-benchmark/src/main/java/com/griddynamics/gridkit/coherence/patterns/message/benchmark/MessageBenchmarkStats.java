package com.griddynamics.gridkit.coherence.patterns.message.benchmark;

public class MessageBenchmarkStats
{
	public double totalTime;
	public double throughput;
	
	public double averageLatency;
	
	public double maxLatency;
	public double minLatency;
	
	public double latencyVariance;

	@Override
	public String toString()
	{
		return "MessageBenchmarkSimStats [averageLatency=" + averageLatency
				+ ", latencyVariance=" + latencyVariance + ", maxLatency="
				+ maxLatency + ", minLatency=" + minLatency + ", throughput="
				+ throughput + ", totalTime=" + totalTime + "]";
	}
}
