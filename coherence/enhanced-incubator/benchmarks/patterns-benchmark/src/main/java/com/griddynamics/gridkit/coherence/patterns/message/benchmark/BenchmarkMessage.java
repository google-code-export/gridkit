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
package com.griddynamics.gridkit.coherence.patterns.message.benchmark;

import java.io.IOException;
import java.io.Serializable;

import com.griddynamics.gridkit.coherence.patterns.benchmark.TimeStamp;
import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.MessageExecutionMark;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class BenchmarkMessage implements Serializable, PortableObject
{
	private static final long serialVersionUID = 2502554412116453378L;

	protected long messageID;
	
	protected String messageHeader = "some random text to increase message size";
	
	protected TimeStamp sendTS;
	
	protected boolean isPoisonPill;
	
	public BenchmarkMessage()
	{
		// For POF
	}
	
	public BenchmarkMessage(long messageID)
	{
		this(messageID, false);
	}
	
	public BenchmarkMessage(long messageID, boolean isPoisonPill)
	{
		this.messageID    = messageID;
		this.sendTS       = null;
		this.isPoisonPill = isPoisonPill;
	}

	public TimeStamp getSendTS()
	{
		return sendTS;
	}

	public BenchmarkMessage send()
	{
		sendTS = TimeStamp.getCurrentTimeStamp();
		return this;
	}
	
	public MessageExecutionMark receive()
	{
		return new MessageExecutionMark(messageID, sendTS, TimeStamp.getCurrentTimeStamp());
	}
	
	public long getMessageID()
	{
		return messageID;
	}

	public boolean isPoisonPill()
	{
		return isPoisonPill;
	}

	protected int getNextPOFParam()
	{
		return 3;
	}
	
	@Override
	public void readExternal(PofReader in) throws IOException
	{
		int propID = 0;
		messageID     = in.readLong(propID++);
		messageHeader = in.readString(propID++);
		sendTS        = (TimeStamp)in.readObject(propID++);
		isPoisonPill  = in.readBoolean(propID++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		int propID = 0;
		out.writeLong(propID++,   messageID);
		out.writeString(propID++, messageHeader);
		out.writeObject(propID++, sendTS);
		out.writeBoolean(propID++, isPoisonPill);
	}
}

