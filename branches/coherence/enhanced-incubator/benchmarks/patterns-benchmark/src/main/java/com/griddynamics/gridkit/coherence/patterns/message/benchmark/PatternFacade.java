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
	
	public static class DefaultFacade implements PatternFacade
	{
		static private final DefaultFacade instance = new DefaultFacade();
		
		public static DefaultFacade getInstance()
		{
			return instance;
		}
		
		private final MessagingSession messagingSession;
		
		private DefaultFacade()
		{
			messagingSession = DefaultMessagingSession.getInstance();
		}
		
		@Override
		public Identifier createQueue(String queueName)
		{
			return messagingSession.createQueue(queueName);
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
			return messagingSession.createTopic(topicName);
		}
	}
}
