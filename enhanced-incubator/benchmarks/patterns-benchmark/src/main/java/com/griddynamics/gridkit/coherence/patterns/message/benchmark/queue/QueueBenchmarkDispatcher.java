package com.griddynamics.gridkit.coherence.patterns.message.benchmark.queue;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.griddynamics.gridkit.coherence.patterns.benchmark.MessageExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.Accamulator;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.MessageBenchmarkStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.PatternFacade;
import com.oracle.coherence.common.identifiers.Identifier;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.Member;

public class QueueBenchmarkDispatcher
{
	private Map<Member,List<Identifier>> sendQueuesMap;
	private Map<Member,List<Identifier>> receiveQueuesMap;
	
	protected final Object memorySynchronizer = new Object();
	protected CountDownLatch latch;
	protected MessageBenchmarkStats dispatcherResult;
	protected List<List<MessageExecutionMark>> workersResult;
	
	private final int queuesCount;
	
	public QueueBenchmarkDispatcher(int queuesCount)
	{
		this.queuesCount = queuesCount;
	}
	
	public MessageBenchmarkStats execute(PatternFacade facade, List<Member> members, QueueBenchmarkWorkerParams params)
	{
		try
		{
			synchronized (memorySynchronizer)
			{
				latch            = new CountDownLatch(members.size());
				dispatcherResult = new MessageBenchmarkStats();
				workersResult    = new ArrayList<List<MessageExecutionMark>>();
			}
			
			sendQueuesMap    = new HashMap<Member, List<Identifier>>();
			receiveQueuesMap = new HashMap<Member, List<Identifier>>();
			
			//Link all members in a ring
			for (int i = 1; i <= members.size(); ++i)
			{
				int sender   = i - 1;
				int receiver = i % members.size();
		
				List<Identifier> queues = new ArrayList<Identifier>(queuesCount);
				
				for(int q = 0; q < queuesCount; ++q)
				{
					Identifier queue = facade.createQueue("queue_from_" + sender + "_to_" + receiver + "_N_" + q);
					
					queues.add(queue);
				}
				
				sendQueuesMap.put(members.get(sender), queues);
				receiveQueuesMap.put(members.get(receiver), queues);
			}
			
			facade.getInvocationService().execute(new QueueBenchmarkWorker(params, sendQueuesMap, receiveQueuesMap),
												  new HashSet<Member>(members), new QueueBenchmarkObserver());
			
			latch.await();
			
			//Guaranteed by latch.await()
			calculateExecutionStatistics();
		}
		catch (Throwable t)
		{
			sysOut("-------- Exception on QueueBenchmarkDispatcher.execute(...) --------");
			t.printStackTrace();
			System.exit(1);
		}
		
		//TODO add clean up
		
		return dispatcherResult;
	}
	
	class QueueBenchmarkObserver implements InvocationObserver
	{
		@Override
		public void memberCompleted(Member member, Object oResult)
		{
			synchronized (memorySynchronizer)
			{
				workersResult.add(Arrays.asList((MessageExecutionMark[])oResult));
				dispatcherResult.setMembersCompleated(dispatcherResult.getMembersCompleated()+1);
				latch.countDown();
			}
		}

		@Override
		public void memberFailed(Member member, Throwable eFailure)
		{
			synchronized (memorySynchronizer)
			{			
				dispatcherResult.setMembersFailed(dispatcherResult.getMembersFailed()+1);
				latch.countDown();
			}
		}

		@Override
		public void memberLeft(Member member)
		{
			synchronized (memorySynchronizer)
			{			
				dispatcherResult.setMembersLeft(dispatcherResult.getMembersLeft()+1);
				latch.countDown();
			}
		}
		
		@Override
		public void invocationCompleted()
		{

		}
	}
	
	protected void calculateExecutionStatistics()
	{
		dispatcherResult.setJavaMsStats(calculateExecutionStatisticsInternal(new MessageExecutionMark.JavaMsExtractor()));
		
		dispatcherResult.setJavaNsStats(calculateExecutionStatisticsInternal(new MessageExecutionMark.JavaNsExtractor()));
		
		dispatcherResult.setCoherenceMsStats(calculateExecutionStatisticsInternal(new MessageExecutionMark.CoherenceMsExtractor()));
	}
	
	protected MessageBenchmarkStats.TimeUnitDependStats calculateExecutionStatisticsInternal
														(MessageExecutionMark.MessageExecutionMarkTimeExtractor te)
	{
		Accamulator     latency = new Accamulator();
		
		Accamulator    sendTime = new Accamulator();
		Accamulator receiveTime = new Accamulator();
		
		int n = 0;
		
		for (List<MessageExecutionMark> l : workersResult)
		{
			for(MessageExecutionMark m : l)
			{
				n++;
				
				sendTime.add(te.getSendTime(m));
				receiveTime.add(te.getReceiveTime(m));
				
				latency.add(te.getReceiveTime(m) - te.getSendTime(m));
			}
		}
		
		MessageBenchmarkStats.TimeUnitDependStats res = new MessageBenchmarkStats.TimeUnitDependStats();
		
		res.totalTime  = (receiveTime.getMax() - sendTime.getMin()) / TimeUnit.SECONDS.toMillis(1);
		res.throughput = n / res.totalTime;
		
		res.averageLatency  = latency.getMean();
		res.latencyVariance = latency.getVariance();
		res.minLatency      = latency.getMin();
		res.maxLatency      = latency.getMax();
		
		return res;
	}
}





























