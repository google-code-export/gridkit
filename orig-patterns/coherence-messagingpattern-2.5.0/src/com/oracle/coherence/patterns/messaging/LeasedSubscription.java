/*
 * File: LeasedSubscription.java
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.common.backingmaplisteners.Cause;
import com.oracle.coherence.common.backingmaplisteners.LifecycleAwareCacheEntry;
import com.oracle.coherence.common.leasing.Lease;
import com.oracle.coherence.common.leasing.LeaseExpiryCoordinator;
import com.oracle.coherence.common.leasing.LeaseListener;
import com.oracle.coherence.common.leasing.Leased;
import com.oracle.coherence.common.leasing.Leasing;
import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.patterns.command.DefaultCommandSubmitter;
import com.oracle.coherence.patterns.messaging.commands.UnsubscribeCommand;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.MapEvent;

/**
 * <p>A {@link LeasedSubscription} is a {@link Subscription} that has a 
 * specific life-time defined by a {@link Lease}.</p>
 *  
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public abstract class LeasedSubscription extends ConfigurableSubscription<LeasedSubscriptionConfiguration> 
										 implements Leased, 
							   			  			LeaseListener,
							   			  		    LifecycleAwareCacheEntry {
	
	
	/**
	 * <p>The {@link Lease} that defines the life-time of the {@link Subscription}.  
	 * When the {@link Lease} expires, the {@link Subscription} is no longer valid.</p>
	 */
	private Lease lease;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public LeasedSubscription() {
	}

	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier
	 * @param leasedSubscriptionConfiguration
	 * @param creationTime The time (since in the epoc in milliseconds) when the {@link Subscription} was created.
	 */
	public LeasedSubscription(SubscriptionIdentifier subscriptionIdentifier,
						      LeasedSubscriptionConfiguration leasedSubscriptionConfiguration,
							  long creationTime) {
		super(subscriptionIdentifier, leasedSubscriptionConfiguration);
		this.lease = Leasing.newLease(creationTime, leasedSubscriptionConfiguration.getLeaseDuration());		
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Lease getLease() {
		return lease;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void onLeaseCanceled(Object leaseOwner, Lease lease) {
		if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.DEBUG, "The lease for %s has been canceled. Requesting to unsubscribe", this);

		DefaultCommandSubmitter.getInstance().submitCommand(
			getIdentifier().getDestinationIdentifier(),
			new UnsubscribeCommand(getIdentifier())
		);	
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void onLeaseExpiry(Object leaseOwner, Lease lease) {
		if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.DEBUG, "The lease for %s has expired. Requesting to unsubscribe", this);

		DefaultCommandSubmitter.getInstance().submitCommand(
			getIdentifier().getDestinationIdentifier(),
			new UnsubscribeCommand(getIdentifier())
		);	
	}
	
		
	/**
	 * {@inheritDoc}
	 */
	public void onCacheEntryLifecycleEvent(MapEvent mapEvent, Cause cause) {
		if (mapEvent.getId() == MapEvent.ENTRY_INSERTED ||
			mapEvent.getId() == MapEvent.ENTRY_UPDATED) {
			
			//update the lease coordinator with the lease for this subscription
			LeaseExpiryCoordinator.INSTANCE.registerLease(getIdentifier(), getLease(), this);
			
		} else {
			//notify the lease coordinator that this subscription has been removed
			LeaseExpiryCoordinator.INSTANCE.deregisterLease(getIdentifier());
		}
	}	

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		this.lease = (Lease)ExternalizableHelper.readObject(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		ExternalizableHelper.writeObject(out, lease);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
		this.lease = (Lease)reader.readObject(200);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
		writer.writeObject(200, lease);
	}	
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("LeasedSubscription{%s, lease=%s}", 
							 super.toString(),
							 lease);
	}
}
