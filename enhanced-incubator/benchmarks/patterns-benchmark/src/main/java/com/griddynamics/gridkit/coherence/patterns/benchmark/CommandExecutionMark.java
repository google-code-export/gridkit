package com.griddynamics.gridkit.coherence.patterns.benchmark;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class CommandExecutionMark implements Serializable, PortableObject
{
	private static final long serialVersionUID = 2771292114161540499L;

	protected long executionID;
	
	protected TimeStamp  submitTS;
	protected TimeStamp executeTS;
	protected TimeStamp  finishTS;
	
	public CommandExecutionMark()
	{
		// for POF
	}
	
	public CommandExecutionMark(long executionID, TimeStamp submitTS)
	{
		this.executionID = executionID;
		this.submitTS    = submitTS;
		this.executeTS   = null;
		this.finishTS    = null;
	}
	
	public long getExecutionID()
	{
		return executionID;
	}

	public TimeStamp getSubmitTS()
	{
		return submitTS;
	}

	public TimeStamp getExecuteTS()
	{
		return executeTS;
	}
	
	public TimeStamp getFinishTS()
	{
		return finishTS;
	}

	public void submit()
	{
		submitTS = TimeStamp.getCurrentTimeStamp();
	}
	
	public void execute()
	{
		executeTS = TimeStamp.getCurrentTimeStamp();
	}
	
	public void finish()
	{
		finishTS = TimeStamp.getCurrentTimeStamp();
	}
	
	protected int getNextPOFParam()
	{
		return 4;
	}
	
	@Override
	public void readExternal(PofReader in) throws IOException
	{
		int propID       = 0;
		this.executionID = in.readLong(propID++);
		this.submitTS    = (TimeStamp)in.readObject(propID++);
		this.executeTS   = (TimeStamp)in.readObject(propID++);
		this.finishTS    = (TimeStamp)in.readObject(propID++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		int propID = 0;
		out.writeLong(propID++, executionID);
		out.writeObject(propID++, submitTS);
		out.writeObject(propID++, executeTS);
		out.writeObject(propID++, finishTS);
	}
	
	static public interface CommandExecutionMarkTimeExtractor
	{
		public double getSubmitTime(CommandExecutionMark ts);
		public double getExecutionTime(CommandExecutionMark ts);
		public double getFinishTime(CommandExecutionMark ts);
	}
	
	static public class JavaMsExtractor implements CommandExecutionMarkTimeExtractor
	{
		public double getSubmitTime(CommandExecutionMark ts)    {return ts.submitTS.getJavaMs();};
		public double getExecutionTime(CommandExecutionMark ts) {return ts.executeTS.getJavaMs();};
		public double getFinishTime(CommandExecutionMark ts)    {return ts.finishTS.getJavaMs();};
	}
	
	static public class CoherenceMsExtractor implements CommandExecutionMarkTimeExtractor
	{
		public double getSubmitTime(CommandExecutionMark ts)    {return ts.submitTS.getCoherenceMs();};
		public double getExecutionTime(CommandExecutionMark ts) {return ts.executeTS.getCoherenceMs();};
		public double getFinishTime(CommandExecutionMark ts)    {return ts.finishTS.getCoherenceMs();};
	}
	
	static public class JavaNsExtractor implements CommandExecutionMarkTimeExtractor
	{
		static final long NStoMS = TimeUnit.MILLISECONDS.toNanos(1);
		
		public double getSubmitTime(CommandExecutionMark ts)    {return ts.submitTS.getJavaNs()  / NStoMS;};
		public double getExecutionTime(CommandExecutionMark ts) {return ts.executeTS.getJavaNs() / NStoMS;};
		public double getFinishTime(CommandExecutionMark ts)    {return ts.finishTS.getJavaNs()  / NStoMS;};
	}
}
