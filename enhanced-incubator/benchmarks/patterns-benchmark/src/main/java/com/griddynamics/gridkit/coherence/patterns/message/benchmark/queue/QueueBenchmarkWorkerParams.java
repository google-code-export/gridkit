package com.griddynamics.gridkit.coherence.patterns.message.benchmark.queue;

import java.io.Serializable;

public final class QueueBenchmarkWorkerParams implements Serializable
{
	private static final long serialVersionUID = 6978301353256207171L;
	
	private final int senderThreadsCount;
	private final int receiverThreadsCount;
	
	private final int senderSpeedLimit;
	private final int receiverSpeedLimit;
	
	private final int messagesPerThread;
	
	public QueueBenchmarkWorkerParams(int senderThreadsCount, int receiverThreadsCount,
									  int messagesPerThread,
									  int senderSpeedLimit, int receiverSpeedLimit)
	{
		this.senderThreadsCount   = senderThreadsCount;
		this.receiverThreadsCount = receiverThreadsCount;
		
		this.senderSpeedLimit     = senderSpeedLimit;
		this.receiverSpeedLimit   = receiverSpeedLimit;
		
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

	public int getReceiverSpeedLimit()
	{
		return receiverSpeedLimit;
	}

	public int getMessagesPerThread()
	{
		return messagesPerThread;
	}
}
