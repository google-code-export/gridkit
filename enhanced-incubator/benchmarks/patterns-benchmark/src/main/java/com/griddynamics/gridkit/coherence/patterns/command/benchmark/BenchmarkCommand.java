package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.griddynamics.gridkit.coherence.patterns.benchmark.CommandExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.SimpleContext;
import com.griddynamics.gridkit.coherence.patterns.benchmark.TimeStamp;
import com.oracle.coherence.patterns.command.Command;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public abstract class BenchmarkCommand implements Command<SimpleContext>, PortableObject, Serializable
{
	private static final long serialVersionUID = -4897703270046458791L;
	
	protected long      executionID;
	protected String    reportBuffer;
	protected TimeStamp timeStamp;
	protected String    taskHeader = "some random text to increase task size";
	protected Map<?, ?> payload    = Collections.EMPTY_MAP;
	
	protected transient CommandExecutionMark executionMark;
	
	public BenchmarkCommand()
	{
		// for POF
	}

	public BenchmarkCommand(long executionID, String reportBuffer, Map<?, ?> payload)
	{
		this(executionID, reportBuffer);
		this.payload = payload;
	}
	
	public BenchmarkCommand(long executionID, String reportBuffer)
	{
		this.executionID  = executionID;
		this.reportBuffer = reportBuffer;
		this.timeStamp    = null;
	}
	
	public BenchmarkCommand send()
	{
		this.timeStamp = TimeStamp.getCurrentTimeStamp();
		return this;
	}
	
	public void startExecution()
	{
		executionMark = new CommandExecutionMark(executionID, timeStamp);
		executionMark.execute();
	}
	
	public void finishExecution()
	{
		executionMark.finish();
		BenchmarkSupport.reportExecution(reportBuffer, executionMark);
	}
	
	protected int getNextPOFParam()
	{
		return 5;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(PofReader in) throws IOException {
		int propId = 0;
		executionID  = in.readLong(propId++);
		timeStamp    = (TimeStamp)in.readObject(propId++);
		reportBuffer = in.readString(propId++);
		taskHeader   = in.readString(propId++);
		payload      = in.readMap(propId++, new HashMap());
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		int propId = 0;
		out.writeLong(propId++, executionID);
		out.writeObject(propId++, timeStamp);
		out.writeString(propId++, reportBuffer);
		out.writeString(propId++, taskHeader);
		out.writeMap(propId++, payload);
	}
}
