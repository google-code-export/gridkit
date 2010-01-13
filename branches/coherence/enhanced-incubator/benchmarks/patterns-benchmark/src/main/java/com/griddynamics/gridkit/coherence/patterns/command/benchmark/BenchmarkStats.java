package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

public final class BenchmarkStats
{
	private final double totalTime;
	
	private final double throughput;
	
	private final double averageLatency;
	private final double latencyVariance;
	
	private final double maxLatency;
	private final double minLatency;
	
	public BenchmarkStats(double totalTime, double throughput, double averageLatency,
						  double latencyVariance, double maxLatency, double minLatency)
	{
		this.totalTime       = totalTime;
		this.throughput      = throughput;
		this.averageLatency  = averageLatency;
		this.latencyVariance = latencyVariance;
		this.maxLatency      = maxLatency;
		this.minLatency      = minLatency;
	}

	public double getTotalTime()
	{
		return totalTime;
	}

	public double getThroughput()
	{
		return throughput;
	}

	public double getAverageLatency()
	{
		return averageLatency;
	}

	public double getLatencyVariance()
	{
		return latencyVariance;
	}

	public double getMaxLatency()
	{
		return maxLatency;
	}

	public double getMinLatency()
	{
		return minLatency;
	}

	public String toCSVRow() 
	{
		return throughput + ";" + 
	            totalTime + ";" + 
	       averageLatency + ";" +
		  latencyVariance + ";" +
			   maxLatency + ";" +
			   minLatency;
	}

	public static String getCSVHeader()
	{
		return "Throughput;TotalTime;AverageLatency;LatencyVariance;MaxLatency;MinLatency";
	}
	
	@Override
	public String toString() {
		return "BenchmarkStats [averageLatency=" + averageLatency
				+ ", latencyVariance=" + latencyVariance + ", maxLatency="
				+ maxLatency + ", minLatency=" + minLatency + ", throughput="
				+ throughput + ", totalTime=" + totalTime + "]";
	}
}
