package com.griddynamics.gridkit.coherence.patterns.message.benchmark.topic;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.io.Serializable;
import java.util.ArrayList;
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
import com.tangosol.util.WrapperException;

public class TopicBenchmarkWorker implements Invocable, Serializable
{
	private static final long serialVersionUID = -5536673473796824919L;

	private final TopicBenchmarkParams params;
	
	private final List<Identifier> allTopics;
	private final Map<Member,List<Identifier>> allReceiveTopics;
	
	private transient Member localMember;
	
	private transient PatternFacade facade;
	
	private transient List<Identifier> sendTopics;
	private transient List<Identifier> receiveTopics;
	
	private transient ConcurrentLinkedQueue<MessageExecutionMark> workerResult;
	
	private transient SpeedLimit senderSpeedLimit;
	
	private transient CyclicBarrier startBarrier;
	private transient CountDownLatch finishLatch;
	
	private transient int poisonPillsToReceive;
	private transient AtomicInteger poisonPillsReceived;
	
	//TODO private final static long startWaitTimeOut = 10;
	
	public TopicBenchmarkWorker(TopicBenchmarkParams params,
								List<Identifier> allTopics,
								Map<Member, List<Identifier>> allReceiveTopics)
	{
		this.params           = params;
		this.allTopics        = allTopics;
		this.allReceiveTopics = allReceiveTopics;
	}

	@Override
	public void run()
	{
		try
		{
			facade = PatternFacade.DefaultFacade.getInstance();
			
			workerResult = new ConcurrentLinkedQueue<MessageExecutionMark>();
			
			localMember = CacheFactory.getCluster().getLocalMember();
			
			receiveTopics = allReceiveTopics.get(localMember);
			
			sendTopics = new ArrayList<Identifier>(allTopics);
			sendTopics.removeAll(receiveTopics);
			
			senderSpeedLimit = SpeedLimit.SpeedLimitHelper.getSpeedLimit(params.getSenderSpeedLimit());
			
			startBarrier = new CyclicBarrier(receiveTopics.size() * params.getReceiverThreadsCount() + params.getSenderThreadsCount());
			finishLatch  = new CountDownLatch(params.getSenderThreadsCount() + 1);
			
			poisonPillsReceived  = new AtomicInteger(0); // last factor is member count in benchmark
			poisonPillsToReceive = receiveTopics.size() * params.getSenderThreadsCount() * params.getReceiverThreadsCount() * allReceiveTopics.size();
			
			ExecutorService sendService    = Executors.newFixedThreadPool(params.getSenderThreadsCount());
			ExecutorService receiveService = Executors.newFixedThreadPool(receiveTopics.size() * params.getReceiverThreadsCount());
			
			for(int i = 0; i < params.getSenderThreadsCount(); ++i)
			{
				sendService.submit(new Sender(i));
			}
			
			for (int i = 0; i < receiveTopics.size(); ++i)
			{
				for (int t = 0; t < params.getReceiverThreadsCount(); ++t)
				{
					System.out.println("Sending reciver " + (i+t));
					receiveService.submit(new Receiver(i));
				}
			}
			
			finishLatch.await();
			
			sendService.shutdown();
			receiveService.shutdownNow();
		}
		catch (Throwable t)
		{
			sysOut("-------- Exception on TopicBenchmarkWorker.run() --------");
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
					
					facade.publishMessage(sendTopics.get(rnd.nextInt(sendTopics.size())), (new BenchmarkMessage(id + messagesCount)).send());
				}
				
				//Sending poison pills
				for (Identifier t : allTopics)
				{
					facade.publishMessage(t, (new BenchmarkMessage(0, true)).send());
				}
				
				finishLatch.countDown();
			}
			catch (Throwable t)
			{
				sysOut("-------- Exception on TopicBenchmarkWorker.Sender.call() --------");
				t.printStackTrace();
				System.exit(1);
			}
			
			return null;
		}
	}
	
	private final class Receiver implements Callable<Void>
	{
		private final Subscriber subscriber;
		private final int topicID;
		
		public Receiver(int topicID)
		{
			this.topicID    = topicID;
			this.subscriber = facade.subscribe(receiveTopics.get(this.topicID));
		}

		@Override
		public Void call() throws Exception
		{
			try
			{
				//TODO maybe pause is needed to wait other members to subscribe
				startBarrier.await();		
				
				while (true)
				{	
					BenchmarkMessage m = (BenchmarkMessage)subscriber.getMessage();
					
					workerResult.add(m.receive());
					
					if (m.isPoisonPill())
					{
						int i = poisonPillsReceived.incrementAndGet();
						
						System.out.println("Recived pill " + i + " needed " + poisonPillsToReceive);
						
						if (i == poisonPillsToReceive)
						{
							finishLatch.countDown();
							return null;
						}
					}
				}
			}
			catch (WrapperException allMessagesReceived)
			{
				sysOut("1+++++++ Exiting on TopicBenchmarkWorker.Receiver.call(). All messages received. ++++++++");
			}
			catch (SubscriberInterruptedException allMessagesReceived)
			{
				sysOut("2+++++++ Exiting on TopicBenchmarkWorker.Receiver.call(). All messages received. ++++++++");
			}
			catch (Throwable t)
			{
				sysOut(t.getClass().toString());
				sysOut("-------- Exception on TopicBenchmarkWorker.Receiver.call() --------");
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
