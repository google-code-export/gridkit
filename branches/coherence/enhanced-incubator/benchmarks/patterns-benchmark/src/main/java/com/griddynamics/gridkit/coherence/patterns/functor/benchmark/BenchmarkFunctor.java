package com.griddynamics.gridkit.coherence.patterns.functor.benchmark;

import java.io.IOException;
import java.io.Serializable;

import com.griddynamics.gridkit.coherence.patterns.benchmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.griddynamics.gridkit.coherence.patterns.benchmark.TimeStamp;
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
