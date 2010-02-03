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
package com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark;

import java.io.IOException;

import com.griddynamics.gridkit.coherence.patterns.benchmark.TimeStamp;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;

public class FunctorExecutionMark extends CommandExecutionMark
{
	private static final long serialVersionUID = -7974356243435142775L;
	
	protected TimeStamp returnTS;
	
	public FunctorExecutionMark()
	{
		// For POF
	}
	
	public FunctorExecutionMark(CommandExecutionMark commandMark)
	{
		this.executionID = commandMark.executionID;
		
		this.sendTS    = commandMark.sendTS;
		this.receiveTS = commandMark.receiveTS;
		this.finishTS  = commandMark.finishTS;
		this.returnTS  = null;
	}
	
	public TimeStamp getReturnTS()
	{
		return returnTS;
	}

	public void returN()
	{
		returnTS = TimeStamp.getCurrentTimeStamp();
	}
	
	protected int getNextPOFParam()
	{
		return super.getNextPOFParam() + 1;
	}

	@Override
	public void readExternal(PofReader in) throws IOException
	{
		super.readExternal(in);
		int propID = super.getNextPOFParam();
		this.returnTS = (TimeStamp)in.readObject(propID++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		super.writeExternal(out);
		int propID = super.getNextPOFParam();
		out.writeObject(propID++, returnTS);
	}
	
	static public interface FunctorExecutionMarkTimeExtractor extends CommandExecutionMarkTimeExtractor
	{
		public double getReturnTime(FunctorExecutionMark ts);
	}
	
	static public class JavaMsExtractor extends CommandExecutionMark.JavaMsExtractor implements FunctorExecutionMarkTimeExtractor
	{
		public double getReturnTime(FunctorExecutionMark ts) {return ts.returnTS.getJavaMs();};
	}
	
	static public class CoherenceMsExtractor extends CommandExecutionMark.CoherenceMsExtractor implements FunctorExecutionMarkTimeExtractor
	{
		public double getReturnTime(FunctorExecutionMark ts) {return ts.returnTS.getCoherenceMs();};
	}
	
	static public class JavaNsExtractor extends CommandExecutionMark.JavaNsExtractor implements FunctorExecutionMarkTimeExtractor
	{
		public double getReturnTime(FunctorExecutionMark ts) {return ts.returnTS.getJavaNs() / NStoMS;};
	}
}
