package com.griddynamics.gridkit.coherence.patterns.benchmark.stats;

public class Accamulator
{
//Algorithms for calculating variance (on-line algorithm)
//From http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
	protected int n;
	
	protected double mean;
	protected double M2;
	
	protected double min;
	protected double max;
	
	protected double delta;
	
	public Accamulator()
	{
		n    = 0;
		mean = M2 = 0.0d;
		min  = Double.MAX_VALUE;
		max  = Double.MIN_VALUE;
	}
	
	public void add(double x)
	{
		if (x < min)
			min = x;
		
		if (x > max)
			max = x;
		
        n     = n + 1;
        delta = x - mean;
        mean  = mean + delta/n;
        M2    = M2 + delta*(x - mean);
	}
	
	public double getMean()
	{
		return mean;
	}
	
	public double getVariance()
	{
		return M2 / n; //TODO read article, may be M2/(n-1)
	}
	
	public double getMin()
	{
		return min;
	}
	
	public double getMax()
	{
		return max;
	}
}
