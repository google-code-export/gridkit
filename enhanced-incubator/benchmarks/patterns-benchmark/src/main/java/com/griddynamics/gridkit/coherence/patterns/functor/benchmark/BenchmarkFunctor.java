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

import java.io.IOException;
import java.io.Serializable;

import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.griddynamics.gridkit.coherence.patterns.benchmark.TimeStamp;
import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.CommandExecutionMark;
import com.oracle.coherence.patterns.functor.Functor;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public abstract class BenchmarkFunctor implements Functor<SimpleContext, CommandExecutionMark>,
												  Serializable, PortableObject
{
	private static final long serialVersionUID = 1361296643827196673L;

	protected long      executionID;
	protected String    taskHeader = "some random text to increase functor size";
	protected TimeStamp sendTS;
	
	protected transient CommandExecutionMark executionMark;
	
	public BenchmarkFunctor()
	{
		// For POF
	}
	
	public BenchmarkFunctor(long executionID)
	{
		this.executionID = executionID;
		this.sendTS    = null;
	}
	
	public BenchmarkFunctor send()
	{
		sendTS = TimeStamp.getCurrentTimeStamp();
		return this;
	}

	protected final void startExecution()
	{
		executionMark = new CommandExecutionMark(executionID, sendTS);
		executionMark.execute();
	}
	
	protected final CommandExecutionMark finishExecution()
	{
		executionMark.finish();
		return executionMark;
	}
	
	protected int getNextPOFParam()
	{
		return 3;
	}
	
	@Override
	public void readExternal(PofReader in) throws IOException
	{
		int propID = 0;
		executionID = in.readLong(propID++);
		sendTS      = (TimeStamp)in.readObject(propID++);
		taskHeader  = in.readString(propID++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		int propID = 0;
		out.writeLong(propID++, executionID);
		out.writeObject(propID++, sendTS);
		out.writeString(propID++, taskHeader);
	}
}
