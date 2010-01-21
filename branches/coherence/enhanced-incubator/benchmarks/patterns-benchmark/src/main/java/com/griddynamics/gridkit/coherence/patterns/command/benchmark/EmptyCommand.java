/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
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

import java.util.Map;

import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class EmptyCommand extends BenchmarkCommand
{
	private static final long serialVersionUID = 20100105L;

	public EmptyCommand()
	{
		// for POF
	}

	public EmptyCommand(long execId, String reportBuffer, Map<?, ?> payload)
	{
		super(execId, reportBuffer, payload);
	}
	
	public EmptyCommand(long execId, String reportBuffer)
	{
		super(execId, reportBuffer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(ExecutionEnvironment<SimpleContext> executionEnvironment)
	{
		// Invoke execution method.
		execute();
		// Save time information
		BenchmarkSupport.reportExecution(reportBuffer, (new ExecMark(executionID, timeStamp)).receive());
	}

	private void execute()
	{
		// TODO: insert some code
	}
}
