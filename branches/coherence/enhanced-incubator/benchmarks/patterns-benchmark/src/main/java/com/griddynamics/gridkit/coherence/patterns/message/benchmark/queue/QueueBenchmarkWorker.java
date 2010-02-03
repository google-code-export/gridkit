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

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.griddynamics.gridkit.coherence.patterns.benchmark.executionmark.MessageExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.speedlimit.SpeedLimit;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.BenchmarkMessage;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.PatternFacade;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.messaging.Subscriber;
import com.oracle.coherence.patterns.messaging.exceptions.SubscriberInterruptedException;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Invocable;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;

public final class QueueBenchmarkWorker implements Invocable, Serializable
{
	private static final long serialVersionUID = -3397451888740243886L;
	
	private final QueueBenchmarkParams params;
	
	private final Map<Member,List<Identifier>> sendQueuesMap;
	private final Map<Member,List<Identifier>> receiveQueuesMap;

	public QueueBenchmarkWorker(QueueBenchmarkParams params,
								Map<Member, List<Identifier>> sendQueuesMap,
								Map<Member, List<Identifier>> receiveQueuesMap)
	{
		this.params           = params;
		this.sendQueuesMap    = sendQueuesMap;
		this.receiveQueuesMap = receiveQueuesMap;
	}

	private transient Member localMember;
	
	private transient PatternFacade facade;
	
	private transient Identifier[] sendQueues;
	private transient Identifier[] receiveQueues;
	
	private transient ConcurrentLinkedQueue<MessageExecutionMark> workerResult;
	
	private transient SpeedLimit senderSpeedLimit;
	private transient SpeedLimit receiverSpeedLimit;
	
	private transient CyclicBarrier startBarrier;
	private transient CountDownLatch finishLatch;
	
	private transient AtomicInteger messagesReceived;
	
	@Override
	public void run()
	{
		try
		{
			facade = PatternFacade.DefaultFacade.getInstance();
			
			workerResult = new ConcurrentLinkedQueue<MessageExecutionMark>();
			
			localMember = CacheFactory.getCluster().getLocalMember();
	
			   sendQueues = sendQueuesMap.get(localMember).toArray(new Identifier[0]);
			receiveQueues = receiveQueuesMap.get(localMember).toArray(new Identifier[0]);
			
			senderSpeedLimit   = SpeedLimit.SpeedLimitHelper.getSpeedLimit(params.getSenderSpeedLimit());
			receiverSpeedLimit = SpeedLimit.SpeedLimitHelper.getSpeedLimit(params.getReceiverSpeedLimit());
			
			//Latch for count sends and receives
			startBarrier = new CyclicBarrier(params.getSenderThreadsCount() + receiveQueues.length * params.getReceiverThreadsCount());
			finishLatch  = new CountDownLatch(params.getSenderThreadsCount() + 1);
			
			ExecutorService sendService    = Executors.newFixedThreadPool(params.getSenderThreadsCount());
			ExecutorService receiveService = Executors.newFixedThreadPool(receiveQueues.length * params.getReceiverThreadsCount());
			
			for(int i = 0; i < params.getSenderThreadsCount(); ++i)
			{
				sendService.submit(new Sender(i));
			}
			
			messagesReceived = new AtomicInteger(0);
			
			for (int i = 0; i < receiveQueues.length; ++i)
			{
				for (int t = 0; t < params.getReceiverThreadsCount(); ++t)
					receiveService.submit(new Receiver(i));
			}
			
			finishLatch.await();
			
			sendService.shutdown();
			receiveService.shutdownNow();
		}
		catch (Throwable t)
		{
			sysOut("-------- Exception on QueueBenchmarkWorker.run() --------");
			t.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public Object getResult()
	{
		return workerResult.toArray(new MessageExecutionMark[0]);
	}
	
	private final class Sender implements Callable<Void>
	{
		private final long id;
		
		public Sender(long id) { this.id = id * 1000000; }
		
		@Override
		public Void call() throws Exception
		{
			try
			{
				startBarrier.await();
				
				Random rnd = new Random(System.currentTimeMillis());
				
				int messagesCount = params.getMessagesPerThread();
				
				while (messagesCount-- > 0)
				{
					senderSpeedLimit.accure();
					
					facade.publishMessage(sendQueues[rnd.nextInt(sendQueues.length)], (new BenchmarkMessage(id + messagesCount)).send());
				}
				
				finishLatch.countDown();
			}
			catch (Throwable t)
			{
				sysOut("-------- Exception on QueueBenchmarkWorker.Sender.call() --------");
				t.printStackTrace();
				System.exit(1);
			}
			
			return null;
		}
	}
	
	private final class Receiver implements Callable<Void>
	{
		private final Subscriber subscriber;
		private final int queueID;
		
		public Receiver(int queueID)
		{
			this.queueID    = queueID;
			this.subscriber = facade.subscribe(receiveQueues[this.queueID]); 
		}

		@Override
		public Void call() throws Exception
		{
			try
			{
				startBarrier.await();

				while (true)
				{
					receiverSpeedLimit.accure();
					
					workerResult.add(((BenchmarkMessage)subscriber.getMessage()).receive());

					if (messagesReceived.incrementAndGet() == params.getMessagesPerThread() * params.getSenderThreadsCount())
					{
						finishLatch.countDown();
						return null;
					}
				}
			}
			catch (SubscriberInterruptedException allMessagesReceived)
			{
				sysOut("++++++++ Exiting on QueueBenchmarkWorker.Receiver.call(). All messages received. ++++++++");
			}
			catch (Throwable t)
			{
				sysOut("-------- Exception on QueueBenchmarkWorker.Receiver.call() --------");
				t.printStackTrace();
				System.exit(1);
			}
			
			return null;
		}
	}
	
	@Override
	public void init(InvocationService paramInvocationService)
	{
		
	}
}
