/*
 * File: DefaultPushReplicationManager.java
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
package com.oracle.coherence.patterns.pushreplication;

import com.oracle.coherence.patterns.command.DefaultCommandSubmitter;
import com.oracle.coherence.patterns.messaging.DefaultMessagingSession;
import com.oracle.coherence.patterns.messaging.DefaultSubscriptionConfiguration;
import com.oracle.coherence.patterns.messaging.MessagingSession;
import com.oracle.coherence.patterns.messaging.Subscription;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.oracle.coherence.patterns.messaging.TopicSubscription;
import com.oracle.coherence.patterns.messaging.commands.SubscribeCommand;

/**
 * <p>A default implementation of a {@link PushReplicationManager}.</p>
 * 
 * @author Brian Oliver
 */
public class DefaultPushReplicationManager implements PushReplicationManager {
	
	/**
	 * <p>The default {@link PushReplicationManager}.</p>
	 */
	private final static PushReplicationManager INSTANCE = new DefaultPushReplicationManager(DefaultMessagingSession.getInstance());
	
	
	/**
	 * <p>The {@link MessagingSession} we use internally for managing {@link TopicSubscription}s.</p>
	 */
	private MessagingSession messagingSession;
	
	
	/**
	 * <p>The Standard Constructor.</p>
	 */
	public DefaultPushReplicationManager(MessagingSession messagingSession) {
		this.messagingSession = messagingSession;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public SubscriptionIdentifier registerBatchPublisher(String cacheName,
			 											 String publisherName, 
			 											 BatchPublisher batchPublisher) {

		//create a specialized subscription for the batch publisher
		Subscription subscription = new DefaultPublishingSubscription(
			cacheName, 
			publisherName, 
			batchPublisher
		);
		
		//TODO: this should be completed using the messaging session command submitter
		DefaultCommandSubmitter.getInstance().submitCommand(
			subscription.getIdentifier().getDestinationIdentifier(), 
			new SubscribeCommand<DefaultSubscriptionConfiguration>(
				subscription.getIdentifier(), 
				new DefaultSubscriptionConfiguration(),
				subscription), 
			true
		);
		
		return subscription.getIdentifier();
	}
	

	/**
	 * <p>Returns an instance of the {@link DefaultPushReplicationManager}.</p>
	 */
	public static PushReplicationManager getInstance() {
		return INSTANCE;
	}	
}
