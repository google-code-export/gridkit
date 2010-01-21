package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

final class CommandBenchmarkStats
{
	public static final class TimeUnitDependStats
	{
		public double totalTime;
		
		public double throughput;
		
		public double averageLatency;
		public double latencyVariance;
		
		public double maxLatency;
		public double minLatency;

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
		public String toString()
		{
			return "BenchmarkStats [averageLatency=" + averageLatency
					+ ", latencyVariance=" + latencyVariance + ", maxLatency="
					+ maxLatency + ", minLatency=" + minLatency + ", throughput="
					+ throughput + ", totalTime=" + totalTime + "]";
		}
	}
	
	public CommandBenchmarkStats()
	{
		this.javaMsStats      = null;
		this.javaNsStats      = null;
		this.coherenceMsStats = null;
	}
	
	protected TimeUnitDependStats javaMsStats;
	protected TimeUnitDependStats javaNsStats;
	protected TimeUnitDependStats coherenceMsStats;
	
	public TimeUnitDependStats getJavaMsStats()
	{
		return javaMsStats;
	}
	
	public void setJavaMsStats(TimeUnitDependStats javaMsStats)
	{
		this.javaMsStats = javaMsStats;
	}
	
	public TimeUnitDependStats getJavaNsStats()
	{
		return javaNsStats;
	}
	
	public void setJavaNsStats(TimeUnitDependStats javaNsStats)
	{
		this.javaNsStats = javaNsStats;
	}
	
	public TimeUnitDependStats getCoherenceMsStats()
	{
		return coherenceMsStats;
	}
	
	public void setCoherenceMsStats(TimeUnitDependStats coherenceMsStats)
	{
		this.coherenceMsStats = coherenceMsStats;
	}
}
