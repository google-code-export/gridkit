/*
 * File: Queue.java
 * 
 * Copyright (c) 2009. All Rights Reserved. Oracle Corporation.
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import com.oracle.coherence.common.ranges.Range;
import com.oracle.coherence.common.ranges.Ranges;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.oracle.coherence.patterns.messaging.commands.PublishMessageCommand;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.filter.NotFilter;
import com.tangosol.util.filter.PresentFilter;
import com.tangosol.util.processor.ConditionalPut;
import com.tangosol.util.processor.UpdaterProcessor;

/**
 * <p>A {@link Queue} is a {@link Destination} that manages the state of a
 * one-to-one messaging implementation.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class Queue extends Destination {
	
	/**
	 * <p>The id of the next {@link Message} we need to deliver 
	 * (iff there are no "redelivery messages") to a {@link Subscription} 
	 * on this {@link Queue}.</p>
	 */
	private long nextMessageIdToDeliver;

	
	/**
	 * <p>The {@link Range} of {@link Message} that must be re-delivered
	 * (before other {@link Message}s) as they have been recovered or rolledback.</p> 
	 */
	private Range messagesToRedeliver;
	
	
	/**
	 * <p>The queue of requests for {@link Message}s from Subscribers 
	 * (ie: {@link Subscription}s).</p>
	 *
	 * <p>When a Subscriber (via it's {@link SubscriptionIdentifier}) requests
	 * a {@link Message} from the {@link Queue}, but none are available,
	 * the {@link SubscriptionIdentifier} for the said {@link Subscriber} is placed
	 * on the end of this ordered-list (ie: queue).</p>
	 * 
	 * <p>When a {@link Message} is published, they are first allocated to
	 * Subscribers in the order as they appear in the queue.</p>
	 */
	private LinkedList<SubscriptionIdentifier> waitingSubscriptionsQueue;
	
	
	/**
	 * <p>For {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public Queue() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param queueName
	 */
	public Queue(String queueName) {
		super(queueName);
		this.nextMessageIdToDeliver = super.getNextAvailableMessageId();
		this.messagesToRedeliver = Ranges.EMPTY;
		this.waitingSubscriptionsQueue = new LinkedList<SubscriptionIdentifier>();
	}


	/**
	 * <p>Returns if the {@link Queue} currently has messages that need to 
	 * be redelivered.</p>
	 */
	public boolean hasMessagesToRedeliver() {
		return !messagesToRedeliver.isEmpty();
	}
	
	
	/**
	 * <p>Returns if the {@link Queue} has {@link Message}s to deliver
	 * to {@link Subscription}s.  Does not include {@link Message}s that
	 * need to be redelivered.</p>
	 */
	public boolean hasMessagesToDeliver() {
		return nextMessageIdToDeliver < getNextAvailableMessageId();
	}
	
	
	/**
	 * <p>Returns if there are any pending requests for {@link Message}s from the {@link Queue}.</p>
	 */
	public boolean hasWaitingSubscriptions() {
		return waitingSubscriptionsQueue.size() > 0;
	}
	
	
	/**
	 * <p>Returns the {@link SubscriptionIdentifier} of the Subscriber that should
	 * be provided with the next delivered {@link Message}.</p>
	 */
	public SubscriptionIdentifier nextWaitingSubscription() {
		return waitingSubscriptionsQueue.isEmpty() ? null : waitingSubscriptionsQueue.removeFirst();
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
			subscription == null ? new QueueSubscription(subscriptionIdentifier, 
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

		//before we publish the message, let's make sure we've delivered any messages that have to be re-delivered
		//(the messages to be redelivered have priority - they should be delivered as soon as possible)
		if (hasMessagesToRedeliver() && hasWaitingSubscriptions()) {
			redeliverMessages();
		}

		//determine which subscription should receive the message we are publishing
		//(when there are no subscriptions, we essentially deliver to no one.
		Set<SubscriptionIdentifier> deliverTo = new TreeSet<SubscriptionIdentifier>();
		if (hasWaitingSubscriptions())
			deliverTo.add(nextWaitingSubscription());
		
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
			Message existingMessage = (Message)messages.invoke(
				message.getKey(), 
				new ConditionalPut(
					new NotFilter(PresentFilter.INSTANCE), 
					message, 
					true)
			);
			
			isSuccessful = existingMessage == null || existingMessage.getTicket().equals(message.getTicket());
		}
			
		//notify the subscription(s) of the message arrival
		if (!deliverTo.isEmpty()) {
			//tell the subscriptions to accept the message
			NamedCache subscriptions = CacheFactory.getCache(Subscription.CACHENAME);
			subscriptions.invokeAll(deliverTo, new UpdaterProcessor("onAcceptMessage", message.getMessageId()));
			
			//we've delivered the message so advance to the next message we'll have to deliver
			nextMessageIdToDeliver = message.getMessageId() + 1;
		} 
		
		executionEnvironment.setContext(this);
	}

	
	/**
	 * <p>Requests that a {@link Message} be made visible to a {@link QueueSubscription}.</p>
	 */
	public void requestMessage(ExecutionEnvironment<Destination> executionEnvironment,
							   SubscriptionIdentifier subscriptionIdentifier) {

		//add the subscription to the end of the list of waiting subscriptions
		//(if it's not already in the list)
		if (!waitingSubscriptionsQueue.contains(subscriptionIdentifier))
			waitingSubscriptionsQueue.add(subscriptionIdentifier);
	
		//attempt to redeliver messsages to the waiting subscriptions
		redeliverMessages();
		
		//allocate regular (not redeliverable) messages to waiting subscriptions
		while (hasMessagesToDeliver() && hasWaitingSubscriptions()) {
			
			//get the subscription to which we'll allocate the message
			SubscriptionIdentifier aSubscriptionIdentifier = nextWaitingSubscription();

			//determine the id of the message we're going to allocate to the subscription
			long messageId = nextMessageIdToDeliver;
			
			//we've allocated a message (so move to the next)
			nextMessageIdToDeliver++;

			//make the message visible to the subscription
			CacheFactory.getCache(Message.CACHENAME).invoke(Message.getKey(this.getIdentifier(), messageId),
														    new UpdaterProcessor("visibleTo", aSubscriptionIdentifier));
			
			//notify the subscription of the message allocation
			CacheFactory.getCache(Subscription.CACHENAME).invoke(aSubscriptionIdentifier, 
															     new UpdaterProcessor("onAcceptMessage", messageId));
		}
		
		executionEnvironment.setContext(this);
	}

	
	/**
	 * <p>Attempts to redeliver {@link Message}s to waiting subscriptions.</p>
	 */
	private void redeliverMessages() {
		while (hasMessagesToRedeliver() && hasWaitingSubscriptions()) {
			//get the messageId to to redeliver
			long messageId = messagesToRedeliver.getFrom();
			messagesToRedeliver = messagesToRedeliver.remove(messageId);
			
			//get the subscription to which we'll allocate the message
			SubscriptionIdentifier subscriptionIdentifier = nextWaitingSubscription();
			
			//make the subscription known to the message
			CacheFactory.getCache(Message.CACHENAME).invoke(Message.getKey(this.getIdentifier(), messageId),
														    new UpdaterProcessor("visibleTo", subscriptionIdentifier));
			
			//notify the subscription of the message allocation
			CacheFactory.getCache(Subscription.CACHENAME).invoke(subscriptionIdentifier, 
															     new UpdaterProcessor("onAcceptMessage", messageId));
		}
	}
	
	
	/**
	 * <p>Rollsback a {@link Range} of messages and attempts to redeliver one of 
	 * the messages to any waiting subscriptions.</p>
	 */
	public void rollbackMessages(ExecutionEnvironment<Destination> executionEnvironment,
								 SubscriptionIdentifier subscriptionIdentifier,
			   					 Range messageRangeToRollback) {

		//add the range of messages to rollback to our existing set to redeliver
		messagesToRedeliver = messagesToRedeliver.union(messageRangeToRollback);
		
		//remove the subscriber from the rolledback messages
		LinkedList<String> messageKeys = new LinkedList<String>();
		for (long messageId : messageRangeToRollback) 
			messageKeys.add(Message.getKey(getIdentifier(), messageId));
		CacheFactory.getCache(Message.CACHENAME).invokeAll(
			messageKeys,
			new UpdaterProcessor("invisibleTo", subscriptionIdentifier)
		);

		//attempt to redeliver the rolledback messages
		redeliverMessages();
		
		executionEnvironment.setContext(this);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void unsubscribe(ExecutionEnvironment<Destination> executionEnvironment,
							SubscriptionIdentifier subscriptionIdentifier,
							Range visibleMessageRange) {
		
		//remove the subscriber from waiting list of subscribers
		waitingSubscriptionsQueue.remove(subscriptionIdentifier);

		//rollback the currently visible messages
		rollbackMessages(executionEnvironment, subscriptionIdentifier, visibleMessageRange);

		//unsubscribe from the destination
		super.unsubscribe(executionEnvironment, subscriptionIdentifier, visibleMessageRange);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		this.nextMessageIdToDeliver = ExternalizableHelper.readLong(in);
		this.messagesToRedeliver = (Range)ExternalizableHelper.readObject(in);
		this.waitingSubscriptionsQueue = new LinkedList<SubscriptionIdentifier>();
		ExternalizableHelper.readCollection(in, waitingSubscriptionsQueue, Thread.currentThread().getContextClassLoader());
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		ExternalizableHelper.writeLong(out, nextMessageIdToDeliver);
		ExternalizableHelper.writeObject(out, messagesToRedeliver);
		ExternalizableHelper.writeCollection(out, waitingSubscriptionsQueue);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
		this.nextMessageIdToDeliver = reader.readLong(100);
		this.messagesToRedeliver = (Range)reader.readObject(101);
		this.waitingSubscriptionsQueue = new LinkedList<SubscriptionIdentifier>();
		reader.readCollection(102, waitingSubscriptionsQueue);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
		writer.writeLong(100, nextMessageIdToDeliver);
		writer.writeObject(101, messagesToRedeliver);
		writer.writeCollection(102, waitingSubscriptionsQueue);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("Queue{%s, nextMessageIdToDeliver=%d, messagesToRedeliver=%s, pendingMessageRequests=%s}", 
							 super.toString(),
							 nextMessageIdToDeliver,
							 messagesToRedeliver,
							 waitingSubscriptionsQueue);
	}
}
