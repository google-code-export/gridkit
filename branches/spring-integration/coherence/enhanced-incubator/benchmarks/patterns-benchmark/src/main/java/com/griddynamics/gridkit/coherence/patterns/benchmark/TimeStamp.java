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

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;

public final class TimeStamp implements Serializable, PortableObject
{
	private static final long serialVersionUID = -3016646724383347163L;
	
	private long javaMs;
	private long javaNs;
	private long coherenceMs;
	
	public TimeStamp()
	{
		//For POF
	}
	
	public static TimeStamp getCurrentTimeStamp()
	{
		TimeStamp res = new TimeStamp();
		
		res.javaMs      = System.currentTimeMillis();
		res.javaNs      = System.nanoTime();
		res.coherenceMs = CacheFactory.getSafeTimeMillis();
		
		return res;
	}
	
	public long getJavaMs()
	{
		return javaMs;
	}

	public long getJavaNs()
	{
		return javaNs;
	}

	public long getCoherenceMs()
	{
		return coherenceMs;
	}

	@Override
	public void readExternal(PofReader in) throws IOException
	{
		int propId  = 0;
		javaMs      = in.readLong(propId++);
		javaNs      = in.readLong(propId++);
		coherenceMs = in.readLong(propId++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		int propId = 0;
		out.writeLong(propId++, javaMs);
		out.writeLong(propId++, javaNs);
		out.writeLong(propId++, coherenceMs);
	}
}
