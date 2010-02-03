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

public class CommandExecutionMark extends MessageExecutionMark
{
	private static final long serialVersionUID = 2771292114161540499L;

	protected TimeStamp finishTS;
	
	public CommandExecutionMark()
	{
		// for POF
	}
	
	public CommandExecutionMark(long executionID, TimeStamp sendTS)
	{
		super(executionID,sendTS,null);
		this.finishTS = null;
	}

	public TimeStamp getFinishTS()
	{
		return finishTS;
	}
	
	public void execute()
	{
		receiveTS = TimeStamp.getCurrentTimeStamp();
	}
	
	public void finish()
	{
		finishTS = TimeStamp.getCurrentTimeStamp();
	}
	
	protected int getNextPOFParam()
	{
		return super.getNextPOFParam() + 1;
	}
	
	@Override
	public void readExternal(PofReader in) throws IOException
	{
		super.readExternal(in);
		int propID    = getNextPOFParam();
		this.finishTS = (TimeStamp)in.readObject(propID++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		super.writeExternal(out);
		int propID = getNextPOFParam();
		out.writeObject(propID++, finishTS);
	}
	
	static public interface CommandExecutionMarkTimeExtractor extends MessageExecutionMarkTimeExtractor
	{
		public double getFinishTime(CommandExecutionMark ts);
	}
	
	static public class JavaMsExtractor extends MessageExecutionMark.JavaMsExtractor implements CommandExecutionMarkTimeExtractor
	{
		public double getFinishTime(CommandExecutionMark ts) {return ts.finishTS.getJavaMs();};
	}
	
	static public class CoherenceMsExtractor extends MessageExecutionMark.CoherenceMsExtractor implements CommandExecutionMarkTimeExtractor
	{
		public double getFinishTime(CommandExecutionMark ts) {return ts.finishTS.getCoherenceMs();};
	}
	
	static public class JavaNsExtractor extends MessageExecutionMark.JavaNsExtractor implements CommandExecutionMarkTimeExtractor
	{
		public double getFinishTime(CommandExecutionMark ts) {return ts.finishTS.getJavaNs() / NStoMS;};
	}
}
