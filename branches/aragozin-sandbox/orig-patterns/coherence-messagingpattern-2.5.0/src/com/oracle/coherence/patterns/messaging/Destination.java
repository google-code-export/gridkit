/*
 * File: Destination.java
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.identifiers.StringBasedIdentifier;
import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.common.ranges.Range;
import com.oracle.coherence.patterns.command.Context;
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

/**
 * <p>A {@link Destination} represents and manages the state 
 * of a uniquely named store-and-forward location to which {@link Message}s 
 * will be logically delivered and then asynchronously forwarded in-order of 
 * arrival to {@link Subscription}s.</p>
 * 
 * <p>There are generally two types of {@link Destination}s; {@link Topic}s 
 * that are used for one-to-many messaging, and {@link Queue}s
 * that are used for one-to-one messaging.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public abstract class Destination implements Context, ExternalizableLite, PortableObject {

	/**
	 * <p>The {@link Identifier} of the {@link Destination}.</p>
	 */
	private Identifier identifier;
	
	
	/**
	 * <p>The next available monotonically increasing message id that
	 * will be allocated to the next {@link Message} sent to the 
	 * {@link Destination}.</p>
	 */
	private long nextAvailableMessageId;
	
	
	/**
	 * <p>The set of {@link SubscriptionIdentifier}s for {@link Subscription} to the
	 * {@link Destination}.</p>
	 * 
	 * <p>{@link Message}s will only be delivered to these
	 * {@link Subscription}s.</p>
	 */
	private HashSet<SubscriptionIdentifier> subscriptionIdentifiers;


	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public Destination() {
		this.nextAvailableMessageId = 1;
		this.subscriptionIdentifiers = new HashSet<SubscriptionIdentifier>();
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param destinationName The name of the {@link Destination}
	 */
	public Destination(String destinationName) {
		this();
		this.identifier = StringBasedIdentifier.newInstance(destinationName);
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param identifier The {@link Identifier} of the {@link Destination}
	 */
	public Destination(Identifier identifier) {
		this();
		this.identifier = identifier;
	}
	
	
	/**
	 * <p>Returns a {@link Identifier} for referring to the {@link Destination}.</p>
	 */
	public Identifier getIdentifier() {
		return identifier;
	}
	
	
	/**
	 * <p>Returns the next available message id.</p>
	 */
	protected long getNextAvailableMessageId() {
		return nextAvailableMessageId;
	}
	
	
	/**
	 * <p>Generate the an id for the next {@link Message} to publish
	 * to the {@link Destination}.</p>
	 */
	public long generateMessageId() {
		return nextAvailableMessageId++;
	}
	

	/**
	 * <p>Return if the {@link Destination} has any current {@link Subscription}s.</p>
	 */
	public boolean hasSubscriptions() {
		return subscriptionIdentifiers.size() > 0;
	}
	
	
	/**
	 * <p>Returns the {@link SubscriptionIdentifier}s of the current {@link Subscription}s
	 * for the {@link Destination}.</p>
	 */
	public Set<SubscriptionIdentifier> getSubscriptionIdentifiers() {
		return subscriptionIdentifiers;
	}

	
	/**
	 * <p>Returns if the {@link Destination} contains a registration for the specified {@link SubscriptionIdentifier}.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public boolean containsSubscriptionWithIdentifer(SubscriptionIdentifier subscriptionIdentifier) {
		return subscriptionIdentifiers.contains(subscriptionIdentifier);
	}
	

	/**
	 * <p>Adds a new {@link Subscription} {@link SubscriptionIdentifier} to the {@link Destination}.</p>
	 * 
	 * @param subscriptionIdentifier
	 * 
	 * @return <code>true</code> if a {@link Subscription} with the specified 
	 * {@link SubscriptionIdentifier} already exists on the {@link Destination}.</p>
	 */
	public boolean addSubscriptionWithIdentifer(SubscriptionIdentifier subscriptionIdentifier) {
		return subscriptionIdentifiers.add(subscriptionIdentifier);
	}
	
	
	/**
	 * <p>Publishes the message specified in the {@link PublishMessageCommand} using the provided
	 * {@link ExecutionEnvironment}.</p>
	 * 
	 * @param executionEnvironment
	 * @param publishMessageCommand
	 */
	public abstract void publishMessage(ExecutionEnvironment<Destination> executionEnvironment, 
										PublishMessageCommand publishMessageCommand);
	
	
	/**
	 * <p>Creates a {@link Subscription} to the {@link Destination} identified by the
	 * specified {@link SubscriptionIdentifier}.</p>  
	 * 
	 * @param executionEnvironment
	 * @param subscriptionIdentifier
	 * @param subscriptionConfiguration
	 * @param subscription The object that will be used to capture the state of the subscription 
	 */
	public void subscribe(ExecutionEnvironment<Destination> executionEnvironment,
						  SubscriptionIdentifier subscriptionIdentifier,
						  SubscriptionConfiguration subscriptionConfiguration,
						  Subscription subscription) {

		//attempt to register the subscription
		if (subscription == null) {
			Logger.log(Logger.ERROR,
					   "Can't subscribe to %s with %s as no subscription was provided. Ignoring request to subscribe", 
					   getIdentifier(),
					   subscriptionIdentifier
			);
			
		} else {
			//first attempt to place the subscription into the subscriptions cache (if it does not already exist)
			NamedCache subscriptionsCache = CacheFactory.getCache(Subscription.CACHENAME);
			Subscription existingSubscription = 
				(Subscription)subscriptionsCache.invoke(subscription.getIdentifier(), 
														new ConditionalPut(new NotFilter(PresentFilter.INSTANCE), subscription, true));
			
			if (existingSubscription != null) {
				if (Logger.isEnabled(Logger.WARN))
					Logger.log(
						Logger.WARN, 
						"Subscription %s already exists in subscriptions cache. Ignoring request to create a subscription", 
						subscription.getIdentifier(),
						getIdentifier()
					);
			}
				
			//then add the subscription to the destination
			if (addSubscriptionWithIdentifer(subscription.getIdentifier())) {	
				executionEnvironment.setContext(this);
				
			} else {
				if (Logger.isEnabled(Logger.WARN))
					Logger.log(
						Logger.WARN, 
						"Subscription %s already registered with the Destination %s. Ignoring request to register the subscription", 
						subscription.getIdentifier(),
						getIdentifier()
					);
			}				
		}
	}
	

	/**
	 * <p>Unsubscribe the specified {@link SubscriptionIdentifier} and appropriately
	 * deal with (perhaps rollback) the {@link Range} of messages.</p> 
	 * 
	 * @param executionEnvironment
	 * @param subscriptionIdentifier
	 * @param visibleMessageRange
	 */
	public void unsubscribe(ExecutionEnvironment<Destination> executionEnvironment,
			  				SubscriptionIdentifier subscriptionIdentifier,
			  				Range visibleMessageRange) {
		
		//remove the subscriber from the destination
		removeSubscriptionWithIdentifier(subscriptionIdentifier);
		
		//remove the subscription
		NamedCache subscriptionsCache = CacheFactory.getCache(Subscription.CACHENAME);
		subscriptionsCache.remove(subscriptionIdentifier);
		
		//the destination has now changed
		executionEnvironment.setContext(this);		
	}
	
	
	/**
	 * <p>Removes the {@link Subscription} with the specified {@link SubscriptionIdentifier}
	 * from the {@link Destination}.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public void removeSubscriptionWithIdentifier(SubscriptionIdentifier subscriptionIdentifier) {
		subscriptionIdentifiers.remove(subscriptionIdentifier);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.identifier = (Identifier)ExternalizableHelper.readObject(in);
		this.nextAvailableMessageId = ExternalizableHelper.readLong(in);
		this.subscriptionIdentifiers = new HashSet<SubscriptionIdentifier>();
		ExternalizableHelper.readCollection(in, this.subscriptionIdentifiers, Thread.currentThread().getContextClassLoader());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, identifier);
		ExternalizableHelper.writeLong(out, nextAvailableMessageId);
		ExternalizableHelper.writeCollection(out, subscriptionIdentifiers);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.identifier = (Identifier)reader.readObject(0);
		this.nextAvailableMessageId = reader.readLong(1);
		this.subscriptionIdentifiers = new HashSet<SubscriptionIdentifier>();
		reader.readCollection(2, subscriptionIdentifiers);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, identifier);
		writer.writeLong(1, nextAvailableMessageId);
		writer.writeCollection(2, subscriptionIdentifiers);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("Destination{identifier=%s, nextMessageSequenceId=%d, subscriptionIdentifiers=%s}", 
							 identifier,
							 nextAvailableMessageId,
							 subscriptionIdentifiers);
	}
}
