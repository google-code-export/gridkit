package com.griddynamics.gridkit.coherence.patterns.message.benchmark;

public class MessageBenchmarkSimStats
{
	private double totalTime;
	private double throughput;
	
	private double averageLatency;
	
	private double maxLatency;
	private double minLatency;
	
	private double latencyVariance;

	public MessageBenchmarkSimStats()
	{
		totalTime = 0;  throughput = 0;  averageLatency = 0;
		maxLatency = 0; minLatency = 0; latencyVariance = 0;
	}

	public double getTotalTime()
	{
		return totalTime;
	}

	public void setTotalTime(double totalTime)
	{
		this.totalTime = totalTime;
	}

	public double getThroughput()
	{
		return throughput;
	}

	public void setThroughput(double throughput)
	{
		this.throughput = throughput;
	}

	public double getAverageLatency()
	{
		return averageLatency;
	}

	public void setAverageLatency(double averageLatency)
	{
		this.averageLatency = averageLatency;
	}

	public double getMaxLatency()
	{
		return maxLatency;
	}

	public void setMaxLatency(double maxLatency)
	{
		this.maxLatency = maxLatency;
	}

	public double getMinLatency()
	{
		return minLatency;
	}

	public void setMinLatency(double minLatency)
	{
		this.minLatency = minLatency;
	}

	public double getLatencyVariance()
	{
		return latencyVariance;
	}

	public void setLatencyVariance(double latencyVariance)
	{
		this.latencyVariance = latencyVariance;
	}

	@Override
	public String toString()
	{
		return "MessageBenchmarkSimStats [averageLatency=" + averageLatency
				+ ", latencyVariance=" + latencyVariance + ", maxLatency="
				+ maxLatency + ", minLatency=" + minLatency + ", throughput="
				+ throughput + ", totalTime=" + totalTime + "]";
	}
}
