package com.griddynamics.gridkit.coherence.patterns.benchmark;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

//TODO merge with other ExecutionMarks
public class MessageExecutionMark implements Serializable, PortableObject
{
	private static final long serialVersionUID = -5927816782287453309L;

	protected long executionID;
	
	protected TimeStamp    sendTS;
	protected TimeStamp receiveTS;
	
	public MessageExecutionMark()
	{
		// for POF
	}
	
	public MessageExecutionMark(long executionID, TimeStamp sendTS, TimeStamp receiveTS)
	{
		this.executionID = executionID;
		this.sendTS      = sendTS;
		this.receiveTS   = receiveTS;
	}
	
	public TimeStamp getSendTS()
	{
		return sendTS;
	}

	public TimeStamp getReceiveTS()
	{
		return receiveTS;
	}
	
	public long getExecutionID()
	{
		return executionID;
	}
	
	protected int getNextPOFParam()
	{
		return 3;
	}
	
	@Override
	public void readExternal(PofReader in) throws IOException
	{
		int propID       = 0;
		this.executionID = in.readLong(propID++);
		this.sendTS      = (TimeStamp)in.readObject(propID++);
		this.receiveTS   = (TimeStamp)in.readObject(propID++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		int propID = 0;
		out.writeLong(propID++,   executionID);
		out.writeObject(propID++, sendTS);
		out.writeObject(propID++, receiveTS);
	}
	
	static public interface MessageExecutionMarkTimeExtractor
	{
		public double getSendTime(MessageExecutionMark ts);
		public double getReceiveTime(MessageExecutionMark ts);
	}
	
	static public class JavaMsExtractor implements MessageExecutionMarkTimeExtractor
	{
		public double getSendTime(MessageExecutionMark ts)    {return ts.sendTS.getJavaMs();};
		public double getReceiveTime(MessageExecutionMark ts) {return ts.receiveTS.getJavaMs();};
	}
	
	static public class CoherenceMsExtractor implements MessageExecutionMarkTimeExtractor
	{
		public double getSendTime(MessageExecutionMark ts)    {return ts.sendTS.getCoherenceMs();};
		public double getReceiveTime(MessageExecutionMark ts) {return ts.receiveTS.getCoherenceMs();};
	}
	
	static public class JavaNsExtractor implements MessageExecutionMarkTimeExtractor
	{
		static final long NStoMS = TimeUnit.MILLISECONDS.toNanos(1);
		
		public double getSendTime(MessageExecutionMark ts)    {return ts.sendTS.getJavaNs() / NStoMS;};
		public double getReceiveTime(MessageExecutionMark ts) {return ts.receiveTS.getJavaNs() / NStoMS;};
	}
}