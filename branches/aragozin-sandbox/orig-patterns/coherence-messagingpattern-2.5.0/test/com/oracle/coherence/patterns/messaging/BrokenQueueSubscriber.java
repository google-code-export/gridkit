/*
 * File: BrokenQueueSubscriber.java
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

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.messaging.AbstractSubscriber.State;
import com.oracle.coherence.patterns.messaging.commands.RequestMessageCommand;

/**
 * <p>A {@link BrokenQueueSubscriber} is a specialized {@link QueueSubscriber}
 * that exhibits poor behavior for the purposes of testing the messaging layer.</p>
 * 
 * <p>In particular, when attempting to {@link #getMessage()} this {@link Subscriber} will
 * terminate abruptly, just as if it's been killed.  The messaging layer should then 
 * recover any messages delivered to it.</p>
 * 
 * <p><strong>NOTE: This should not be used for real applications, just testing.</strong></p>
 * 
 * @author Brian Oliver
 */
public class BrokenQueueSubscriber implements Subscriber {

	/**
	 * <p>The {@link QueueSubscriber} that we're going to delegate onto.</p>
	 */
	private QueueSubscriber queueSubscriber;
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param queueSubscriber
	 */
	public BrokenQueueSubscriber(Subscriber subscriber) {
		assert subscriber != null && subscriber instanceof QueueSubscriber;
		this.queueSubscriber = (QueueSubscriber)subscriber;
	}


	/**
	 * {@inheritDoc}
	 */
	public void commit() {
		queueSubscriber.commit();
	}


	/**
	 * {@inheritDoc}
	 */
	public Identifier getDestinationIdentifier() {
		return queueSubscriber.getDestinationIdentifier();
	}


	/**
	 * {@inheritDoc}
	 */
	public Object getMessage() {
		//ensure that the subscriber is still active
		queueSubscriber.ensureActive();
		
		//ensure that we have a subscription from which we can retrieve messages
		queueSubscriber.ensureSubscription();

		//shutdown the subscriber now - so we fail to update leases and receive call backs
		//about messages being delivered
		queueSubscriber.shutdown(State.Shutdown);
		
		//NOW: request a message from the queue for this subscriber... it should not be delivered
		//(or lost)
		queueSubscriber.getCommandSubmitter().submitCommand(
			getDestinationIdentifier(), 
			new RequestMessageCommand(getSubscriptionIdentifier())
		);
		
		//nothing really to return here
		return null;
	}


	/**
	 * {@inheritDoc}
	 */
	public MessagingSession getMessagingSession() {
		return queueSubscriber.getMessagingSession();
	}


	/**
	 * {@inheritDoc}
	 */
	public SubscriptionIdentifier getSubscriptionIdentifier() {
		return queueSubscriber.getSubscriptionIdentifier();
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isActive() {
		return queueSubscriber.isActive();
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean isAutoCommitting() {
		return queueSubscriber.isAutoCommitting();
	}


	/**
	 * {@inheritDoc}
	 */
	public void release() {
		queueSubscriber.release();
	}


	/**
	 * {@inheritDoc}
	 */
	public void rollback() {
		queueSubscriber.rollback();
	}


	/**
	 * {@inheritDoc}
	 */
	public void setAutoCommit(boolean autoCommit) {
		queueSubscriber.setAutoCommit(autoCommit);
	}


	/**
	 * {@inheritDoc}
	 */
	public void unsubscribe() {
		queueSubscriber.unsubscribe();
	}
}
