/*
 * File: Publisher.java
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

import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;

/**
 * <p>A {@link Publisher} is responsible for publishing individual
 * {@link EntryOperation}, one-at-a-time to a device or location 
 * as a result of some operation on an entry in a Coherence cache.</p>
 * 
 * <p>Standard implementations of a {@link Publisher} are in the
 * {@link com.oracle.coherence.patterns.pushreplication.publishers} package.</p>
 * 
 * <p>NOTE: It is recommended that publishers are based on the
 * {@link BatchPublisher} interface instead of this interface
 * as in most circumstances, batch publishing is more efficient
 * and provides higher-through-put, especially in WAN environments.</p>
 *
 * @author Brian Oliver
 */
public interface Publisher {

	/**
	 * <p>Start the {@link Publisher} so that we can commence
	 * publishing using the {@link Publisher#publish(EntryOperation)}.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public void start(SubscriptionIdentifier subscriptionIdentifier);
	
	
	/**
	 * <p>Stop the {@link Publisher} as we no longer need to 
	 * publish.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public void stop(SubscriptionIdentifier subscriptionIdentifier);
		
	
	/**
	 * <p>Return the number of milliseconds we should wait 
	 * before we retry publishing after an error occurs
	 * when calling {@link Publisher#publish(EntryOperation)}.</p>
	 */
	public long getRestartDelay();
	
	
	/**
	 * <p>Returns the number of consecutive publishing failures
	 * before the publisher is suspended (and won't be automatically
	 * retried).  That is, manual intervention will be required (most likely via JMX).</p>
	 * 
	 * <p>A value less than 0 means publishing will never be suspended
	 * after a failure.</p>
	 */
	public int getTotalConsecutiveFailuresBeforeSuspending();
	
	
	/**
	 * <p>Publish the specified {@link EntryOperation}.</p>
	 * 
	 * @param entryOperation
	 */
	public void publish(EntryOperation entryOperation);
	
}
