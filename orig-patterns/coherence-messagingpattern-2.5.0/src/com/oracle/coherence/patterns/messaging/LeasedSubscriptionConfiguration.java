/*
 * File: LeasedSubscriptionConfiguration.java
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

import com.oracle.coherence.common.leasing.Lease;

/**
 * <p>A {@link LeasedSubscriptionConfiguration} specifies the standard
 * type-safe parameters required to create a {@link LeasedSubscription}, typically
 * for a {@link Subscriber}.</p>
 * 
 * @author Brian Oliver
 */
public interface LeasedSubscriptionConfiguration extends SubscriptionConfiguration {
	
	/**
	 * <p>The standard duration of a {@link Lease} for a {@link LeasedSubscription}.</p>
	 */
	public static final long STANDARD_LEASE_DURATION = 2 * 60 * 1000; //two minutes

	
	/**
	 * <p>The {@link Lease} duration specifies how long a {@link Subscription}
	 * is initially valid for use.  If the {@link Lease} is not extended, then 
	 * the {@link Subscription} is cleaned up; including a). rolling back of 
	 * any received messages, b). being removed (ie: unsubscribed) from the 
	 * system.</p>
	 * 
	 * <p>NOTE: The {@link Lease} duration has nothing to do with whether a
	 * {@link Subscription} is durable.  Durable {@link Subscription}s 
	 * (only available on {@link Topic}s) will maintain their state until 
	 * unsubscribed.</p>
	 */
	public long getLeaseDuration();
	
}
