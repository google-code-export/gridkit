package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;

public class FunctorBenchmarkStats extends InvocationServiceStats
{
	public FunctorBenchmarkStats()
	{
		coherenceMsStats  = javaNsStats   = javaMsStats = null;
	}
	
	static public class TimeUnitDependStats
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
			return "TimeUnitDependStats \n[\n\taverageReturnLatency="
					+ averageReturnLatency + "\n\taverageSumbitLatency="
					+ averageSumbitLatency + "\n\tmaxReturnLatency="
					+ maxReturnLatency + "\n\tmaxSumbitLatency="
					+ maxSumbitLatency + "\n\tminReturnLatency="
					+ minReturnLatency + "\n\tminSumbitLatency="
					+ minSumbitLatency + "\n\treturnLatencyVariance="
					+ returnLatencyVariance + "\n\tsumbitLatencyVariance="
					+ sumbitLatencyVariance + "\n\tthroughput=" + throughput
					+ "\n\ttotalTime=" + totalTime + "\n]";
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
