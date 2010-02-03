package com.griddynamics.gridkit.coherence.patterns.message.benchmark.topic;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.getOtherInvocationServiceMembers;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setCoherenceConfig;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.List;
import java.util.Set;

import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.MessageBenchmarkSimStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.PatternFacade;
import com.tangosol.net.Member;

public class SingleRunTopicBenchmark
{
	public static void warmUp(PatternFacade facade, List<Member> members)
	{
		
	}

	public static void main(String[] args)
	{
		setCoherenceConfig();
		setSysProp("tangosol.coherence.distributed.localstorage", "false");
		
		setSysProp("benchmark.topic.senderThreadsCount",   "2");
		setSysProp("benchmark.topic.receiverThreadsCount", "2");
		
		setSysProp("benchmark.topic.messagesPerThread", "250");
		
		setSysProp("benchmark.topic.senderSpeedLimit", "1000");
		
		setSysProp("benchmark.topic.topicsCount", "6");
		setSysProp("benchmark.topic.topicsPerMember", "3");
		
		TopicBenchmarkWorkerParams params = new TopicBenchmarkWorkerParams
		(
			Integer.getInteger("benchmark.topic.senderThreadsCount"),
			Integer.getInteger("benchmark.topic.receiverThreadsCount"),
			Integer.getInteger("benchmark.topic.messagesPerThread"),
			Integer.getInteger("benchmark.topic.senderSpeedLimit")
		);
		
		PatternFacade facade = PatternFacade.DefaultFacade.getInstance();
		
		Set<Member> members = getOtherInvocationServiceMembers(facade.getInvocationService());
		
		TopicBenchmarkDispatcher dispatcher = new TopicBenchmarkDispatcher(Integer.getInteger("benchmark.topic.topicsCount"),
																		   Integer.getInteger("benchmark.topic.topicsPerMember"),
																		   params, members, facade);
		
		sysOut("Starting up SingleRunTopicBenchmark ...");
		
		InvocationServiceStats<MessageBenchmarkSimStats> res = dispatcher.execute();
		
		sysOut("SingleRunTopicBenchmark results:");
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
