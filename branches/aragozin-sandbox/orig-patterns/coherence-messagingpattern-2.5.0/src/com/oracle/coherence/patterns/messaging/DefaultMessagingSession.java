/*
 * File: DefaultMessagingSession.java
 * 
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.
 * 
 * Oracle is a registered trademark of Oracle Corporation and/or its
 * affiliates.
 * 
 * This software is the confidential and proprietary information of Oracle
 * Corporation. You shall not disclose such confidential and proprietary
 * information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Oracle Corporation.
 * 
 * Oracle Corporation makes no representations or warranties about 
 * the suitability of the software, either express or implied, 
 * including but not limited to the implied warranties of 
 * merchantability, fitness for a particular purpose, or 
 * non-infringement.  Oracle Corporation shall not be liable for 
 * any damages suffered by licensee as a result of using, modifying 
 * or distributing this software or its derivatives.
 * 
 * This notice may not be removed or altered.
 */
package com.oracle.coherence.patterns.messaging;

import java.util.Set;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.identifiers.StringBasedIdentifier;
import com.oracle.coherence.common.identifiers.UUIDBasedIdentifier;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.CommandSubmitter;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ContextConfiguration;
import com.oracle.coherence.patterns.command.ContextsManager;
import com.oracle.coherence.patterns.command.DefaultCommandSubmitter;
import com.oracle.coherence.patterns.command.DefaultContextConfiguration;
import com.oracle.coherence.patterns.command.DefaultContextsManager;
import com.oracle.coherence.patterns.command.ContextConfiguration.ManagementStrategy;
import com.oracle.coherence.patterns.command.internal.ContextWrapper;
import com.oracle.coherence.patterns.messaging.commands.PublishMessageCommand;
import com.oracle.coherence.patterns.messaging.commands.SubscribeCommand;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.extractor.ChainedExtractor;
import com.tangosol.util.filter.LikeFilter;

/**
 * <p>The default implementation of the {@link MessagingSession} interface.</p>
 *
 * @author Brian Oliver
 */
public class DefaultMessagingSession implements MessagingSession {
	
	/**
	 * <p>The default {@link MessagingSession}.</p>
	 */
	private final static MessagingSession INSTANCE = new DefaultMessagingSession(DefaultContextsManager.getInstance(), 
																				 DefaultCommandSubmitter.getInstance());

	
	/**
	 * <p>The {@link ContextsManager} we'll use to create, remove and manage
	 * {@link Destination}s (as they are {@link Context}s).</p>
	 */
	private ContextsManager contextsManager;
	
	
	/**
	 * <p>The {@link CommandSubmitter} we'll use to submit {@link Command}s 
	 * to {@link Destination}s.</p>
	 */
	private CommandSubmitter commandSubmitter;
	
	
	/**
	 * <p>Standard Constructor.</p>
	 */
	public DefaultMessagingSession(ContextsManager contextsManager, 
								   CommandSubmitter commandSubmitter) {
		this.contextsManager = contextsManager;
		this.commandSubmitter = commandSubmitter;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Identifier createTopic(String topicName,
								  ContextConfiguration contextConfiguration) {
		Topic topic = new Topic(topicName);
		return contextsManager.registerContext(topicName, topic, contextConfiguration);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Identifier createTopic(String topicName) {
		return createTopic(topicName, new DefaultContextConfiguration(ManagementStrategy.DISTRIBUTED));
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Identifier createQueue(String queueName, ContextConfiguration contextConfiguration) {
		Queue queue = new Queue(queueName);
		return contextsManager.registerContext(queueName, queue, contextConfiguration);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Identifier createQueue(String queueName) {
		return createQueue(queueName, new DefaultContextConfiguration(ManagementStrategy.DISTRIBUTED));
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void publishMessage(Identifier destinationIdentifier, Object payload) {
		commandSubmitter.submitCommand(destinationIdentifier, new PublishMessageCommand(payload));
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void publishMessage(String destinationName, Object payload) {
		publishMessage(StringBasedIdentifier.newInstance(destinationName), payload);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({"unchecked"})
	public Subscriber subscribe(Identifier destination, 
								SubscriptionConfiguration subscriptionConfiguration) {
		
		//use the class name of the destination to determine the type of subscriber to create
		String destinationClassName = 
			(String)contextsManager.extractValueFromContext(destination, 
														    new ChainedExtractor("getClass.getName")); 
				
		if (destinationClassName == null) {
			throw new UnsupportedOperationException(String.format("Can't create a subscriber for %s as the destination does not exist", destination));
			
		} else if (destinationClassName.contains("Queue")) {
			SubscriptionIdentifier subscriptionIdentifier = new SubscriptionIdentifier(destination, UUIDBasedIdentifier.newInstance());
			Subscriber subscriber = new QueueSubscriber(this, this.commandSubmitter, subscriptionIdentifier);
			commandSubmitter.submitCommand(
				destination, 
				new SubscribeCommand(subscriptionIdentifier, subscriptionConfiguration)
			);
			return subscriber;
			
		} else if (destinationClassName.contains("Topic")) {			
			SubscriptionIdentifier subscriptionIdentifier;
			if (subscriptionConfiguration instanceof TopicSubscriptionConfiguration &&
				((TopicSubscriptionConfiguration)subscriptionConfiguration).getName() != null) {
				subscriptionIdentifier = new SubscriptionIdentifier(
					destination, 
					((TopicSubscriptionConfiguration)subscriptionConfiguration).getName()
				);
			} else {
				subscriptionIdentifier = new SubscriptionIdentifier(destination, UUIDBasedIdentifier.newInstance());
			}
			
			Subscriber subscriber = new TopicSubscriber(this, this.commandSubmitter, subscriptionIdentifier);
			commandSubmitter.submitCommand(
				destination, 
				new SubscribeCommand(subscriptionIdentifier, subscriptionConfiguration)
			);
			return subscriber;
			
		} else {
			throw new UnsupportedOperationException(String.format("Can't create a subscriber for %s as the destination type is not supported", destination));
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Subscriber subscribe(String destinationName,
								SubscriptionConfiguration subscriptionConfiguration) {
		return subscribe(StringBasedIdentifier.newInstance(destinationName), subscriptionConfiguration);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Subscriber subscribe(String destinationName) {
		return subscribe(StringBasedIdentifier.newInstance(destinationName));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Subscriber subscribe(Identifier destination) {	
		return subscribe(destination, new DefaultSubscriptionConfiguration());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Iterable<Identifier> getTopicIdentifiers() {
		//NOTE: we should use the Command Pattern to provide infrastructure for this...
		Set<Identifier> identifiers = (Set<Identifier>)CacheFactory.getCache(ContextWrapper.CACHENAME).keySet(
			new LikeFilter("getContext.getClass.getName", "%Topic%")
		);
		
		return identifiers;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public Iterable<Identifier> getQueueIdentifiers() {
		//NOTE: we should use the Command Pattern to provide infrastructure for this...
		Set<Identifier> identifiers = (Set<Identifier>)CacheFactory.getCache(ContextWrapper.CACHENAME).keySet(
			new LikeFilter("getContext.getClass.getName", "%Queue%")
		);
		
		return identifiers;
	}
	
	
	/**
	 * <p>Returns an instance of the {@link DefaultMessagingSession}.</p>
	 */
	public static MessagingSession getInstance() {
		return INSTANCE;
	}	
}
