/*
 * File: Topic.java
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

import java.util.LinkedList;
import java.util.Set;

import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.common.ranges.Range;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.oracle.coherence.patterns.messaging.commands.PublishMessageCommand;
import com.oracle.coherence.patterns.messaging.entryprocessors.AcknowledgeMessageProcessor;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.filter.NotFilter;
import com.tangosol.util.filter.PresentFilter;
import com.tangosol.util.processor.ConditionalPut;
import com.tangosol.util.processor.UpdaterProcessor;

/**
 * <p>A {@link Topic} is a {@link Destination} that manages the state of a
 * one-to-many messaging implementation.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class Topic extends Destination {

	/**
	 * <p>For {@link ExternalizableLite}.</p>
	 */
	public Topic() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param topicName
	 */
	public Topic(String topicName) {
		super(topicName);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void subscribe(ExecutionEnvironment<Destination> executionEnvironment,
						  SubscriptionIdentifier subscriptionIdentifier,
						  SubscriptionConfiguration subscriptionConfiguration,
						  Subscription subscription) {
		
		super.subscribe(
			executionEnvironment, 
			subscriptionIdentifier, 
			subscriptionConfiguration,
			subscription == null ? new TopicSubscription(subscriptionIdentifier,
														 (LeasedSubscriptionConfiguration)subscriptionConfiguration,
					 									 CacheFactory.getSafeTimeMillis())
								 : subscription
		);
	}


	/**
	 * {@inheritDoc}
	 */
	public void publishMessage(ExecutionEnvironment<Destination> executionEnvironment,
							   PublishMessageCommand publishMessageCommand) {

		//only publish the message if there are subscriptions
		if (hasSubscriptions()) {

			//determine the set of subscriptions to which the published message will be made visible
			//(these are the subscriptions that will be able to see and acknowledge the message
			// once it is published)
			Set<SubscriptionIdentifier> deliverTo = getSubscriptionIdentifiers();
			
			//NOTE: this fragment of code is responsible for ensuring we don't attempt to publish
			//a message more than once (ie: when this publish command execution is recovering).
			//while it looks crude, this can be simplified at lot by implementing the DUPS/NODUPS
			//semantics of the JMS specification
			//
			//this is brute force.  it simply attempts to put a message in the messages cache
			//with an ID generated from the destination.  If a message already exists with the
			//same ID (from a previous execution of now recovering PublishMessageCommand), 
			//we try again, with a new ID.  Alternatively, if the message already exists 
			//then we don't have to do anything!
			//
			//NOTE: due to the commandpattern layer generally avoiding "re-executing" previously
			//executed commands, it is highly unlikely that this code performs iterations.  It's just "in case".
			boolean isSuccessful = false;
			Message message = null;
			while (!isSuccessful) {
				message = new Message(executionEnvironment.getContextIdentifier(),
									  executionEnvironment.getTicket(),
									  generateMessageId(),
									  deliverTo,
									  publishMessageCommand.getPayload());
				
				NamedCache messages = CacheFactory.getCache(Message.CACHENAME);
				Message existingMessage = (Message)messages.invoke(message.getKey(), 
																   new ConditionalPut(new NotFilter(PresentFilter.INSTANCE), message, true));
				
				isSuccessful = existingMessage == null || existingMessage.getTicket().equals(message.getTicket());
			}
				
			//notify the subscription(s) of the message arrival
			NamedCache subscriptions = CacheFactory.getCache(Subscription.CACHENAME);
			subscriptions.invokeAll(deliverTo, new UpdaterProcessor("onAcceptMessage", message.getMessageId()));					
			
			executionEnvironment.setContext(this);
			
		} else {
			if (Logger.isEnabled(Logger.INFO)) Logger.log(Logger.INFO, "Not publishing %s on %s as there are no subscriptions", publishMessageCommand.getPayload(), this);
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void unsubscribe(ExecutionEnvironment<Destination> executionEnvironment,
							SubscriptionIdentifier subscriptionIdentifier,
							Range visibleMessageRange) {
		
		//remove the subscriber visiblity from all of the messages (from the range above) to which it is visible
		LinkedList<String> messageKeys = new LinkedList<String>();
		for (long messageId : visibleMessageRange) 
			messageKeys.add(Message.getKey(getIdentifier(), messageId));
		CacheFactory.getCache(Message.CACHENAME).invokeAll(
			messageKeys,
			new AcknowledgeMessageProcessor(subscriptionIdentifier)
		);
		
		super.unsubscribe(executionEnvironment, subscriptionIdentifier, visibleMessageRange);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("Topic{%s}", 
							 super.toString());
	}
}
