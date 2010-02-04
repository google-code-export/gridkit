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
package com.griddynamics.gridkit.coherence.patterns.message.benchmark;

import com.griddynamics.gridkit.coherence.patterns.benchmark.Names;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.messaging.DefaultMessagingSession;
import com.oracle.coherence.patterns.messaging.MessagingSession;
import com.oracle.coherence.patterns.messaging.Subscriber;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;

public interface PatternFacade
{
	public InvocationService getInvocationService();
	
	public void publishMessage(Identifier destinationIdentifier, Object payload);
	
	public Identifier createQueue(String queueName);
	
	public Identifier createTopic(String topicName);
	
	public Subscriber subscribe(Identifier destination);
	
	public static class DefaultFacade extends com.griddynamics.gridkit.coherence.patterns.benchmark.PatternFacade implements PatternFacade
	{
		static private final DefaultFacade instance = new DefaultFacade();
		
		public static DefaultFacade getInstance()
		{
			return instance;
		}
		
		private final MessagingSession messagingSession;
		
		private DefaultFacade()
		{
			super();
			messagingSession = DefaultMessagingSession.getInstance();
		}
		
		@Override
		public Identifier createQueue(String queueName)
		{
			return messagingSession.createQueue(queueName, conf);
		}

		@Override
		public InvocationService getInvocationService()
		{
			return (InvocationService)CacheFactory.getService(Names.invocationService);
		}

		@Override
		public void publishMessage(Identifier destinationIdentifier, Object payload)
		{
			messagingSession.publishMessage(destinationIdentifier, payload);
		}

		@Override
		public Subscriber subscribe(Identifier destination)
		{
			return messagingSession.subscribe(destination);
		}

		@Override
		public Identifier createTopic(String topicName)
		{
			return messagingSession.createTopic(topicName, conf);
		}
	}
}
