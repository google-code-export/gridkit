package com.griddynamics.gridkit.coherence.patterns.command.benchmark;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.griddynamics.gridkit.coherence.patterns.benchmark.TimeStamp;
import com.oracle.coherence.patterns.command.Command;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public abstract class BenchmarkCommand implements Command<SimpleTestContext>, PortableObject
{
	protected long      execId;
	protected String    reportBuffer;
	protected TimeStamp timeStamp;
	protected String    taskHeader = "some random text to increase task size";
	protected Map<?, ?> taskPayload = Collections.EMPTY_MAP;
	
	public BenchmarkCommand()
	{
		// for POF
	}

	public BenchmarkCommand(long execId, String reportBuffer, Map<?, ?> payload)
	{
		this(execId, reportBuffer);
		this.taskPayload = payload;
	}
	
	public BenchmarkCommand(long execId, String reportBuffer)
	{
		this.execId       = execId;
		this.reportBuffer = reportBuffer;
		this.timeStamp    = TimeStamp.getCurrentTimeStamp();
	}
	
	protected int POFNextParam()
	{
		return 5;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(PofReader in) throws IOException {
		int propId = 0;
		execId = in.readLong(propId++);
		timeStamp = (TimeStamp)in.readObject(propId++);
		reportBuffer = in.readString(propId++);
		taskHeader = in.readString(propId++);
		taskPayload = in.readMap(propId++, new HashMap());
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		int propId = 0;
		out.writeLong(propId++, execId);
		out.writeObject(propId++, timeStamp);
		out.writeString(propId++, reportBuffer);
		out.writeString(propId++, taskHeader);
		out.writeMap(propId++, taskPayload);
	}
}
