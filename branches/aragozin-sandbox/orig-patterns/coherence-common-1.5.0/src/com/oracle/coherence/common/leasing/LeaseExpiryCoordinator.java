/*
 * File: LeaseExpiryCoordinator.java
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
package com.oracle.coherence.common.leasing;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.common.threading.ThreadFactories;
import com.oracle.coherence.common.tuples.Pair;

/**
 * <p>A {@link LeaseExpiryCoordinator} is responsible for ensuring that a collection
 * of registered {@link Lease}s are valid (ie: not expired).</p>
 * 
 * @author Brian Oliver
 */
public class LeaseExpiryCoordinator {
	
	/**
	 * <p>A singleton instance of a {@link LeaseExpiryCoordinator}.</p>
	 */
	public final static LeaseExpiryCoordinator INSTANCE = new LeaseExpiryCoordinator(1000);
	
	
	/**
	 * <p>The {@link ScheduledExecutorService} that we'll use to check the validity 
	 * of {@link Lease}s.</p>
	 */
	private ScheduledExecutorService executorService;
	
	
	/**
	 * <p>The collection of {@link Lease}s being coordinated, indexed by 
	 * their "leaseOwner" {@link Object} together with a {@link LeaseListener}
	 * that will be notified when a said {@link Lease} expires.</p>
	 */
	private ConcurrentHashMap<Object, Pair<Lease, LeaseListener>> leases;
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param leaseValidityCheckingDelay The number of milliseconds to wait before the validity
	 * 									 of the coordinated {@link Lease}s are checked.
	 */
	public LeaseExpiryCoordinator(long leaseValidityCheckingDelay) {
		this.executorService = Executors.newSingleThreadScheduledExecutor(
			ThreadFactories.newThreadFactory(true, "LeaseCoordinator", null)
		);
		this.leases = new ConcurrentHashMap<Object, Pair<Lease,LeaseListener>>();
		
		this.executorService.scheduleAtFixedRate(
			new LeaseValidator(), 
			leaseValidityCheckingDelay, 
			leaseValidityCheckingDelay, 
			TimeUnit.MILLISECONDS
		);
	}
	

	/**
	 * <p>Registers the specified {@link Lease}, owned by a specified "owner" to be coordinated
	 * (ie: monitored) by the {@link LeaseExpiryCoordinator}.</p>
	 * 
	 * <p>When the said {@link Lease} expires, the {@link LeaseExpiryCoordinator} will notify the 
	 * specified {@link LeaseListener}.</p>
	 * 
	 * @param leaseOwner
	 * @param lease
	 * @param leaseExpiryListener
	 */
	public void registerLease(Object leaseOwner,
							  Lease lease,
							  LeaseListener leaseExpiryListener) {
	
		leases.put(leaseOwner, new Pair<Lease, LeaseListener>(lease, leaseExpiryListener));
	}
	
	
	/**
	 * <p>Deregisters the previously registered {@link Lease} with the specified lease owner.</p>
	 *  
	 * @param leaseOwner
	 */
	public void deregisterLease(Object leaseOwner) {
		leases.remove(leaseOwner);
	}
	
	
	/**
	 * <p>A {@link LeaseValidator} is responsible for ensuring all of the {@link Lease}s
	 * coordinated by a {@link LeaseExpiryCoordinator} are valid.</p>
	 */
	private class LeaseValidator implements Runnable {

		/**
		 * {@inheritDoc}
		 */
		public void run() {
			try {
				if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "Validating %d Lease(s)", leases.size());
	
				long currentTime = System.currentTimeMillis();
				LinkedList<Object> expiredLeaseOwners = null;
				
				//find all of the expired leases as of now
				for(Object leaseOwner : leases.keySet()) {
					Pair<Lease, LeaseListener> pair = leases.get(leaseOwner);
					Lease lease = pair.getX();
					LeaseListener leaseListener = pair.getY();
	
					boolean deregisterLease = false;
					if (lease.isCanceled()) {
						if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "%s for %s has been canceled", lease, leaseOwner);
					
						if (leaseListener != null)
							leaseListener.onLeaseCanceled(leaseOwner, lease);
	
						deregisterLease = true;
						
					} else if (lease.isSuspended()) {
						if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "%s for %s has been suspended", lease, leaseOwner);
						
						if (leaseListener != null)
							leaseListener.onLeaseSuspended(leaseOwner, lease);
	
						deregisterLease = true;
						
					} else if (!lease.isValidAt(currentTime)) {
						if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "%s for %s has expired", lease, leaseOwner);
	
						if (leaseListener != null)
							leaseListener.onLeaseExpiry(leaseOwner, lease);
						
						deregisterLease = true;
					}
					
					if (deregisterLease) {
						if (expiredLeaseOwners == null)
							expiredLeaseOwners = new LinkedList<Object>();
						expiredLeaseOwners.add(leaseOwner);
					}
				}
				
				//remove all of the expired leases
				if (expiredLeaseOwners != null) {
					if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.DEBUG, "Deregistering %d Lease(s)", expiredLeaseOwners.size());
					for(Object leaseOwner : expiredLeaseOwners) {
						deregisterLease(leaseOwner);
					}
				}
			} catch (Exception exception) {
				Logger.log(Logger.ERROR, "LeaseValidator Failed due to\n%s\n", exception);
			}
		}
	}
}
