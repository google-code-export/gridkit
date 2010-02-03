package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import java.io.Serializable;

import com.griddynamics.gridkit.coherence.patterns.benchmark.BenchmarkParams;

public final class FunctorBenchmarkParams implements Serializable, BenchmarkParams
{
	private static final long serialVersionUID = 7824359508250443358L;

	//------------ Worker parameters ------------//
	private String functorType;
	
	private int threadCount;
	private int invocationPerThread;
	private int opsPerSec;
	//-------------------------------------------//
	
	//---------- Dispatcher parameters ----------//
	private int contextsCount;
	//-------------------------------------------//

	public String getFunctorType()
	{
		return functorType;
	}

	public void setFunctorType(String functorType)
	{
		this.functorType = functorType;
	}

	public int getThreadCount()
	{
		return threadCount;
	}

	public void setThreadCount(int threadCount)
	{
		this.threadCount = threadCount;
	}

	public int getInvocationPerThread()
	{
		return invocationPerThread;
	}

	public void setInvocationPerThread(int invocationPerThread)
	{
		this.invocationPerThread = invocationPerThread;
	}

	public int getOpsPerSec()
	{
		return opsPerSec;
	}

	public void setOpsPerSec(int opsPerSec)
	{
		this.opsPerSec = opsPerSec;
	}

	public int getContextsCount()
	{
		return contextsCount;
	}

	public void setContextsCount(int contextsCount)
	{
		this.contextsCount = contextsCount;
	}
}
