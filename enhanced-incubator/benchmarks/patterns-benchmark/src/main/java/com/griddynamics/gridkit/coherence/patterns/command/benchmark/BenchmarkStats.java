package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

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
		return CSVHelper.formatDoubleToCSV(throughput) + ";" + 
			   CSVHelper.formatDoubleToCSV(totalTime) + ";" + 
			   CSVHelper.formatDoubleToCSV(averageLatency) + ";" +
			   CSVHelper.formatDoubleToCSV(latencyVariance) + ";" +
			   CSVHelper.formatDoubleToCSV(maxLatency) + ";" +
			   CSVHelper.formatDoubleToCSV(minLatency);
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
