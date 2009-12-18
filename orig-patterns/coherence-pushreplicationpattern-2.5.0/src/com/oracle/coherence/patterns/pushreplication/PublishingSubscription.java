/*
 * File: PublishingSubscription.java
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.oracle.coherence.common.backingmaplisteners.Cause;
import com.oracle.coherence.common.backingmaplisteners.LifecycleAwareCacheEntry;
import com.oracle.coherence.common.identifiers.StringBasedIdentifier;
import com.oracle.coherence.patterns.messaging.Message;
import com.oracle.coherence.patterns.messaging.Subscription;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.oracle.coherence.patterns.pushreplication.PublishingService.State;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.management.Registry;
import com.tangosol.util.MapEvent;

/**
 * <p>A {@link PublishingSubscription} is a specialized
 * {@link Subscription} that provides a {@link BatchPublisher}
 * implementation which may be used to publish {@link Message}s 
 * containing {@link EntryOperation}s.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public abstract class PublishingSubscription extends Subscription 
											 implements LifecycleAwareCacheEntry {


	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public PublishingSubscription() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param cacheName The name of the cache to which we're subscribing for updates.  
	 * @param subscriberName The name of the subscriber for this {@link PublishingSubscription}
	 */
	public PublishingSubscription(String cacheName, 
								  String subscriberName) {
		super(new SubscriptionIdentifier(StringBasedIdentifier.newInstance(cacheName), subscriberName));
	}
	
	
	/**
	 * <p>Create and return an appropriate {@link BatchPublisher} that
	 * can be used to publish {@link EntryOperation}s that are sent
	 * to the {@link PublishingSubscription}.</p> 
	 */
	public abstract BatchPublisher createBatchPublisher();

	
	/**
	 * {@inheritDoc}
	 */
	public void onCacheEntryLifecycleEvent(MapEvent mapEvent, Cause cause) {
		
 		//for newly created publishing subscriptions, schedule the start of an associated publishing service
 		if (mapEvent.getId() == MapEvent.ENTRY_INSERTED) {
 			
			if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG))
				CacheFactory.log(String.format("Scheduling the PublishingService for %s to start.", this), CacheFactory.LOG_DEBUG);

			final PublishingService publishingService = PublishingServiceManager.getPublishingService(this);
			
			//attempt to register the MBean for this PublishingService (may fail if JMX is not enabled)
			Registry registry = CacheFactory.ensureCluster().getManagement();
			if (registry != null) {
				String mBeanName = registry.ensureGlobalName("type=PublishingService,id=" + this.getIdentifier().getSubscriberIdentifier());
				publishingService.setMBeanName(mBeanName);
				registry.register(mBeanName, publishingService);
			}
			
			//start the publisher (if necessary)
			if (publishingService.getState() == State.Paused) {
	 			publishingService.setState(State.Starting);
	 			publishingService.schedule(new Runnable() {
	 				public void run() {
	 					publishingService.start();
	 				}
	 			}, 0, TimeUnit.MILLISECONDS);
			}
 			
 		//for deleted publishing subscriptions, schedule the stopping of the associated publishing service
 		} else if (mapEvent.getId() == MapEvent.ENTRY_DELETED) {
 			
			if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG))
				CacheFactory.log(String.format("Scheduling the PublishingService for %s to stop.", this), CacheFactory.LOG_DEBUG);
 			
 			final PublishingService publishingService = PublishingServiceManager.getPublishingService(this);
 			publishingService.setState(State.Stopping);
 			publishingService.schedule(new Runnable() {
 				public void run() {
 					publishingService.stop();
 				}
 			}, 0, TimeUnit.MILLISECONDS);

 			//remove registered mBean (if it was registered)
 			Registry registry = CacheFactory.ensureCluster().getManagement();
 			if (registry != null) {
 				registry.unregister(publishingService.getMBeanName());
 			}			
 			
 		//when publishing subscriptions are updated that are not-using batch-based publishers
 		//we need to schedule publishing to start (if not already doing so)	
 		} else if (mapEvent.getId() == MapEvent.ENTRY_UPDATED) {

 			final PublishingService publishingService = PublishingServiceManager.getPublishingService(this);

			if (CacheFactory.isLogEnabled(CacheFactory.LOG_QUIET))
				CacheFactory.log(String.format("Scheduling the PublishingService for %s to publish available messages.", this), CacheFactory.LOG_QUIET);
			
 			if (hasVisibleMessages() && publishingService.getState() == State.Ready) {
 				publishingService.schedule(new Runnable() {
 	 				public void run() {
 	 					publishingService.publish();
 	 				}
 	 			}, 0, TimeUnit.MILLISECONDS);
 			}
 		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("PublishingSubscription{%s}", super.toString());
	}
}
 