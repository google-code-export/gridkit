/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
