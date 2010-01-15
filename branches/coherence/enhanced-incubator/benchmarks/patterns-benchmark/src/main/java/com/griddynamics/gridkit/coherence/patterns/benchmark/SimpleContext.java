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
