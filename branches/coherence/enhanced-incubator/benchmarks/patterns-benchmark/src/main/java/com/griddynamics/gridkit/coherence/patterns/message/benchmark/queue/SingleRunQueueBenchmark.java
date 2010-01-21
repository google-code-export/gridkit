package com.griddynamics.gridkit.coherence.patterns.message.benchmark.queue;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setCoherenceConfig;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.griddynamics.gridkit.coherence.patterns.message.benchmark.MessageBenchmarkStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.PatternFacade;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Member;

public class SingleRunQueueBenchmark
{
	public static void warmUp(PatternFacade facade, List<Member> members)
	{
		
	}
	
	public static void main(String[] args)
	{
		setCoherenceConfig();
		setSysProp("tangosol.coherence.distributed.localstorage", "false");
		
		setSysProp("benchmark.queue.senderThreadsCount",   "5");
		setSysProp("benchmark.queue.receiverThreadsCount", "2");
		
		setSysProp("benchmark.queue.messagesPerThread", "250");
		
		setSysProp("benchmark.queue.senderSpeedLimit",   "0");
		setSysProp("benchmark.queue.receiverSpeedLimit", "0");
		
		setSysProp("benchmark.queue.queuesCount", "3");
		
		QueueBenchmarkWorkerParams params = new QueueBenchmarkWorkerParams
		(
			Integer.getInteger("benchmark.queue.senderThreadsCount"),
			Integer.getInteger("benchmark.queue.receiverThreadsCount"),
			Integer.getInteger("benchmark.queue.messagesPerThread"),
			Integer.getInteger("benchmark.queue.senderSpeedLimit"),
			Integer.getInteger("benchmark.queue.receiverSpeedLimit")
		);
		
		PatternFacade facade = PatternFacade.DefaultFacade.getInstance();
		
		@SuppressWarnings("unchecked")
		Set<Member> members_ = facade.getInvocationService().getInfo().getServiceMembers();
		members_.remove(CacheFactory.getCluster().getLocalMember());
		List<Member> members = setToList(members_);
		
		QueueBenchmarkDispatcher dispatcher = new QueueBenchmarkDispatcher(Integer.getInteger("benchmark.queue.queuesCount"));
		
		sysOut("Starting up SingleRunQueueBenchmark ...");
		
		MessageBenchmarkStats res = dispatcher.execute(facade, members, params);
		
		sysOut("SingleRunQueueBenchmark results:");
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println(res.toString());
		System.out.print("Java MS stats");
		System.out.println(res.getJavaMsStats().toString());
		System.out.print("Java NS stats");
		System.out.println(res.getJavaNsStats().toString());
		System.out.print("Coherence MS stats");
		System.out.println(res.getCoherenceMsStats().toString());
		System.out.println("--------------------------------------------------------------------------------");
	}
	
	private static <T> List<T> setToList(Set<T> s)
	{
		List<T> r = new ArrayList<T>(s.size());
		
		for(T e : s)
			r.add(e);
		
		return r;
	}
}
