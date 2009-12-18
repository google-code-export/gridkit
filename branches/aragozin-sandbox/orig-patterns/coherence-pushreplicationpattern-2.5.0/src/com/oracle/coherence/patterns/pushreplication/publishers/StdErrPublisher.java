/*
 * File: StdErrPublisher.java
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
package com.oracle.coherence.patterns.pushreplication.publishers;

import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.oracle.coherence.patterns.pushreplication.EntryOperation;
import com.oracle.coherence.patterns.pushreplication.Publisher;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PortableObject;

/**
 * <p>A simple {@link Publisher} implementation that outputs all
 * {@link EntryOperation}s to stderr.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class StdErrPublisher extends AbstractPublisher {

	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * <p>Also required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public StdErrPublisher() {
		super(0);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void start(SubscriptionIdentifier subscriptionIdentifier) {
		System.err.printf("Started: subscription=%s\n", subscriptionIdentifier);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void stop(SubscriptionIdentifier subscriptionIdentifier) {
		System.err.printf("Stopped: subscription=%s\n", subscriptionIdentifier);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void publish(EntryOperation entryOperation) {
		System.err.printf("cacheName=%s, operation=%s, key=%s, value=%s\n",
						  entryOperation.getCacheName(),
					      entryOperation.getOperation(),
					      entryOperation.getKey(),
					      entryOperation.getValue());
	}
}
