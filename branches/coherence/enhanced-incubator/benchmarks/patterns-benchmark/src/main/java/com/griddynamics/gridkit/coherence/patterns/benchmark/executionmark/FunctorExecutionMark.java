package com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark;

import java.io.IOException;

import com.griddynamics.gridkit.coherence.patterns.benchmark.TimeStamp;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;

public class FunctorExecutionMark extends CommandExecutionMark
{
	private static final long serialVersionUID = -7974356243435142775L;
	
	protected TimeStamp returnTS;
	
	public FunctorExecutionMark()
	{
		// For POF
	}
	
	public FunctorExecutionMark(CommandExecutionMark commandMark)
	{
		this.executionID = commandMark.executionID;
		
		this.sendTS    = commandMark.sendTS;
		this.receiveTS = commandMark.receiveTS;
		this.finishTS  = commandMark.finishTS;
		this.returnTS  = null;
	}
	
	public TimeStamp getReturnTS()
	{
		return returnTS;
	}

	public void returN()
	{
		returnTS = TimeStamp.getCurrentTimeStamp();
	}
	
	protected int getNextPOFParam()
	{
		return super.getNextPOFParam() + 1;
	}

	@Override
	public void readExternal(PofReader in) throws IOException
	{
		super.readExternal(in);
		int propID = super.getNextPOFParam();
		this.returnTS = (TimeStamp)in.readObject(propID++);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException
	{
		super.writeExternal(out);
		int propID = super.getNextPOFParam();
		out.writeObject(propID++, returnTS);
	}
	
	static public interface FunctorExecutionMarkTimeExtractor extends CommandExecutionMarkTimeExtractor
	{
		public double getReturnTime(FunctorExecutionMark ts);
	}
	
	static public class JavaMsExtractor extends CommandExecutionMark.JavaMsExtractor implements FunctorExecutionMarkTimeExtractor
	{
		public double getReturnTime(FunctorExecutionMark ts) {return ts.returnTS.getJavaMs();};
	}
	
	static public class CoherenceMsExtractor extends CommandExecutionMark.CoherenceMsExtractor implements FunctorExecutionMarkTimeExtractor
	{
		public double getReturnTime(FunctorExecutionMark ts) {return ts.returnTS.getCoherenceMs();};
	}
	
	static public class JavaNsExtractor extends CommandExecutionMark.JavaNsExtractor implements FunctorExecutionMarkTimeExtractor
	{
		public double getReturnTime(FunctorExecutionMark ts) {return ts.returnTS.getJavaNs() / NStoMS;};
	}
}
