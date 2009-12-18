/*
 * File: BatchPublisher.java
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

import java.util.Iterator;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;

/**
 * <p>A {@link BatchPublisher} is responsible for publishing batches of   
 * {@link EntryOperation}s (that arrive as messages to a {@link PublishingSubscription}) 
 * to some implementation specific device/location (represented by an
 * implementation of this interface).</p>
 * 
 * <p>When using a {@link BatchPublisher}, the {@link PublishingService} operates 
 * by waiting a specified amount of time (specified by 
 * {@link #getBatchPublishingDelay()}) and then, when the time has expired, 
 * publishes all of the available {@link EntryOperation}s in batches 
 * (containing at most {@link #getBatchSize()} {@link EntryOperation}s) 
 * using the {@link #publishBatch(SubscriptionIdentifier, Iterator)} method.</p>
 * 
 * <p>Should there be less than the specified {@link #getBatchSize()} {@link EntryOperation}s
 * available to publish, only those that are available will be published. That is,
 * an under-sized "batch" will be sent in favor of waiting for a full-sized 
 * batch to become available.</p>
 * 
 * <p>When publishing with a {@link BatchPublisher}, the {@link PublishingService}
 * expects that {@link #publishBatch(SubscriptionIdentifier, Iterator)} operation is atomic.  
 * Should the method fail for any reason, the entire batch will retried, possibly
 * with more {@link EntryOperation}s in the batch than originally tried (if the 
 * original batch size was less than {@link #getBatchSize()})</p>
 * 
 * <p>NOTE: While it is possible to use a {@link Publisher} to publish
 * individual {@link EntryOperation}s one at a time, this is generally not recommended, 
 * especially for WAN environments where the round-trip latency of publishing a single 
 * operation is usually high.  Consequently it is often far more efficient 
 * to use batches (to reduce the number of round-trips).</p>
 *
 * @author Brian Oliver
 */
public interface BatchPublisher {

	/**
	 * <p>Start the {@link BatchPublisher} so that a
	 * {@link PublishingService} may commence publishing
	 * {@link EntryOperation}s with {@link #publishBatch(SubscriptionIdentifier, Iterator)}.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public void start(SubscriptionIdentifier subscriptionIdentifier);
	
	
	/**
	 * <p>Stop the {@link BatchPublisher}.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public void stop(SubscriptionIdentifier subscriptionIdentifier);
	
	
	/**
	 * <p>Returns if the {@link PublishingService} for the {@link BatchPublisher} 
	 * should be automatically started when the {@link BatchPublisher} is registered
	 * (as a {@link PublishingSubscription})
	 */
	public boolean autostart();
	
	
	/**
	 * <p>Return the number of milliseconds we should wait 
	 * before we retry publishing after an error occurs
	 * when calling {@link #publishBatch(SubscriptionIdentifier, Iterator)}.</p>
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
	 * <p>Returns the number of milliseconds to wait between 
	 * attempts to publish batches of {@link EntryOperation}s.</p>
	 * 
	 * <p>NOTE: This value should be greater than zero.  If
	 * the value is close to zero, very little batching may 
	 * occur.</p>
	 */
	public long getBatchPublishingDelay();

	
	/**
	 * <p>Returns the maximum number of {@link EntryOperation}s 
	 * that may be "batched" together in an individual batch.</p>
	 * 
	 * <p>NOTE: This value should be greater than one.  If 
	 * the value is one no batching will occur.</p>
	 */
	public int getBatchSize();
	
	
	/**
	 * <p>Publish the specified {@link EntryOperation}s in the 
	 * order in which they produced by the {@link Iterator}.</p>
	 * 
	 * <p>NOTE: If publishing fails, the entire collection
	 * represented by the {@link Iterator} may be retried. 
	 * (not simply where it left off).  Consequently this method
	 * should be idempotent.</p>
	 * 
	 * @param subscriptionIdentifier
	 * @param entryOperations
	 */
	public void publishBatch(SubscriptionIdentifier subscriptionIdentifier, 
							 Iterator<EntryOperation> entryOperations);
}
