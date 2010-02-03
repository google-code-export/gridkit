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
package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.io.Serializable;

public final class CommandBenchmarkParams implements Serializable
{
	private static final long serialVersionUID = -680095570543015774L;

	private final String command;
	
	private final int threadCount;
	private final int commandPerThread;
	private final int contextCount;
	private final int opsPerSec;
	
	public CommandBenchmarkParams(String command, int threadCount,  int commandPerThread,
												  int contextCount, int opsPerSec)
	{
		this.command          = command;
		this.threadCount      = threadCount;
		this.commandPerThread = commandPerThread;
		this.contextCount     = contextCount;
		this.opsPerSec        = opsPerSec;
	}

	public String getCommand()
	{
		return command;
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
				+ ((command == null) ? 0 : command.hashCode());
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
		if (command == null) {
			if (other.command != null) {
				return false;
			}
		} else if (!command.equals(other.command)) {
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
				+ ", command=" + command + ", threadCount=" + threadCount
				+ "]";
	}
	
	public String toCSVRow()
	{
		return command + ";" +
		   threadCount + ";" +
	  commandPerThread + ";" +
	   	  contextCount + ";" + 
	   		 opsPerSec;
	}
	
	public static String getCSVHeader()
	{
		return "Command;ThreadCount;CommandPerThread;ContextCount;OpsPerSec";
	}
}
