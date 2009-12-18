/*
 * File: SubscriptionLostException.java
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
package com.oracle.coherence.patterns.messaging.exceptions;

import com.oracle.coherence.patterns.messaging.Subscriber;
import com.oracle.coherence.patterns.messaging.Subscription;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;

/**
 * <p>The {@link SubscriptionLostException} is thrown when attempts
 * to use a {@link Subscriber} or {@link Subscription} have failed,
 * typically due to a {@link Subscription} being removed from the system.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class SubscriptionLostException extends RuntimeException {

	/**
	 * <p>The {@link SubscriptionIdentifier} that has been lost.</p>
	 */
	private SubscriptionIdentifier subscriptionIdentifier;
	

	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier
	 * @param message
	 */
	public SubscriptionLostException(SubscriptionIdentifier subscriptionIdentifier, String message) {
		super(message);
		this.subscriptionIdentifier = subscriptionIdentifier;
	}
	
	
	/**
	 * <p>Returns the {@link SubscriptionIdentifier} that was lost.</p>
	 */
	public SubscriptionIdentifier getSubscriptionIdentifier() {
		return subscriptionIdentifier;
	}
}
