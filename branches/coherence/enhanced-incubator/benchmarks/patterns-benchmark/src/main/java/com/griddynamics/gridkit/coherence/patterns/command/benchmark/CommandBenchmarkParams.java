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

import com.griddynamics.gridkit.coherence.patterns.benchmark.BenchmarkParams;
import com.griddynamics.gridkit.coherence.patterns.benchmark.csv.CSVRow;

public final class CommandBenchmarkParams implements Serializable, BenchmarkParams, CSVRow
{
	private static final long serialVersionUID = -680095570543015774L;

	public CommandBenchmarkParams() {}
	public CommandBenchmarkParams(CommandBenchmarkParams bp)
	{
		command          = bp.command;
		reportBuffer     = bp.reportBuffer;
		threadCount      = bp.threadCount;
		commandPerThread = bp.commandPerThread;
		opsPerSec        = bp.opsPerSec;
		contextCount     = bp.contextCount;
	}
	
	//------------ Worker parameters ------------//
	private String command;
	private String reportBuffer;
	
	private int threadCount;
	private int commandPerThread;
	private int opsPerSec;
	//-------------------------------------------//
	
	//---------- Dispatcher parameters ----------//
	private int contextCount;
	//-------------------------------------------//

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
	
	public void setCommand(String command)
	{
		this.command = command;
	}

	public void setThreadCount(int threadCount)
	{
		this.threadCount = threadCount;
	}

	public void setCommandPerThread(int commandPerThread)
	{
		this.commandPerThread = commandPerThread;
	}

	public void setOpsPerSec(int opsPerSec)
	{
		this.opsPerSec = opsPerSec;
	}

	public void setContextCount(int contextCount)
	{
		this.contextCount = contextCount;
	}
	
	public String getReportBuffer()
	{
		return reportBuffer;
	}

	public void setReportBuffer(String reportBuffer)
	{
		this.reportBuffer = reportBuffer;
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
