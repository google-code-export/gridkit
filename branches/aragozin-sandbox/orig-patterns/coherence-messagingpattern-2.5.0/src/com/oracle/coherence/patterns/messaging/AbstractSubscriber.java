/*
 * File: AbstractSubscriber.java
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

import java.util.concurrent.LinkedBlockingQueue;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.leasing.Lease;
import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.CommandSubmitter;
import com.oracle.coherence.patterns.messaging.exceptions.SubscriberInterruptedException;
import com.oracle.coherence.patterns.messaging.exceptions.SubscriptionLostException;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.processor.UpdaterProcessor;

/**
 * <p>The base implementation of a {@link Subscriber} for
 * {@link LeasedSubscription}s.</p>
 * 
 * @author Brian Oliver
 */
abstract class AbstractSubscriber<S extends LeasedSubscription> implements Subscriber {

	/**
	 * <p>The {@link MessagingSession} that owns this {@link Subscriber}.</p>
	 */
	private MessagingSession messagingSession;
	
	
	/**
	 * <p>The {@link CommandSubmitter} for the messaging layer.  This is
	 * used to send {@link Command}s to the {@link Destination}s.</p>
	 */
	private CommandSubmitter commandSubmitter;
	
	
	/**
	 * <p>The {@link Identifier} allocated to the {@link Subscriber}.</p>
	 */
	private SubscriptionIdentifier subscriptionIdentifier;

	
	/**
	 * <p>This {@link Queue} is used by the {@link Subscriber} to wait for updates
	 * on it's {@link Subscription} to arrive.  This is especially especially useful
	 * to we known when {@link Message}s have arrived.</p>
	 */
	private LinkedBlockingQueue<S> subscriptionUpdates;

	
	/**
	 * <p>Should messages received during a getMessage be automatically 
	 * acknowledged (and thus committed as read).</p>
	 */
	private boolean autocommit;
	
	
	/**
	 * <p>The {@link MapListener} that will be used to receive call back events
	 * (and new {@link Subscription} state) from the cluster.</p>
	 */
	private MapListener mapListener;
	
	
	/**
	 * <p>A {@link Thread} that is responsible for maintaining the
	 * {@link Lease} associated with this {@link Subscription}.</p>
	 */
	private LeaseMaintainerThread leaseMaintainerThread;
	
	
	/**
	 * <p>The internal {@link State} of the {@link Subscriber}.</p>
	 */
	protected enum State {
		/**
		 * <p><code>Starting</code> indicates that the {@link Subscriber} has started
		 * but is yet to receive it's {@link Subscription} state from the cluster.</p>
		 */
		Starting,
		
		/**
		 * <p><code>Active</code> indicates that the {@link Subscriber} is ready for use.</p>
		 */
		Active,
		
		/**
		 * <p><code>Interrupted</code> indicates that the {@link Subscriber} was interrupted while
		 * attempting to perform some action.  This is a terminating state.</p>
		 */
		Interrupted,
		
		/**
		 * <p><code>Removed</code> indicates that the {@link Subscription} for the {@link Subscriber}
		 * has been removed.  This is a terminating state.</p>
		 */
		Removed,
		
		/**
		 * <p><code>Shutdown</code> indicates that the {@link Subscriber} has been shutdown.
		 * This is a terminating state.</p>
		 */
		Shutdown
	} 
	protected State state;
	
	
	/**
	 * <p>Protected Constructor</p>
	 * 
	 * @param messagingSession The {@link MessagingSession} that owns this {@link Subscriber}.
	 * @param commandSubmitter
	 * @param subscriptionIdentifier
	 */
	protected AbstractSubscriber(MessagingSession messagingSession,
								 CommandSubmitter commandSubmitter,
								 SubscriptionIdentifier subscriptionIdentifier) {
		
		this.messagingSession = messagingSession;
		this.commandSubmitter = commandSubmitter;
		this.subscriptionIdentifier = subscriptionIdentifier;
		this.subscriptionUpdates = new LinkedBlockingQueue<S>();
		this.autocommit = true;
		this.state = State.Starting;
		
		//register a MapListener for keeps our local copy of the subscription up-to-date
		this.mapListener = new MapListener() {
			@SuppressWarnings("unchecked")
			public void entryInserted(MapEvent mapEvent) {
				if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "%s received insert event %s", this, mapEvent);

				//added the newly arrived subscription state to the queue for internal processing
				subscriptionUpdates.offer((S)mapEvent.getNewValue());
			}
			
			@SuppressWarnings("unchecked")
			public void entryUpdated(MapEvent mapEvent) {
				if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "%s received update event %s", this, mapEvent);
				
				S oldSubscription = (S)mapEvent.getOldValue();
				S newSubscription = (S)mapEvent.getNewValue();

				//only queue the updated subscription if the event was a result of;
				//1. the visible range has increased in size (ie: a message was delivered)
				//2. a lease was unsuspended 
				if (oldSubscription.getVisibleMessageRange().size() < newSubscription.getVisibleMessageRange().size() ||
					(((LeasedSubscription)oldSubscription).getLease().isSuspended() && !((LeasedSubscription)newSubscription).getLease().isSuspended())) {
					
					//added the updated arrived subscription state to the queue for internal processing
					subscriptionUpdates.offer((S)mapEvent.getNewValue());
				}
			}
			
