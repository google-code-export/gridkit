/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
