/*
 * File: RequestMessageProcessor.java
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
package com.oracle.coherence.patterns.messaging.entryprocessors;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.patterns.messaging.Message;
import com.oracle.coherence.patterns.messaging.Subscription;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * <p>The {@link RequestMessageProcessor} is used to request a 
 * {@link Message} for a {@link Subscription}.</p>
 * 
 * <p>The side-effect is that the {@link SubscriptionIdentifier} of the {@link Subscription}
 * is added to deliveredTo state of the {@link Message}.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class RequestMessageProcessor extends AbstractProcessor implements ExternalizableLite, PortableObject {

	/**
	 * <p>The {@link SubscriptionIdentifier} of the subscriber that is requesting the message.</p>
	 */
	private SubscriptionIdentifier subscriptionIdentifier;

	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public RequestMessageProcessor() {
	}


	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public RequestMessageProcessor(SubscriptionIdentifier subscriptionIdentifier) {
		this.subscriptionIdentifier = subscriptionIdentifier;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Object process(Entry entry) {		
		if (entry.isPresent()) {
			Message message = (Message)entry.getValue();
			message.deliveredTo(subscriptionIdentifier);
			entry.setValue(message);
			return message;
			
		} else {
			return null;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.subscriptionIdentifier = (SubscriptionIdentifier)ExternalizableHelper.readExternalizableLite(in);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeExternalizableLite(out, subscriptionIdentifier);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.subscriptionIdentifier = (SubscriptionIdentifier)reader.readObject(0);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, subscriptionIdentifier);
	}	
}
