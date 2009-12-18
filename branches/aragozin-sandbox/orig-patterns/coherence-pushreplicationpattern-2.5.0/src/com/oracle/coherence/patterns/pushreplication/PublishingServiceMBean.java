/*
 * File: PublishingServiceMBean.java
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

import com.oracle.coherence.common.ranges.Range;
import com.oracle.coherence.patterns.messaging.Message;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.oracle.coherence.patterns.pushreplication.PublishingService.State;

/**
 * <p>The {@link PublishingServiceMBean} specifies the JMX monitoring 
 * and mangability interface for {@link PublishingService}s.</p>
 * 
 * @author Nicholas Gregory
 */
public interface PublishingServiceMBean {

	/**
	 * <p>Returns the {@link SubscriptionIdentifier} of the 
	 * {@link PublishingSubscription} that is providing messages (containing {@link EntryOperation}s)
	 * to publish.</p>
	 */
	public String getSubscriptionIdentity();
	
	
	/**
	 * <p>Returns the {@link State} of the {@link PublishingService}.</p>
	 */
	public String getServiceState();	
	
	
	/**
	 * <p>Returns the last {@link Range} of {@link Message}s 
	 * (ie: {@link EntryOperation}s) that was published by the {@link PublishingService}.</p>
	 */
	public String getLastPublishedRange();
	
	
	/**
	 * <p>Returns the duration (in ms) of the last publishing batch.</p>
	 */
	public long getLastPublishingDuration();
	
	
	/**
	 * <p>Returns the minimum publishing duration (in ms).</p>
	 */
	public long getMinimumPublishingDuration();
	
	
	/**
	 *<p>Returns the maximum publishing duration (is ms).</p>
	 */
	public long getMaximumPublishingDuration();
	
	
	/**
	 *<p>Returns the total duration (in ms) the publisher has been publishing.</p>
	 */
	public long getTotalPublishingDuration();
	
	
	/**
	 * <p>Returns the current number of consecutive publishing failures.</p> 
	 */
	public int getConsecutivePublishingFailures();
	
	
	/**
	 * <p>Suspends the {@link PublishingService} (if it is either Paused, Publishing, Ready or Waiting).</p>
	 */
	public void suspend();
	
	
	/**
	 * <p>Resumes (or starts) the {@link PublishingService} (if it is Suspended).</p>
	 */
	public void resume();
	
	
	/**
	 * <p>Removes all of the currently pending {@link EntryOperation}s to publish.  The {@link PublishingService}
	 * must be <strong>suspended</strong> before this happens.</p>
	 */
	public void drain();
}
