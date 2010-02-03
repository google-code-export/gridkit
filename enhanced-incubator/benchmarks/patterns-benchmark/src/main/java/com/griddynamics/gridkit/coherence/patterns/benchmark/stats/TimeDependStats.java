package com.griddynamics.gridkit.coherence.patterns.benchmark.stats;

import java.util.EnumMap;
import java.util.Map;

public class TimeDependStats<T>
{
	static enum TimeMeasure {JavaMS, JavaNS, CoherenceMS};
	
	protected Map<TimeMeasure, T> results = new EnumMap<TimeMeasure, T>(TimeMeasure.class);
	
	public T getJavaMsStats()
	{
		return results.get(TimeMeasure.JavaMS);
	}
	
	public void setJavaMsStats(T javaMsStats)
	{
		results.put(TimeMeasure.JavaMS, javaMsStats);
	}
	
	public T getJavaNsStats()
	{
		return results.get(TimeMeasure.JavaNS);
	}
	
	public void setJavaNsStats(T javaNsStats)
	{
		results.put(TimeMeasure.JavaNS, javaNsStats);
	}
	
	public T getCoherenceMsStats()
	{
		return results.get(TimeMeasure.CoherenceMS);
	}
	
	public void setCoherenceMsStats(T coherenceMsStats)
	{
		results.put(TimeMeasure.CoherenceMS, coherenceMsStats);
	}
}
