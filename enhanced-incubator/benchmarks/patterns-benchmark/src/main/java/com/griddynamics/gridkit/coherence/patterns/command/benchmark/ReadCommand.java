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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ReadCommand extends BenchmarkCommand
{
	private static final long serialVersionUID = 20100105L;

	public ReadCommand()
	{
		// for POF
	}

	public ReadCommand(long execId, String reportBuffer, Map<?, ?> payload)
	{
		super(execId, reportBuffer, payload);
	}
	
	public ReadCommand(long execId, String reportBuffer)
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
		executionEnvironment.getContext();
		// Save time information
		BenchmarkSupport.reportExecution(reportBuffer, (new ExecMark(executionID, timeStamp)).receive());
	}
}
