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
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.griddynamics.gridkit.coherence.patterns.benchmark.TimeStamp;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

//TODO merge with other ExecutionMarks
public class MessageExecutionMark implements Serializable, PortableObject
{
	private static final long serialVersionUID = -5927816782287453309L;

	protected long executionID;
	
	protected TimeStamp    sendTS;
	protected TimeStamp receiveTS;
	
	public MessageExecutionMark()
	{
		// for POF
	}
	
	public MessageExecutionMark(long executionID, TimeStamp sendTS, TimeStamp receiveTS)
	{
		this.executionID = executionID;
		this.sendTS      = sendTS;
		this.receiveTS   = receiveTS;
	}
	
	public TimeStamp getSendTS()
	{
		return sendTS;
	}

	public TimeStamp getReceiveTS()
	{
		return receiveTS;
	}
	
	public long getExecutionID()
	{
		return executionID;
	}
	
	protected int getNextPOFParam()
	{
		return 3;
	}
	
	@Override
	public void readExternal(PofReader in) throws IOException
	{
		int propID       = 0;
		this.executionID = in.readLong(propID++);
		this.sendTS      = (TimeStamp)in.readObject(propID++);
		this.receiveTS   = (TimeStamp)in.readObject(propID++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		int propID = 0;
		out.writeLong(propID++,   executionID);
		out.writeObject(propID++, sendTS);
		out.writeObject(propID++, receiveTS);
	}
	
	static public interface MessageExecutionMarkTimeExtractor
	{
		public double getSendTime(MessageExecutionMark ts);
		public double getReceiveTime(MessageExecutionMark ts);
	}
	
	static public class JavaMsExtractor implements MessageExecutionMarkTimeExtractor
	{
		public double getSendTime(MessageExecutionMark ts)    {return ts.sendTS.getJavaMs();};
		public double getReceiveTime(MessageExecutionMark ts) {return ts.receiveTS.getJavaMs();};
	}
	
	static public class CoherenceMsExtractor implements MessageExecutionMarkTimeExtractor
	{
		public double getSendTime(MessageExecutionMark ts)    {return ts.sendTS.getCoherenceMs();};
		public double getReceiveTime(MessageExecutionMark ts) {return ts.receiveTS.getCoherenceMs();};
	}
	
	static public class JavaNsExtractor implements MessageExecutionMarkTimeExtractor
	{
		static final long NStoMS = TimeUnit.MILLISECONDS.toNanos(1);
		
		public double getSendTime(MessageExecutionMark ts)    {return ts.sendTS.getJavaNs() / NStoMS;};
		public double getReceiveTime(MessageExecutionMark ts) {return ts.receiveTS.getJavaNs() / NStoMS;};
	}
}
