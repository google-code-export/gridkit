package com.griddynamics.gridkit.coherence.patterns.message.benchmark.queue;

import java.io.Serializable;

import com.griddynamics.gridkit.coherence.patterns.benchmark.BenchmarkParams;

public final class QueueBenchmarkParams implements Serializable, BenchmarkParams
{
	private static final long serialVersionUID = 6978301353256207171L;
	
	//------------ Worker parameters ------------//
	private int senderThreadsCount;
	private int receiverThreadsCount;
	
	private int senderSpeedLimit;
	private int receiverSpeedLimit;
	
	private int messagesPerThread;
	//-------------------------------------------//
	
	//---------- Dispatcher parameters ----------//
	private int queuesCount;
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

	public int getReceiverSpeedLimit()
	{
		return receiverSpeedLimit;
	}

	public void setReceiverSpeedLimit(int receiverSpeedLimit)
	{
		this.receiverSpeedLimit = receiverSpeedLimit;
	}

	public int getMessagesPerThread()
	{
		return messagesPerThread;
	}

	public void setMessagesPerThread(int messagesPerThread)
	{
		this.messagesPerThread = messagesPerThread;
	}

	public int getQueuesCount()
	{
		return queuesCount;
	}

	public void setQueuesCount(int queuesCount)
	{
		this.queuesCount = queuesCount;
	}
}
