package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.io.Serializable;

public final class CommandBenchmarkParams implements Serializable
{
	private static final long serialVersionUID = -680095570543015774L;

	private final String taskType;
	
	private final int threadCount;
	private final int commandPerThread;
	private final int contextCount;
	private final int opsPerSec;
	
	public CommandBenchmarkParams(String taskType, int threadCount,  int commandPerThread,
												   int contextCount, int opsPerSec)
	{
		this.taskType         = taskType;
		
		this.threadCount      = threadCount;
		this.commandPerThread = commandPerThread;
		this.contextCount     = contextCount;
		this.opsPerSec        = opsPerSec;
	}

	public String getTaskType()
	{
		return taskType;
	}

	public int getThreadCount()
	{
		return threadCount;
	}

	public int getCommandPerThread()
	{
		return commandPerThread;
	}

	public int getContextCount()
	{
		return contextCount;
	}

	public int getOpsPerSec()
	{
		return opsPerSec;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + commandPerThread;
		result = prime * result + contextCount;
		result = prime * result + opsPerSec;
		result = prime * result
				+ ((taskType == null) ? 0 : taskType.hashCode());
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
		if (!(obj instanceof CommandBenchmarkParams)) {
			return false;
		}
		CommandBenchmarkParams other = (CommandBenchmarkParams) obj;
		if (commandPerThread != other.commandPerThread) {
			return false;
		}
		if (contextCount != other.contextCount) {
			return false;
		}
		if (opsPerSec != other.opsPerSec) {
			return false;
		}
		if (taskType == null) {
			if (other.taskType != null) {
				return false;
			}
		} else if (!taskType.equals(other.taskType)) {
			return false;
		}
		if (threadCount != other.threadCount) {
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "CommandBenchmarkParams [commandPerThread=" + commandPerThread
				+ ", contextCount=" + contextCount + ", opsPerSec=" + opsPerSec
				+ ", taskType=" + taskType + ", threadCount=" + threadCount
				+ "]";
	}
	
	public String toCSVRow()
	{
		return taskType + ";" +
			threadCount + ";" +
	   commandPerThread + ";" +
	   	   contextCount + ";" + 
	   		  opsPerSec;
	}
	
	public static String getCSVHeader()
	{
		return "TaskType;ThreadCount;CommandPerThread;ContextCount;OpsPerSec";
	}
}
