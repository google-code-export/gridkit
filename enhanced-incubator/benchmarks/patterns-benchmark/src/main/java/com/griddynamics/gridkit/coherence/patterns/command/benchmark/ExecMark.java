package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.io.IOException;
import java.io.Serializable;

import com.griddynamics.gridkit.coherence.patterns.benchmark.TimeStamp;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class ExecMark implements Serializable, PortableObject
{
	private static final long serialVersionUID = 20100105L;
	
	long execId;
	TimeStamp submitTS;
	TimeStamp  execTS;
	
	public ExecMark() {
		// for POF
	}
	
	public ExecMark(long execId, TimeStamp submitTS)
	{
		this.execId   = execId;
		this.submitTS = submitTS;
		this.execTS   = TimeStamp.getCurrentTimeStamp();
	}

	@Override
	public void readExternal(PofReader in) throws IOException
	{
		int propId    = 0;
		this.execId   = in.readLong(propId++);
		this.submitTS = (TimeStamp)in.readObject(propId++);
		this.execTS   = (TimeStamp)in.readObject(propId++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		int propId = 0;
		out.writeLong(propId++, execId);
		out.writeObject(propId++, submitTS);
		out.writeObject(propId++, execTS);
	}
}
