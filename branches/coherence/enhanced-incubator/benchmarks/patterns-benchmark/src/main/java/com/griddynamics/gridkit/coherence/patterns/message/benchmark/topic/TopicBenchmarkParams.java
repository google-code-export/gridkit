package com.griddynamics.gridkit.coherence.patterns.message.benchmark.topic;

import java.io.Serializable;

import com.griddynamics.gridkit.coherence.patterns.benchmark.BenchmarkParams;

public class TopicBenchmarkParams implements Serializable, BenchmarkParams
{
	private static final long serialVersionUID = 3376885905977831480L;
	
	//------------ Worker parameters ------------//
	private int senderThreadsCount;
	private int receiverThreadsCount;
	
	private int senderSpeedLimit;
	
	private int messagesPerThread;
	//-------------------------------------------//
	
	//---------- Dispatcher parameters ----------//
	private int topicsCount;
	private int topicsPerMember;
	//-------------------------------------------//
	
	public int getSenderThreadsCount()
	{
		return senderThreadsCount;
	}
	
	public void setSenderThreadsCount(int senderThreadsCount)
	{
		this.senderThreadsCount = senderThreadsCount;
	}
	
	public int getReceiverThreadsCount()
	{
		return receiverThreadsCount;
	}
	
	public void setReceiverThreadsCount(int receiverThreadsCount)
	{
		this.receiverThreadsCount = receiverThreadsCount;
	}
	
	public int getSenderSpeedLimit()
	{
		return senderSpeedLimit;
	}
	
	public void setSenderSpeedLimit(int senderSpeedLimit)
	{
		this.senderSpeedLimit = senderSpeedLimit;
	}
	
	public int getMessagesPerThread()
	{
		return messagesPerThread;
	}
	
	public void setMessagesPerThread(int messagesPerThread)
	{
		this.messagesPerThread = messagesPerThread;
	}
	
	public int getTopicsCount()
	{
		return topicsCount;
	}
	
	public void setTopicsCount(int topicsCount)
	{
		this.topicsCount = topicsCount;
	}
	
	public int getTopicsPerMember()
	{
		return topicsPerMember;
	}
	
	public void setTopicsPerMember(int topicsPerMember)
	{
		this.topicsPerMember = topicsPerMember;
	}
}
