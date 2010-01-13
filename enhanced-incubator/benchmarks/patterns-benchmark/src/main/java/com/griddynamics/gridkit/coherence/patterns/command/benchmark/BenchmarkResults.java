package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

final class BenchmarkResults
{
	public BenchmarkResults(BenchmarkStats msResults, BenchmarkStats nsResults, BenchmarkStats coherenceMsResults)
	{
		this.javaMsResults      = msResults;
		this.javaNsResults      = nsResults;
		this.coherenceMsResults = coherenceMsResults;
	}
	
	public final BenchmarkStats javaMsResults;
	public final BenchmarkStats javaNsResults;
	public final BenchmarkStats coherenceMsResults;
}
