/*
 * File: MessagingSession.java
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

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.ContextConfiguration;
import com.oracle.coherence.patterns.command.DefaultContextConfiguration;

/**
 * <p>A {@link MessagingSession} provides mechanisms for a). publishing
 * {@link Message}s to {@link Destination}s (typically {@link Topic}s
 * and {@link Queue}s) and b). creating and managing {@link Subscriber}s 
 * that may receive {@link Message}s for the said {@link Destination}s.</p>
 * 
 * <p>While a {@link MessagingSession} instance is generally designed to be used by 
 * a single-thread, multiple-threads may use a single {@link MessagingSession} 
 * instance to <strong>publish</strong> a {@link Message} to any {@link Destination}.</p>
 * 
 * @author Brian Oliver
 */
public interface MessagingSession {

	/**
	 * <p>Creates a {@link Topic} {@link Destination} to which messages may be published. 
	 * Returns a unique {@link Identifier} for the created {@link Topic}.  If the topic
	 * already exists, the existing {@link Topic} {@link Identifier} is returned.</p>
	 * 
	 * <p>Uses the provided {@link ContextConfiguration} to configure the underlying
	 * runtime behaviors.</p>
	 * 
	 * @param topicName
	 */
	public Identifier createTopic(String topicName,
								  ContextConfiguration contextConfiguration);

	/**
	 * <p>Creates a {@link Topic} {@link Destination} to which messages may be published. 
	 * Returns a unique {@link Identifier} for the created {@link Topic}.  If the topic
	 * already exists, the existing {@link Topic} {@link Identifier} is returned.</p>
	 *
	 * <p>Uses a {@link DefaultContextConfiguration} for configuration.</p>
	 * 
	 * @param topicName
	 */
	public Identifier createTopic(String topicName);

	
	/**
	 * <p>Creates a {@link Queue} {@link Destination} to which messages may be published. 
	 * Returns a unique {@link Identifier} for the created {@link Queue}.  If the queue
	 * already exists, the existing {@link Queue} {@link Identifier} is returned.</p>
	 * 
	 * <p>Uses the provided {@link ContextConfiguration} to configure the underlying
	 * runtime behaviors.</p>
	 * 
	 * @param queueName
	 */
	public Identifier createQueue(String queueName,
								  ContextConfiguration contextConfiguration);

	
	/**
	 * <p>Creates a {@link Queue} {@link Destination} to which messages may be published. 
	 * Returns a unique {@link Identifier} for the created {@link Queue}.  If the queue
	 * already exists, the existing {@link Queue} {@link Identifier} is returned.</p>
	 *
	 * <p>Uses a {@link DefaultContextConfiguration} for configuration.</p>
	 * 
	 * @param queueName
	 */
	public Identifier createQueue(String queueName);
	

	/**
	 * <p>Creates a {@link Subscription} to the specified {@link Destination} configured using the 
	 * provided {@link SubscriptionConfiguration} and returns a {@link Subscriber} capable of 
	 * consuming {@link Message}s from the said {@link Destination}.</p>
	 * 
	 * @param destination
	 * @param subscriptionConfiguration
	 */
	public Subscriber subscribe(Identifier destination,
								SubscriptionConfiguration subscriptionConfiguration);
	

	/**
	 * <p>Creates a {@link Subscription} to the named {@link Destination} configured using the 
	 * provided {@link SubscriptionConfiguration} and returns a {@link Subscriber} capable of 
	 * consuming {@link Message}s from the said {@link Destination}.</p>
	 * 
	 * @param destinationName
	 * @param subscriptionConfiguration
	 */
	public Subscriber subscribe(String destinationName,
								SubscriptionConfiguration subscriptionConfiguration);
	

	/**
	 * <p>Creates and returns a {@link Subscriber} for the specified {@link Destination}
	 * using a {@link DefaultSubscriptionConfiguration}.</p>
	 * 
	 * @param destination
	 */
	public Subscriber subscribe(Identifier destination);
	

	/**
	 * <p>Creates and returns a {@link Subscriber} for the named {@link Destination}
	 * using a {@link DefaultSubscriptionConfiguration}.</p>
	 * 
	 * @param destinationName
	 */
	public Subscriber subscribe(String destinationName);
	
	
	/**
	 * <p>Publishes some payload at a message to a {@link Destination} with the specified
	 * {@link Identifier}.</p>
	 * 
	 * @param destinationIdentifier
	 * @param payload
	 */
	public void publishMessage(Identifier destinationIdentifier, Object payload);
	
	
	/**
	 * <p>Publishes some payload at a message to a {@link Destination} with the specified
	 * name.</p>
	 * 
	 * @param destinationName
	 * @param payload
	 */
	public void publishMessage(String destinationName, Object payload);

	
	/**
	 * <p>Returns an {@link Iterable} for the current {@link Identifier}s of {@link Topic}s.</p>
	 */
	public Iterable<Identifier> getTopicIdentifiers();

	
	/**
	 * <p>Returns an {@link Iterable} for the current {@link Identifier}s of {@link Queue}s.</p>
	 */
	public Iterable<Identifier> getQueueIdentifiers();
	
}
