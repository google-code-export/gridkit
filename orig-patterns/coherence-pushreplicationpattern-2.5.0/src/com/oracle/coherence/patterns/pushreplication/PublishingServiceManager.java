/*
 * File: PublishingServiceManager.java
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

import java.util.concurrent.ConcurrentHashMap;

import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;

/**
 * <p>The {@link PublishingServiceManager} is responsible of managing a
 * set of {@link PublishingService}s for {@link PublishingSubscription}s 
 * (ie: individual named caches and destinations).</p>
 *
 * @author Brian Oliver
 * @author Nick Gregory
 */
public final class PublishingServiceManager {

	/**
	 * <p>The set of {@link PublishingService}s being managed.</p>
	 */
	private static ConcurrentHashMap<SubscriptionIdentifier, PublishingService> publishingServices;
	
	
	/**
	 * <p>Returns (including potentially creating) a {@link PublishingService} for
	 * the specified {@link PublishingSubscription}.</p>
	 * 
	 * @param publishingSubscription
	 */
	public static PublishingService getPublishingService(PublishingSubscription publishingSubscription) {
		PublishingService publishingService;

		if (!publishingServices.containsKey(publishingSubscription.getIdentifier())) {	
			
			publishingService = new PublishingService(publishingSubscription.getIdentifier(), 
										 			  publishingSubscription.createBatchPublisher());
			PublishingService previousPublishingService = publishingServices.putIfAbsent(publishingSubscription.getIdentifier(), publishingService);
			
			if (previousPublishingService != null)
				publishingService = previousPublishingService;
			
		} else {
			publishingService = publishingServices.get(publishingSubscription.getIdentifier());
		}
		
		return publishingService;
	}
	
	
	/**
	 * <p>Removes and stops the {@link PublishingService} for a specified {@link PublishingSubscription}
	 * {@link SubscriptionIdentifier}.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public static void stopPublishingService(SubscriptionIdentifier subscriptionIdentifier) {
		publishingServices.remove(subscriptionIdentifier).stop();
	}
	
	
	static {
		publishingServices = new ConcurrentHashMap<SubscriptionIdentifier, PublishingService>();
	}
}
