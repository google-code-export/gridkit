/*
 * File: QueueSubscription.java
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

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.leasing.Lease;
import com.oracle.coherence.common.logging.Logger;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PortableObject;

/**
 * <p>A {@link QueueSubscription} represents the <strong>state</strong> of an individual subscription
 * to a {@link Queue}.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class QueueSubscription extends LeasedSubscription {
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public QueueSubscription() {
	}

	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier
	 * @param leasedSubscriptionConfiguration
	 * @param creationTime The time (since in the epoc in milliseconds) when the subscription was created.
	 */
	public QueueSubscription(SubscriptionIdentifier subscriptionIdentifier,
							 LeasedSubscriptionConfiguration leasedSubscriptionConfiguration,
							 long creationTime) {
		super(subscriptionIdentifier, leasedSubscriptionConfiguration, creationTime);
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param destinationIdentifier
	 * @param subscriberName
	 * @param leasedSubscriptionConfiguration
	 * @param creationTime The time (since in the epoc in milliseconds) when the subscription was created.
	 */
	public QueueSubscription(Identifier destinationIdentifier, 
							 String subscriberName,
							 LeasedSubscriptionConfiguration leasedSubscriptionConfiguration,
							 long creationTime) {
		this(new SubscriptionIdentifier(destinationIdentifier, subscriberName), 
			 leasedSubscriptionConfiguration,
			 creationTime);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void onLeaseSuspended(Object leaseOwner, Lease lease) {
		Logger.log(Logger.ERROR, "Unexpected %s Lease %s was suspended.  This should never happen! Ignoring request", leaseOwner, lease);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("QueueSubscription{%s}", super.toString());
	}
}
