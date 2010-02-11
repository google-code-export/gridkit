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
package com.griddynamics.gridkit.coherence.patterns.message.benchmark.topic;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.getOtherInvocationServiceMembers;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setCoherenceConfig;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setSysProp;
import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.sysOut;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.MessageBenchmarkStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.PatternFacade;
import com.tangosol.net.Member;

public class SingleRunTopicBenchmark
{
	public static void warmUp(PatternFacade facade, Set<Member> members)
	{
		TopicBenchmarkParams benchmarkParams = new TopicBenchmarkParams();
		
		benchmarkParams.setSenderThreadsCount(2);
		benchmarkParams.setReceiverThreadsCount(2);
		benchmarkParams.setMessagesPerThread(250);
		benchmarkParams.setSenderSpeedLimit(0);
		
		benchmarkParams.setTopicsCount(12);
		benchmarkParams.setTopicsPerMember(3);
		
		for (int i = 1; i <= 5; ++i)
		{
			sysOut("Warming up (stage " + i + " started)");
			
			TopicBenchmarkDispatcher dispatcher = new TopicBenchmarkDispatcher(members, facade);
			dispatcher.execute(benchmarkParams);
			
			sysOut("Warming up (waiting...)");
			LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
		}
		
		return;
		
	}

	public static void main(String[] args)
	{
		setCoherenceConfig(false);
		
		System.setProperty("benchmark.gc-in-worker",     "true");
		System.setProperty("benchmark.gc-in-dispatcher", "true");
		
		setSysProp("benchmark.topic.senderThreadsCount",   "2");
		setSysProp("benchmark.topic.receiverThreadsCount", "2");
		
		setSysProp("benchmark.topic.messagesPerThread", "250");
		
		setSysProp("benchmark.topic.senderSpeedLimit", "0");
		
		setSysProp("benchmark.topic.topicsCount", "12");
		setSysProp("benchmark.topic.topicsPerMember", "3");
		
		TopicBenchmarkParams benchmarkParams = new TopicBenchmarkParams();
		
		benchmarkParams.setSenderThreadsCount(Integer.getInteger("benchmark.topic.senderThreadsCount"));
		benchmarkParams.setReceiverThreadsCount(Integer.getInteger("benchmark.topic.receiverThreadsCount"));
		benchmarkParams.setMessagesPerThread(Integer.getInteger("benchmark.topic.messagesPerThread"));
		benchmarkParams.setSenderSpeedLimit(Integer.getInteger("benchmark.topic.senderSpeedLimit"));
		
		benchmarkParams.setTopicsCount(Integer.getInteger("benchmark.topic.topicsCount"));
		benchmarkParams.setTopicsPerMember(Integer.getInteger("benchmark.topic.topicsPerMember"));
		
		PatternFacade facade = PatternFacade.DefaultFacade.getInstance();
		
		Set<Member> members = getOtherInvocationServiceMembers(facade.getInvocationService());
		
		warmUp(facade, members);
		
		TopicBenchmarkDispatcher dispatcher = new TopicBenchmarkDispatcher(members, facade);
		
		sysOut("Starting up SingleRunTopicBenchmark ...");
		
		InvocationServiceStats<MessageBenchmarkStats> res = dispatcher.execute(benchmarkParams);
		
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
