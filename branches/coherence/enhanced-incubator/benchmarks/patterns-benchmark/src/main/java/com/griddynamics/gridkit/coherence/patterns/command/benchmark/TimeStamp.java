package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;

public final class TimeStamp implements PortableObject
{
	long javaMs;
	long javaNs;
	long coherenceMs;
	
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
