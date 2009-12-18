/*
 * File: QueueSubscriber.java
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

import java.util.ArrayList;

import com.oracle.coherence.common.ranges.Range;
import com.oracle.coherence.common.ranges.Ranges;
import com.oracle.coherence.patterns.command.CommandSubmitter;
import com.oracle.coherence.patterns.messaging.commands.RequestMessageCommand;
import com.oracle.coherence.patterns.messaging.commands.RollbackMessagesCommand;
import com.oracle.coherence.patterns.messaging.entryprocessors.RequestMessageProcessor;
import com.oracle.coherence.patterns.messaging.entryprocessors.AcknowledgeMessageProcessor;
import com.oracle.coherence.patterns.messaging.entryprocessors.AcknowledgeSubscriptionMessagesProcessor;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.processor.ExtractorProcessor;

/**
 * <p>A specific implementation of {@link Subscriber} for {@link Queue}s.</p>
 * 
 * @author Brian Oliver
 */
class QueueSubscriber extends AbstractSubscriber<QueueSubscription> {
	
	/**
	 * <p>Package Level Constructor.</p>
	 * 
	 * @param messagingSession
	 * @param commandSubmitter
	 * @param subscriptionIdentifier
	 */
	QueueSubscriber(MessagingSession messagingSession,
				    CommandSubmitter commandSubmitter,
					SubscriptionIdentifier subscriptionIdentifier) {
		
		super(messagingSession,
			  commandSubmitter,
			  subscriptionIdentifier);		
	}


	/**
	 * {@inheritDoc} 
	 */	
	public Object getMessage() {
		//ensure that the subscriber is still active
		ensureActive();
		
		//ensure that we have a subscription from which we can retrieve messages
		ensureSubscription();

		//request a message from the queue
		getCommandSubmitter().submitCommand(getDestinationIdentifier(), 
											new RequestMessageCommand(getSubscriptionIdentifier()));

		//wait for the message to arrive (we'll be notified by the map listener when this happens)
		QueueSubscription subscription = getNextSubscriptionUpdate();

		//ensure the subscription has not been removed/shutdown
		ensureSubscription();
		
		//get the message payload and return it
		long messageId = subscription.getVisibleMessageRange().getTo();
			
		//get the message
		Message message;
		if (isAutoCommitting()) {
			//retrieve and acknowledge the message on behalf of this subscriber
			message = (Message)CacheFactory.getCache(Message.CACHENAME).invoke(
				Message.getKey(getDestinationIdentifier(), messageId), 
				new AcknowledgeMessageProcessor(getSubscriptionIdentifier())
			);
			
			//let the subscriber know that we've acknowledged the message
			CacheFactory.getCache(Subscription.CACHENAME).invoke(
				getSubscriptionIdentifier(),
				new AcknowledgeSubscriptionMessagesProcessor(Ranges.newSingletonRange(messageId))
			);
			
		} else {
			//retrieve the message 
			message = (Message)CacheFactory.getCache(Message.CACHENAME).invoke(
					Message.getKey(getDestinationIdentifier(), messageId), 
					new RequestMessageProcessor(getSubscriptionIdentifier())
				);				
		}
		
		if (message == null) {
			//TODO: message is missing! we need to do something here like
			//log a warning and retry
			return null;
			
		} else {
			return message.getPayload();
		}
	}	
	
	
	/**
	 * {@inheritDoc} 
	 */	
	public void commit() {
		ensureActive();
		
		if (!isAutoCommitting()) {
			//ensure that we have a subscription from which we can retrieve messages
			ensureSubscription();

			//determine the range of messages that are visible
			//(these are the ones we must acknowledge)
			Range messageRange = (Range)CacheFactory.getCache(Subscription.CACHENAME).invoke(
				getSubscriptionIdentifier(),
				new ExtractorProcessor("getVisibleMessageRange")
			);
			
			//send the AcknowledgeMessageProcessor to all of the visible messages
			ArrayList<String> messageKeys = new ArrayList<String>((int)messageRange.size());
			for (long messageId : messageRange)
				messageKeys.add(Message.getKey(getDestinationIdentifier(), messageId));
			
			CacheFactory.getCache(Message.CACHENAME).invokeAll(
				messageKeys,
				new AcknowledgeMessageProcessor(getSubscriptionIdentifier())
			);
			
			//let the subscriber know that we've acknowledged all messages
			CacheFactory.getCache(Subscription.CACHENAME).invoke(
				getSubscriptionIdentifier(),
				new AcknowledgeSubscriptionMessagesProcessor(messageRange)
			);
		}
	}
	
	
	/**
	 * {@inheritDoc} 
	 */	
	public void rollback() {
		ensureActive();
		
		if (!isAutoCommitting()) {
			//ensure that we have a subscription from which we can retrieve messages
			ensureSubscription();

			//determine the range of messages that are visible
			//(these are the ones we must rollback)
			Range messageRange = (Range)CacheFactory.getCache(Subscription.CACHENAME).invoke(
				getSubscriptionIdentifier(),
				new ExtractorProcessor("getVisibleMessageRange")
			);
			
			//execute a priority command to place the messages back at the front of the destination
			getCommandSubmitter().submitCommand(
				getDestinationIdentifier(),
				new RollbackMessagesCommand(getSubscriptionIdentifier(), messageRange)
			);
			
			//clear all currently visible messages from the subscription
			CacheFactory.getCache(Subscription.CACHENAME).invoke(
				getSubscriptionIdentifier(),
				new AcknowledgeSubscriptionMessagesProcessor(messageRange)
			);
		}
	}
	
	
	/**
	 * {@inheritDoc} 
	 */	
	public void release() {
		unsubscribe();
	}
}