			@SuppressWarnings("unchecked")
			public void entryDeleted(MapEvent mapEvent) {
				if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "%s received delete event %s", this, mapEvent);
				
				//shutdown the subscriber
				shutdown(State.Removed);
				
				//added the removed subscription state to the queue for internal processing
				subscriptionUpdates.offer((S)mapEvent.getOldValue());
			}
		};
		CacheFactory.getCache(Subscription.CACHENAME).addMapListener(mapListener, subscriptionIdentifier, false);

		//create and start our lease maintenance thread
		leaseMaintainerThread = new LeaseMaintainerThread(this);	
		leaseMaintainerThread.setDaemon(true);
		leaseMaintainerThread.setName(String.format("LeaseMaintainerThread[%s]", subscriptionIdentifier.getSubscriberIdentifier()));
		leaseMaintainerThread.start();
	}
	
	
	/**
	 * {@inheritDoc} 
	 */
	public MessagingSession getMessagingSession() {
		return messagingSession;
	}
	
	
	/**
	 * {@inheritDoc} 
	 */
	CommandSubmitter getCommandSubmitter() {
		return commandSubmitter;
	}
	

	/**
	 * <p>Returns the {@link State} of the {@link Subscriber}.</p>
	 */
	protected synchronized State getState() {
		return state;
	}


	/**
	 * <p>Returns the current {@link State} of the {@link Subscriber}.</p>
	 */
	protected synchronized void setState(State state) {
		//ensure to don't try to "reset" from a terminating state
		if (this.state != State.Removed && 
			this.state != State.Interrupted &&
			this.state != State.Shutdown) {
			
			this.state = state;
		}
	}


	/**
	 * <p>Returns the next state update of the {@link Subscription} for this {@link Subscriber}.
	 * If an updated {@link Subscription} state is not available 
	 * (ie: has not been sent to the {@link Subscriber}), this method waits until one 
	 * arrives (via the {@link MapListener}).</p> 
	 */
	protected S getNextSubscriptionUpdate() {
		try {
			//get the next available subscription state
			//(wait's if it has not arrived)
			S subscription = subscriptionUpdates.take();
			
			//set the state to active
			setState(State.Active);
			
			return subscription;
			
		} catch (InterruptedException interruptedException) {
			if (Logger.isEnabled(Logger.WARN)) Logger.log(Logger.WARN, "Subscriber was interrupted while waiting for its subscription state to arrive.\n%s", interruptedException);
			
			//attempt to clean up the subscriber (local resources)
			shutdown(State.Interrupted);
			
			//throw a suitable runtime exception
			throw new SubscriberInterruptedException(
				getSubscriptionIdentifier(), 
				"Subscriber was interrupted while waiting for subscription state to arrive",
				interruptedException
			);
		}		
	}
	
	
	/**
	 * <p>Ensures that we have a {@link Subscription} from which we may retrieve {@link Message}s.</p>
	 */
	void ensureSubscription() {
		State state = getState();
		
		if (state == State.Starting) {
			//wait for the subscription state to arrive
			getNextSubscriptionUpdate();
			
		} else if (state == State.Removed) {
			//throw a suitable runtime exception
			throw new SubscriptionLostException(
				getSubscriptionIdentifier(), 
				"Subscriber subscription was lost (with expired or the underlying destination has been removed)"
			);
			
		} else if (state == State.Shutdown) {
			//throw a suitable runtime exception
			throw new SubscriptionLostException(
				getSubscriptionIdentifier(), 
				"Attempting to use a Subscription that has been previously released or shutdown"
			);
			
		} else if (state == State.Interrupted) {
			//throw a suitable runtime exception
			throw new SubscriptionLostException(
				getSubscriptionIdentifier(), 
				"Attempting to use a Subscription that was previously interrupted and shutdown"
			);
		}
	}
	
	
	/**
	 * {@inheritDoc} 
	 */
	public SubscriptionIdentifier getSubscriptionIdentifier() {
		return subscriptionIdentifier;
	}

	
	/**
	 * {@inheritDoc} 
	 */
	public Identifier getDestinationIdentifier() {
		return subscriptionIdentifier.getDestinationIdentifier();
	}
	

	/**
	 * {@inheritDoc} 
	 */	
	public boolean isActive() {
		return getState() == State.Starting || getState() == State.Active;
	}
	
	
	/**
	 * <p>Ensures that the {@link Subscription} for the {@link Subscriber}
	 * is still valid and that the {@link Subscriber} instance may be used 
	 * to receive {@link Message}s</p>
	 */
	void ensureActive() {
		if (!isActive()) 
			throw new SubscriptionLostException(getSubscriptionIdentifier(), "Attempted to use an inactive Subscriber");
	}
	
	
	/**
	 * <p>Shutdowns the local resources for the {@link Subscriber}.</p>
	 */
	protected synchronized void shutdown(State finalState) {
		if (isActive()) {
			//clean up the map listener
			CacheFactory.getCache(Subscription.CACHENAME).removeMapListener(mapListener, subscriptionIdentifier);
			this.mapListener = null;

			//we're now shutdown
			setState(finalState);
			
			//shutdown the lease maintainer - it's no longer needed
			if (leaseMaintainerThread != null &&
				leaseMaintainerThread.isAlive())
				leaseMaintainerThread.terminate();
		}		
	}

	
	/**
	 * {@inheritDoc} 
	 */
	public void unsubscribe() {
		if (isActive()) {
			//shutdown local resources for the subscription
			shutdown(State.Shutdown);
			
			//cancel the lease on the subscription to trigger (asynchronously) rollback and clean up 
			//the subscription
			CacheFactory.getCache(Subscription.CACHENAME).invoke(
				subscriptionIdentifier,
				new UpdaterProcessor("getLease.setIsCanceled", Boolean.TRUE)
			);
		}
	}
	
	
	/**
	 * {@inheritDoc} 
	 */	
	public boolean isAutoCommitting() {
		return autocommit;
	}
	
	
	/**
	 * {@inheritDoc} 
	 */	
	public void setAutoCommit(boolean autoCommit) {
		if (autocommit && !this.autocommit)
			this.rollback();
		this.autocommit = autoCommit;
	}
	
	
	/**
	 * <p>A {@link Thread} that will attempt to continuously maintain 
	 * the {@link Lease} for the specified {@link QueueSubscription}.</p>
	 * 
	 *
	 */
	static class LeaseMaintainerThread extends Thread {
		
		/**
		 * <p>The duration to wait before attempting to refresh the lease for
		 * the {@link Subscriber}.</p>
		 */
		private static final long LEASE_REFRESH_TIME_MS = 1000 * 2; //one second
		
		
		/**
		 * <p>The {@link Subscriber} for which we are maintaining it's {@link Lease}.</p>
		 */
		private Subscriber subscriber;
		
		
		/**
		 * <p>Should the {@link LeaseMaintainerThread} be terminated at the next opportunity?</p>
		 */
		private boolean isTerminated;
		

		/**
		 * <p>Standard Constructor.</p>
		 * 
		 * @param subscriber
		 */
		public LeaseMaintainerThread(Subscriber subscriber) {
			this.subscriber = subscriber;
			this.isTerminated = false;
		}
		
		
		/**
		 * <p>Terminate this {@link LeaseMaintainerThread} at the next opportunity.</p>
		 */
		public synchronized void terminate() {
			this.isTerminated = true;
		}
		
		
		/**
		 * <p>Returns if the {@link LeaseMaintainerThread} should be terminated at the next opportunity.</p>
		 */
		public synchronized boolean isTerminated() {
			return isTerminated;
		}
		
		
		/**
		 * {@inheritDoc} 
		 */	
		public void run() {
			if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "LeaseMaintainerThread for %s commenced", subscriber.getSubscriptionIdentifier());
			while (!isTerminated()) {
				try {
					if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "LeaseMaintainerThread for %s sleeping for %s ms", subscriber.getSubscriptionIdentifier(), LEASE_REFRESH_TIME_MS);
					Thread.sleep(LEASE_REFRESH_TIME_MS);
					
					//update the lease (using an entry processor)
					if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "LeaseMaintainerThread for %s extending current lease", subscriber.getSubscriptionIdentifier());
					CacheFactory.getCache(Subscription.CACHENAME).invoke(
						subscriber.getSubscriptionIdentifier(),
						new UpdaterProcessor("getLease.extend", LEASE_REFRESH_TIME_MS * 2)
					);
					
					//TODO: ensure that the lease was updated.  if it wasn't then we should to terminate
					
					if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "LeaseMaintainerThread for %s has extended the lease", subscriber.getSubscriptionIdentifier());
					
				} catch (InterruptedException interruptedException) {
					if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "LeaseMaintainerThread for %s interrupted", subscriber.getSubscriptionIdentifier());
				}
			}
			if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "LeaseMaintainerThread for %s completed", subscriber.getSubscriptionIdentifier());
		}
	}
}
