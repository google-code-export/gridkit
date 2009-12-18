/*
 * File: TopicSubscription.java
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

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.leasing.Lease;
import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.common.ranges.InfiniteRange;
import com.oracle.coherence.common.ranges.Range;
import com.oracle.coherence.common.ranges.Ranges;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.processor.UpdaterProcessor;

/**
 * <p>A {@link TopicSubscription} represents the <b>state</b> of {@link Subscription} to
 * a {@link Topic}.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class TopicSubscription extends LeasedSubscription {

	/**
	 * <p>The {@link Range} of {@link Message}s that have been delivered
	 * to the {@link Subscription} (a sub-set of the visibile {@link Range} of {@link Message}s).</p>
	 */
	private Range deliveredMessageRange;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public TopicSubscription() {
	}


	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier
	 * @param leasedSubscriptionConfiguration
	 * @param creationTime
	 */
	public TopicSubscription(SubscriptionIdentifier subscriptionIdentifier,
							 LeasedSubscriptionConfiguration leasedSubscriptionConfiguration,
			 				 long creationTime) {
		super(subscriptionIdentifier, leasedSubscriptionConfiguration, creationTime);
		this.deliveredMessageRange = Ranges.EMPTY;
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param destinationIdentifier
	 * @param subscriberName
	 * @param leasedSubscriptionConfiguration
	 * @param creationTime
	 */
	public TopicSubscription(Identifier destinationIdentifier, 
							 String subscriberName, 
							 LeasedSubscriptionConfiguration leasedSubscriptionConfiguration,
							 long creationTime) {
		this(new SubscriptionIdentifier(destinationIdentifier, subscriberName),
			 leasedSubscriptionConfiguration,
			 creationTime);
	}

	
	/**
	 * <p>Returns if the {@link TopicSubscription} is durable.</p>
	 */
	public boolean isDurable() {
		return getConfiguration() != null &&
			   getConfiguration() instanceof TopicSubscriptionConfiguration &&
			   ((TopicSubscriptionConfiguration)getConfiguration()).isDurable();
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void onLeaseSuspended(Object leaseOwner, Lease lease) {
		if (Logger.isEnabled(Logger.DEBUG)) Logger.log(Logger.DEBUG, "The lease for %s has been suspended. Rolling back delivered messages\n", this);

		CacheFactory.getCache(Subscription.CACHENAME).invoke(
			getIdentifier(),
			new UpdaterProcessor("rollback", Ranges.INFINITE)
		);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void onLeaseExpiry(Object leaseOwner, Lease lease) {
		if (isDurable()) {
			if (Logger.isEnabled(Logger.DEBUG)) Logger.log(Logger.DEBUG, "The lease for durable %s has expired. Rolling back delivered messages\n", this);

			CacheFactory.getCache(Subscription.CACHENAME).invoke(
				getIdentifier(),
				new UpdaterProcessor("rollback", Ranges.INFINITE)
			);
		} else {
			super.onLeaseExpiry(leaseOwner, lease);			
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void onAcceptMessage(long messageId) {
		if (!hasVisibleMessages() && deliveredMessageRange.isEmpty()) {
			deliveredMessageRange = Ranges.newEmptyRangeFrom(messageId);
		}
		super.onAcceptMessage(messageId);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Range onAcknowledgeMessageRange(Range messageRange) {
		Range acknowledgedMessageRange = super.onAcknowledgeMessageRange(messageRange);
		for(long messageId : acknowledgedMessageRange) {
			deliveredMessageRange = deliveredMessageRange.remove(messageId);
		}
		return acknowledgedMessageRange;
	}
	
	
	/**
	 * <p>Returns the {@link Range} of {@link Message}s that have been delivered to the 
	 * {@link TopicSubscription}.</p>
	 */
	public Range getDeliveredMessageRange() {
		return deliveredMessageRange;
	}
	
	
	/**
	 * <p>Returns the next {@link Message} id that should be delivered
	 * to the {@link Subscription}.</p>
	 */
	public long nextMessageToDeliver() {
		if (hasVisibleMessages()) { 
			if (deliveredMessageRange.isEmpty()) {
				return getVisibleMessageRange().getFrom();
				
			} else if (deliveredMessageRange.getTo() < getVisibleMessageRange().getTo()) {
				return deliveredMessageRange.getTo() + 1;
				
			} else {
				return -1;
			}
			
		} else {
			return -1;
		}
	}
	

	/**
	 * <p>Handles when a {@link Message} is delivered to a {@link TopicSubscription}.</p>
	 * 
	 * @param messageId
	 */
	public void onMessageDelivered(long messageId) {
		if (deliveredMessageRange.isEmpty()) {
			deliveredMessageRange = Ranges.newSingletonRange(messageId);
		} else {
			deliveredMessageRange = deliveredMessageRange.add(messageId);
		}
	}
	
	
	/**
	 * <p>Rollback the currently delivered {@link Message}s.</p>
	 * 
	 * @param messageRangeToRollback
	 */
	public void rollback(Range messageRangeToRollback) {
		if (messageRangeToRollback instanceof InfiniteRange) {
			deliveredMessageRange = Ranges.newEmptyRangeFrom(getVisibleMessageRange().getFrom());
			
		} else if (messageRangeToRollback.isEmpty()) {
			//nothing to do here
			
		} else {
			for(long messageId : messageRangeToRollback) 
				deliveredMessageRange = deliveredMessageRange.remove(messageId);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		this.deliveredMessageRange = (Range)ExternalizableHelper.readExternalizableLite(in);
	}	
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		ExternalizableHelper.writeExternalizableLite(out, (ExternalizableLite)deliveredMessageRange);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
		this.deliveredMessageRange = (Range)reader.readObject(300);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
		writer.writeObject(300, deliveredMessageRange);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("TopicSubscription{%s, deliveredMessageRange=%s}", 
							 super.toString(),
							 deliveredMessageRange);
		
	}
}