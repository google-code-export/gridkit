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

import java.util.List;
import java.util.Set;

import com.griddynamics.gridkit.coherence.patterns.benchmark.stats.InvocationServiceStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.MessageBenchmarkStats;
import com.griddynamics.gridkit.coherence.patterns.message.benchmark.PatternFacade;
import com.tangosol.net.Member;

public class SingleRunTopicBenchmark
{
	public static void warmUp(PatternFacade facade, List<Member> members)
	{
		
	}

	public static void main(String[] args)
	{
		setCoherenceConfig(false);
		
		setSysProp("benchmark.topic.senderThreadsCount",   "2");
		setSysProp("benchmark.topic.receiverThreadsCount", "2");
		
		setSysProp("benchmark.topic.messagesPerThread", "250");
		
		setSysProp("benchmark.topic.senderSpeedLimit", "1000");
		
		setSysProp("benchmark.topic.topicsCount", "6");
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
