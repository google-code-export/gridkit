package com.griddynamics.gridkit.coherence.patterns.message.benchmark;

import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;

public class MessageBenchmarkStats extends InvocationServiceStats
{
	public MessageBenchmarkStats()
	{
		super();
		javaMsStats = javaNsStats = coherenceMsStats = null;
	}
	
	static public class TimeUnitDependStats
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
			return "TimeUnitDependStats \n[\n\ttotalTime="
					+ totalTime + "\n\tthroughput="
					+ throughput + "\n\taverageLatency="
					+ averageLatency + "\n\tmaxLatency="
					+ maxLatency + "\n\tminLatency="
					+ minLatency + "\n\tlatencyVariance="
					+ latencyVariance + "\n]";
		}
	}
	
	protected TimeUnitDependStats      javaMsStats;
	protected TimeUnitDependStats      javaNsStats;
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
