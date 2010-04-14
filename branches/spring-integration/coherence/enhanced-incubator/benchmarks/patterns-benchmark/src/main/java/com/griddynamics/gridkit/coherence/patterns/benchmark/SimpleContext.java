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
package com.griddynamics.gridkit.coherence.patterns.benchmark;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

import com.oracle.coherence.patterns.command.Context;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class SimpleContext implements Context, Serializable, PortableObject
{
	private static final long serialVersionUID = -8847401321119125693L;

	private static Random rnd = new Random(System.currentTimeMillis());
	
	protected long   counter;
	protected String contextName;
	
	public SimpleContext()
	{
		// for POF
	}
	
	public SimpleContext(String contextName)
	{
		this.counter     = rnd.nextLong();
		this.contextName = contextName;
	}
	
	public void touch()
	{
		++counter;
	}

	public long getCounter()
	{
		return counter;
	}

	public String getContextName()
	{
		return contextName;
	}

	protected int getNextPOFParam()
	{
		return 2;
	}
	
	@Override
	public void readExternal(PofReader in) throws IOException
	{
		int propID = 0;
		this.contextName = in.readString(propID++);
		this.counter = in.readLong(propID++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		int propID = 0;
		out.writeString(propID++, contextName);
		out.writeLong(propID++, counter);
	}
}
