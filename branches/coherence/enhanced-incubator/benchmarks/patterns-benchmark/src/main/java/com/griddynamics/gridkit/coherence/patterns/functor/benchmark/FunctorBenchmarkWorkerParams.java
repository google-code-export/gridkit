package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import java.io.Serializable;

public final class FunctorBenchmarkWorkerParams implements Serializable
{
	private static final long serialVersionUID = 7824359508250443358L;

	private final String functorType;
	
	private final int threadCount;
	private final int invocationPerThread;
	private final int opsPerSec;
	
	public FunctorBenchmarkWorkerParams(String functorType, int threadCount,  int invocationPerThread, int opsPerSec)
	{
		this.functorType         = functorType;
		
		this.threadCount         = threadCount;
		this.invocationPerThread = invocationPerThread;
		this.opsPerSec           = opsPerSec;
	}

	public String getFunctorType()
	{
		return functorType;
	}

	public int getThreadCount()
	{
		return threadCount;
	}

	public int getInvocationPerThread()
	{
		return invocationPerThread;
	}

	public int getOpsPerSec()
	{
		return opsPerSec;
	}

	@Override
	public String toString()
	{
		return "FunctorBenchmarkWorkerParams [commandPerThread="
				+ invocationPerThread + ", functorType=" + functorType
				+ ", opsPerSec=" + opsPerSec + ", threadCount=" + threadCount
				+ "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + invocationPerThread;
		result = prime * result
				+ ((functorType == null) ? 0 : functorType.hashCode());
		result = prime * result + opsPerSec;
		result = prime * result + threadCount;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof FunctorBenchmarkWorkerParams)) {
			return false;
		}
		FunctorBenchmarkWorkerParams other = (FunctorBenchmarkWorkerParams) obj;
		if (invocationPerThread != other.invocationPerThread) {
			return false;
		}
		if (functorType == null) {
			if (other.functorType != null) {
				return false;
			}
		} else if (!functorType.equals(other.functorType)) {
			return false;
		}
		if (opsPerSec != other.opsPerSec) {
			return false;
		}
		if (threadCount != other.threadCount) {
			return false;
		}
		return true;
	}
}
