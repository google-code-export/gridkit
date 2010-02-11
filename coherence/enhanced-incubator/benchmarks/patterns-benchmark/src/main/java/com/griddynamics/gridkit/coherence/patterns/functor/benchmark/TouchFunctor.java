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
package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.CommandExecutionMark;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;

public class TouchFunctor extends BenchmarkFunctor
{
	private static final long serialVersionUID = 8465156891248807008L;

	public TouchFunctor()
	{
		// For POF
	}
	
	public TouchFunctor(long executionID)
	{
		super(executionID);
	}
	
	@Override
	public CommandExecutionMark execute(ExecutionEnvironment<SimpleContext> executionEnvironment)
	{
		startExecution();
		
		SimpleContext context = executionEnvironment.getContext();
		context.touch();
		executionEnvironment.setContext(context);
		
		return finishExecution();
	}

}
