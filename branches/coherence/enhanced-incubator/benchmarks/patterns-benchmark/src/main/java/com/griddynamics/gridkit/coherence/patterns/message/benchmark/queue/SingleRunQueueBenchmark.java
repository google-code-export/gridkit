package com.griddynamics.gridkit.coherence.patterns.message.benchmark.queue;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.getOtherInvocationServiceMembers;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setCoherenceConfig;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.List;
import java.util.Set;

import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.MessageBenchmarkStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.PatternFacade;
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
		
		setSysProp("benchmark.queue.senderThreadsCount",   "2");
		setSysProp("benchmark.queue.receiverThreadsCount", "2");
		
		setSysProp("benchmark.queue.messagesPerThread", "250");
		
		setSysProp("benchmark.queue.senderSpeedLimit",   "0");
		setSysProp("benchmark.queue.receiverSpeedLimit", "0");
		
		setSysProp("benchmark.queue.queuesCount", "3");
		
		QueueBenchmarkParams benchmarkParams = new QueueBenchmarkParams();
		
		benchmarkParams.setSenderThreadsCount(Integer.getInteger("benchmark.queue.senderThreadsCount"));
		benchmarkParams.setReceiverThreadsCount(Integer.getInteger("benchmark.queue.receiverThreadsCount"));
		benchmarkParams.setMessagesPerThread(Integer.getInteger("benchmark.queue.messagesPerThread"));
		benchmarkParams.setSenderSpeedLimit(Integer.getInteger("benchmark.queue.senderSpeedLimit"));
		benchmarkParams.setReceiverSpeedLimit(Integer.getInteger("benchmark.queue.receiverSpeedLimit"));
		
		benchmarkParams.setQueuesCount(Integer.getInteger("benchmark.queue.queuesCount"));
		
		PatternFacade facade = PatternFacade.DefaultFacade.getInstance();
		
		Set<Member> members = getOtherInvocationServiceMembers(facade.getInvocationService());
		
		QueueBenchmarkDispatcher dispatcher = new QueueBenchmarkDispatcher(members, facade);
		
		sysOut("Starting up SingleRunQueueBenchmark ...");
		
		InvocationServiceStats<MessageBenchmarkStats> res = dispatcher.execute(benchmarkParams);
		
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
}
