package com.griddynamics.gridkit.coherence.patterns.message.benchmark.topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.griddynamics.gridkit.coherence.patterns.benchmark.Dispatcher;
import com.griddynamics.gridkit.coherence.patterns.benchmark.MessageExecutionMark;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.Accamulator;
import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.MessageBenchmarkSimStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.PatternFacade;
import com.oracle.coherence.common.identifiers.Identifier;
import com.tangosol.net.Invocable;
import com.tangosol.net.Member;

public class TopicBenchmarkDispatcher extends Dispatcher<MessageExecutionMark,
														 InvocationServiceStats<MessageBenchmarkSimStats>>
{
	protected final int topicsCount;
	protected final int topicsPerMember;
	
	protected final TopicBenchmarkWorkerParams workerParams;
	
	protected final PatternFacade facade;
	
	protected Invocable invocableWorker;
	
	public TopicBenchmarkDispatcher(int topicsCount, int topicsPerMember, TopicBenchmarkWorkerParams workerParams,
									Set<Member> members, PatternFacade facade)
	{
		super(members, facade.getInvocationService());
		
		if ((topicsCount < 1) || (topicsPerMember < 1) || (topicsCount < topicsPerMember + 1))
		{
			throw new IllegalArgumentException("Wrong topicsCount(" + topicsCount + ") or topicsPerMember(" + topicsPerMember + ")");
		}
		
		this.topicsCount     = topicsCount;
		this.topicsPerMember = topicsPerMember;
		this.workerParams    = workerParams;
		this.facade          = facade;
	}

	@Override
	protected void prepare() throws Exception
	{
		List<Identifier> topics = new ArrayList<Identifier>(topicsCount);
		
		for (int i = 0; i < topicsCount; ++i)
		{
			topics.add(facade.createTopic("topic-"+i));
		}
		
		Map<Member, List<Identifier>> workDistribution = distributeTopics(topics);
		
		invocableWorker = new TopicBenchmarkWorker(workerParams, topics, workDistribution);
	}
	
	protected Map<Member, List<Identifier>> distributeTopics(List<Identifier> topics)
	{
		Map<Member, List<Identifier>> result = new HashMap<Member, List<Identifier>>();
		
		List<Integer> load = new ArrayList<Integer>(topicsCount);
		for (int i = 0; i < topicsCount; ++i)
		{
			load.add(0);
		}
		
		for(Member m : members)
		{
			List<Integer> indexes = findMinimalValues(load);
			
			List<Identifier> m_receive = new ArrayList<Identifier>(topicsPerMember);
			
			for (Integer i : indexes)
			{
				m_receive.add(topics.get(i));
				load.set(i, load.get(i) + 1);
			}
			
			result.put(m, m_receive);
		}
		
		return result;
	}
	
	protected List<Integer> findMinimalValues(List<Integer> l)
	{
		List<Integer> res = new ArrayList<Integer>(topicsPerMember);
		
		List<Integer>        list = new ArrayList<Integer>(l);
		List<Integer> sorted_list = new ArrayList<Integer>(l); Collections.sort(sorted_list);
		
		for(int i = 0; i < topicsPerMember; ++i)
		{
			Integer min = sorted_list.get(0);
			sorted_list.remove(0);
			
			int min_index = list.indexOf(min);
			
			list.set(min_index, Integer.MAX_VALUE);
			res.add(min_index);
		}
		
		return res;
	}
	
	@Override
	protected void calculateExecutionStatistics()
	{
		dispatcherResult.setJavaMsStats(calculateExecutionStatisticsInternal(new MessageExecutionMark.JavaMsExtractor()));
		
		dispatcherResult.setJavaNsStats(calculateExecutionStatisticsInternal(new MessageExecutionMark.JavaNsExtractor()));
		
		dispatcherResult.setCoherenceMsStats(calculateExecutionStatisticsInternal(new MessageExecutionMark.CoherenceMsExtractor()));
		
		dispatcherResult.setExecutionMarksProcessed(getDispatcherResultSise());
	}

	protected MessageBenchmarkSimStats calculateExecutionStatisticsInternal(MessageExecutionMark.MessageExecutionMarkTimeExtractor te)
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
		
		MessageBenchmarkSimStats res = new MessageBenchmarkSimStats();
		
		res.setTotalTime((receiveTime.getMax() - sendTime.getMin()) / TimeUnit.SECONDS.toMillis(1));
		res.setThroughput(n / res.getTotalTime());
		
		res.setAverageLatency (latency.getMean());
		res.setLatencyVariance(latency.getVariance());
		res.setMinLatency     (latency.getMin());
		res.setMaxLatency     (latency.getMax());
		
		return res;
	}

	@Override
	protected InvocationServiceStats<MessageBenchmarkSimStats> createDispatcherResult()
	{
		return new InvocationServiceStats<MessageBenchmarkSimStats>();
	}

	@Override
	protected List<List<MessageExecutionMark>> createWorkersResult()
	{
		return new ArrayList<List<MessageExecutionMark>>();
	}

	@Override
	protected Invocable getInvocableWorker()
	{
		return invocableWorker;
	}
}
