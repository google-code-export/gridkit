/*
 * File: Subscription.java
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

import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.common.ranges.InfiniteRange;
import com.oracle.coherence.common.ranges.Range;
import com.oracle.coherence.common.ranges.Ranges;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link Subscription} represents the current state of a 
 * subscriber of {@link Message}s for a {@link Destination}.</p>
 * 
 * <p>This class is abstract as there are many forms of {@link Subscription}s to
 * {@link Destination}s, each with their own particular semantics and
 * state management requirements.  For example, a {@link Subscription} to
 * a {@link Topic} is very different from a {@link Subscription} to a {@link Queue}.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public abstract class Subscription implements ExternalizableLite, PortableObject {

	/**
	 * <p>The name of the Coherence Cache that will store {@link Subscription}s.</p>
	 */
	public static String CACHENAME = "coherence.messagingpattern.subscriptions";

	
	/**
	 * <p>The {@link SubscriptionIdentifier} identifying the {@link Subscription}.</p>
	 */
	private SubscriptionIdentifier identifier;
	
	
	/**
	 * <p>The {@link Range} of {@link Message} that are visible to the {@link Subscription}.</p>
	 */
	private Range visibleMessageRange;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public Subscription() {
	}


	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param identifier
	 */
	public Subscription(SubscriptionIdentifier identifier) {
		this.identifier = identifier;
		this.visibleMessageRange = Ranges.EMPTY;
	}

	
	/**
	 * <p>Returns the {@link SubscriptionIdentifier} of the {@link Subscription}.</p>
	 */
	public SubscriptionIdentifier getIdentifier() {
		return identifier;
	}

	
	/**
	 * <p>Handles when a {@link Message} becomes visible to the {@link Subscription}.</p>
	 * 
	 * @param messageId
	 */
	public void onAcceptMessage(long messageId) {
		if (hasVisibleMessages()) {
			if (visibleMessageRange.contains(messageId)) {
				Logger.log(Logger.WARN, "Message %d is already visible to %s.  Ignoring request", messageId, this);
			} else {
				visibleMessageRange = visibleMessageRange.add(messageId);
			}	
			
		} else {
			visibleMessageRange = Ranges.newSingletonRange(messageId);
		}
	}

	
	/**
	 * <p>Handles when a {@link Range} of visible {@link Message}s for this 
	 * {@link Subscription} have been acknowledged.</p>
	 * 
	 * <p>Once the {@link Message}s have been acknowledged, they are no longer visible
	 * to this {@link Subscription}.</p>
	 * 
	 * <p>Returns the {@link Range} that was acknowledged.</p>
	 * 
	 * @param messageRange A {@link Range} of {@link Message} ids to acknowledge.
	 */
	public Range onAcknowledgeMessageRange(Range messageRange) {
		Range acknowledgedMessageRange;
		
		if (messageRange == null || messageRange instanceof InfiniteRange) {
			acknowledgedMessageRange = visibleMessageRange;
			visibleMessageRange = Ranges.EMPTY;
		
		} else if (messageRange.isEmpty()) {
			//nothing to do if the range is empty
			acknowledgedMessageRange = Ranges.EMPTY;
			
		} else {
			//TODO: we should ensure that we are acknowledging from the front of the range!
			//otherwise we are acknowledging out of order.  do a check and issue a warning ;)

			//TODO: FUTURE: we should use a range.difference method here (if we had one)
			
			for(long messageId : messageRange) 
				visibleMessageRange = visibleMessageRange.remove(messageId);
			
			acknowledgedMessageRange = messageRange;
		}	
		
		return acknowledgedMessageRange;
	}
	
	
	/**
	 * <p>Returns the current {@link Range} of {@link Message} ids that are visible to 
	 * the {@link Subscription}.</p>
	 */
	public Range getVisibleMessageRange() {
		return visibleMessageRange;
	}

	
	/**
	 * <p>Returns if the {@link Subscription} currently has any visible {@link Message}s
	 * it must acknowledge.</p>
	 */
	public boolean hasVisibleMessages() {
		return !visibleMessageRange.isEmpty();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.identifier = (SubscriptionIdentifier)ExternalizableHelper.readExternalizableLite(in);
		this.visibleMessageRange = (Range)ExternalizableHelper.readExternalizableLite(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeExternalizableLite(out, identifier);
		ExternalizableHelper.writeExternalizableLite(out, (ExternalizableLite)visibleMessageRange);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.identifier = (SubscriptionIdentifier)reader.readObject(0);
		this.visibleMessageRange = (Range)reader.readObject(1);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, identifier);
		writer.writeObject(1, visibleMessageRange);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("Subscription{identifier=%s, visibleMessageRange=%s}",
							 identifier,
							 visibleMessageRange);
	}	
}
