/*
 * File: Message.java
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
import java.util.TreeSet;

import com.oracle.coherence.patterns.command.internal.CommandExecutionRequest;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.common.ticketing.Ticket;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link Message} represents a developer provided 
 * payload to be acknowledged by one or more {@link Subscription}s.</p>
 * 
 * <p>To publish a {@link Message} use an implementation of
 * {@link MessagingSession#publishMessage(Identifier, Object)}.</p> 
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class Message implements ExternalizableLite, PortableObject {
	
	/**
	 * <p>The name of the Coherence Cache that will store {@link Message}s.</p>
	 */
	public static final String CACHENAME = "coherence.messagingpattern.messages";
	
	
	/**
	 * <p>The {@link Identifier} of the {@link Destination} to which the {@link Message} 
	 * was published.</p>
	 */
	private Identifier destinationIdentifier;
	
	
	/**
	 * <p>The unique message id (for ordering {@link Message}s) allocated
	 * to the {@link Message} by the {@link Destination}.</p>
	 */
	private long messageId;
	
	
	/**
	 * <p>The {@link Ticket} from the {@link CommandExecutionRequest} that 
	 * was responsible for publishing the {@link Message}.</p>
	 */
	private Ticket ticket;

	
	/**
	 * <p>The set of {@link SubscriptionIdentifier}s of {@link Subscription}s
	 * to which the {@link Message} is visible.  This is the set of
	 * {@link Subscription}s that must acknowledged the {@link Message} before
	 * the {@link Message} is removed from the messaging infrastructure.</p>
	 */
	private Set<SubscriptionIdentifier> visibleTo;
	
	
	/**
	 * <p>The set of {@link SubscriptionIdentifier}s of {@link Subscription}s
	 * that have received delivery of the {@link Message}.  When a {@link Subscription}
	 * reads a {@link Message} they place their {@link SubscriptionIdentifier} in 
	 * this set.</p>
	 */
	private Set<SubscriptionIdentifier> deliveredTo;
	
	
	/**
	 * <p>The set of {@link SubscriptionIdentifier}s of {@link Subscription}s
	 * that have acknowledged the {@link Message}.  When this set is the same as
	 * the {@link #visibleTo} set, the {@link Message} may be safely removed 
	 * from the messaging infrastructure.</p> 
	 * 
	 * <p>A {@link Subscription} may not receive a {@link Message} again once it
	 * belongs to this set.</p>
	 */
	private Set<SubscriptionIdentifier> acknowledgedBy;
	
	
	/**
	 * <p>The developer provided payload of the {@link Message}.</p>
	 */
	private Object payload;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public Message() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * <p>To construct a {@link Message}, you should use an implementation of 
	 * {@link MessagingSession#publishMessage(Identifier, Object)} .</p>
	 * 
	 * @param destinationIdentifier The {@link Identifier} of the {@link Destination} to which the {@link Message} was sent.
	 * @param ticket The {@link Ticket} of the command that created and delivered the {@link Message}
	 * @param messageId The unique and in-order sequence number of the {@link Message}.
	 * @param subscriptionIdentifiers The set of {@link SubscriptionIdentifier}s that must acknowledge the {@link Message}
	 * @param payload The payload for the {@link Message}
	 */
	public Message(Identifier destinationIdentifier, 
				   Ticket ticket,
				   long messageId, 
				   Set<SubscriptionIdentifier> subscriptionIdentifiers, 
				   Object payload) {
		this.destinationIdentifier = destinationIdentifier;
		this.ticket = ticket;
		this.messageId = messageId;
		this.visibleTo = subscriptionIdentifiers;
		this.deliveredTo = new TreeSet<SubscriptionIdentifier>();
		this.acknowledgedBy = new TreeSet<SubscriptionIdentifier>();
		this.payload = payload;
	}

	
	/**
	 * <p>Generates a cluster-wide unique key for the {@link Message}
	 * with in it's {@link Destination}.</p>
	 * 
	 * @param destinationIdentifier
	 * @param messageId
	 */
	public static String getKey(Identifier destinationIdentifier, long messageId) {
		return String.format("%s-%d", destinationIdentifier, messageId);
	}
	

	/**
	 * <p>Returns the cluster-wide unique key for the {@link Message}.</p>
	 */
	public String getKey() {
		return getKey(destinationIdentifier, messageId);
	}
	
	
	/**
	 * <p>Returns the {@link Identifier} of the {@link Destination} to which the {@link Message}
	 * was published.</p>
	 */
	public Identifier getDestinationIdentifier() {
		return destinationIdentifier;
	}
	

	/**
	 * <p>Returns the {@link Ticket} of the {@link CommandExecutionRequest} that was 
	 * responsible for publishing the {@link Message}.</p>
	 */
	public Ticket getTicket() {
		return ticket;
	}
	

	/**
	 * <p>Returns the uniquely allocated id of the {@link Message} that 
	 * will be used to order the delivery and consumption 
	 * of the said {@link Message}.</p>
	 */
	public long getMessageId() {
		return messageId;
	}


	/**
	 * <p>Returns the payload of the {@link Message}.</p>
	 */
	public Object getPayload() {
		return payload;
	}

	
	/**
	 * <p>Makes the {@link Message} visible to the {@link Subscription} identified
	 * by the {@link SubscriptionIdentifier}.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public void visibleTo(SubscriptionIdentifier subscriptionIdentifier) {
		visibleTo.add(subscriptionIdentifier);
	}
	
	
	/**
	 * <p>Removes the visibility of the {@link Message} to the specified {@link Subscription}
	 * by the {@link SubscriptionIdentifier}.</p
	 * 
	 * @param subscriptionIdentifier
	 */
	public void invisibleTo(SubscriptionIdentifier subscriptionIdentifier) {
		visibleTo.remove(subscriptionIdentifier);
	}
	
	
	/**
	 * <p>Returns if the {@link Message} is visible to the specified {@link SubscriptionIdentifier}.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public boolean isVisibleTo(SubscriptionIdentifier subscriptionIdentifier) {
		return visibleTo.contains(subscriptionIdentifier);
	}
	
	
	/**
	 * <p>Adds the {@link SubscriptionIdentifier} of a {@link Subscription}
	 * that has acknowledged the {@link Message}.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public void acknowledgedBy(SubscriptionIdentifier subscriptionIdentifier) {
		//ensure that the message is only being acknowledged by subscriptions
		//to which this message is visible.
		if (visibleTo.contains(subscriptionIdentifier)) {
			//we also make sure we've acknowledge it
			this.deliveredTo.add(subscriptionIdentifier);
			
			//the subscription has acknowledged the message
			this.acknowledgedBy.add(subscriptionIdentifier);
		} else {
			Logger.log(Logger.ERROR, "An attempt was made to acknowledge %s by %s and the message should not be visible to that subscription.  This should not happen.  Ignoring", this, subscriptionIdentifier);
		}
	}
	
	
	/**
	 * <p>Returns if the {@link Message} has been complete acknowledged by it's {@link Subscription}s.</p>
	 */
	public boolean isAcknowledged() {
		return visibleTo.size() == acknowledgedBy.size();
	}

	
	/**
	 * <p>Returns if the {@link Message} has already been acknowledged by the specified {@link SubscriptionIdentifier}.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public boolean isAcknowledgedBy(SubscriptionIdentifier subscriptionIdentifier) {
		return acknowledgedBy.contains(subscriptionIdentifier);
	}
	
	
	/**
	 * <p>Adds the {@link SubscriptionIdentifier} of a {@link Subscription}
	 * that has received (been delivered) the {@link Message}.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public void deliveredTo(SubscriptionIdentifier subscriptionIdentifier) {
		//ensure that the message is only being delivered to subscriptions
		//to which this message is visible.
		if (visibleTo.contains(subscriptionIdentifier)) {
			//remember that it's been delivered to the subscriber
			this.deliveredTo.add(subscriptionIdentifier);
		} else {
			Logger.log(Logger.ERROR, "An attempt was made to deliver %s to %s and the message should not be visible to that subscription.  This should not happen.  Ignoring", this, subscriptionIdentifier);
		}
	}
	

	/**
	 * <p>Removes all traces of a {@link SubscriptionIdentifier} from the {@link Message}, including
	 * if it was visible, delivered and/or acknowledged.</p>
	 * 
	 * <p>This method is called when a {@link Subscription} is removed from the messaging infrastructure
	 * or the said {@link Subscription} is closed.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public void removeSubscriptionIdentifer(SubscriptionIdentifier subscriptionIdentifier) {
		visibleTo.remove(subscriptionIdentifier);
		deliveredTo.remove(subscriptionIdentifier);
		acknowledgedBy.remove(subscriptionIdentifier);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.destinationIdentifier = (Identifier)ExternalizableHelper.readObject(in);
		this.ticket = (Ticket)ExternalizableHelper.readExternalizableLite(in);
		this.messageId = ExternalizableHelper.readLong(in);
		this.visibleTo = new HashSet<SubscriptionIdentifier>();
		ExternalizableHelper.readCollection(in, visibleTo, Thread.currentThread().getContextClassLoader());
		this.deliveredTo = new HashSet<SubscriptionIdentifier>();
		ExternalizableHelper.readCollection(in, deliveredTo, Thread.currentThread().getContextClassLoader());
		this.acknowledgedBy = new HashSet<SubscriptionIdentifier>();
		ExternalizableHelper.readCollection(in, acknowledgedBy, Thread.currentThread().getContextClassLoader());
		this.payload = ExternalizableHelper.readObject(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, destinationIdentifier);
		ExternalizableHelper.writeExternalizableLite(out, ticket);
		ExternalizableHelper.writeLong(out, messageId);
		ExternalizableHelper.writeCollection(out, visibleTo);
		ExternalizableHelper.writeCollection(out, deliveredTo);
		ExternalizableHelper.writeCollection(out, acknowledgedBy);
		ExternalizableHelper.writeObject(out, payload);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.destinationIdentifier = (Identifier)reader.readObject(0);
		this.ticket = (Ticket)reader.readObject(1);
		this.messageId = reader.readLong(2);
		this.visibleTo = new HashSet<SubscriptionIdentifier>();
		reader.readCollection(3, visibleTo);
		this.deliveredTo = new HashSet<SubscriptionIdentifier>();
		reader.readCollection(4, deliveredTo);
		this.acknowledgedBy = new HashSet<SubscriptionIdentifier>();
		reader.readCollection(5, acknowledgedBy);
		this.payload = reader.readObject(6);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, destinationIdentifier);
		writer.writeObject(1, ticket);
		writer.writeLong(2, messageId);
		writer.writeCollection(3, visibleTo);
		writer.writeCollection(4, deliveredTo);
		writer.writeCollection(5, acknowledgedBy);
		writer.writeObject(6, payload);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("Message{destinationIdentifier=%s, messageId=%d, ticket=%s, visibleTo=%s, deliveredTo=%s, acknowledgedBy=%s, payload=%s}", 
							 destinationIdentifier,
							 messageId,
							 ticket,
							 visibleTo,
							 deliveredTo,
							 acknowledgedBy,
							 payload);
	}
}
