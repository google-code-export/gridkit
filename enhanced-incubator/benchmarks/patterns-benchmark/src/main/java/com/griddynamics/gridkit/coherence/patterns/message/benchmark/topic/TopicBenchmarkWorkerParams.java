package com.griddynamics.gridkit.coherence.patterns.message.benchmark.topic;

import java.io.Serializable;

public class TopicBenchmarkWorkerParams implements Serializable
{
	private static final long serialVersionUID = 3376885905977831480L;
	
	private final int senderThreadsCount;
	private final int receiverThreadsCount;
	
	private final int senderSpeedLimit;
	
	private final int messagesPerThread;
	
	public TopicBenchmarkWorkerParams(int senderThreadsCount, int receiverThreadsCount,
									  int messagesPerThread, int senderSpeedLimit)
	{
		this.senderThreadsCount   = senderThreadsCount;
		this.receiverThreadsCount = receiverThreadsCount;
		
		this.senderSpeedLimit     = senderSpeedLimit;
		
		this.messagesPerThread    = messagesPerThread;
	}

	public int getSenderThreadsCount()
	{
		return senderThreadsCount;
	}

	public int getReceiverThreadsCount()
	{
		return receiverThreadsCount;
	}

	public int getSenderSpeedLimit()
	{
		return senderSpeedLimit;
	}

	public int getMessagesPerThread()
	{
		return messagesPerThread;
	}
}
