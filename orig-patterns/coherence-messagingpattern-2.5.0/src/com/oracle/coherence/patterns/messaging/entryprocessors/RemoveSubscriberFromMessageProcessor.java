/*
 * File: RemoveSubscriberFromMessageProcessor.java
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
 * <p>The {@link RemoveSubscriberFromMessageProcessor} is used to remove the
 * {@link SubscriptionIdentifier} for a subscriber from a {@link Message}.</p>
 * 
 * <p>NOTE: This processor is used to cleanup delivered {@link Message} state when 
 * {@link Subscription}s have been removed or closed.</p>
 * 
 * <p>You should use the {@link AcknowledgeMessageProcessor} if you want to mark a 
 * {@link Message} as acknowledged and <strong>not this processor</strong></p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class RemoveSubscriberFromMessageProcessor extends AbstractProcessor implements ExternalizableLite, PortableObject {

	/**
	 * <p>The {@link SubscriptionIdentifier} of the subscriber to remove.</p>
	 */
	private SubscriptionIdentifier subscriptionIdentifier;

	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public RemoveSubscriberFromMessageProcessor() {
	}


	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public RemoveSubscriberFromMessageProcessor(SubscriptionIdentifier subscriptionIdentifier) {
		this.subscriptionIdentifier = subscriptionIdentifier;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Object process(Entry entry) {		
		if (entry.isPresent()) {
			Message message = (Message)entry.getValue();
			message.removeSubscriptionIdentifer(subscriptionIdentifier);
			if (message.isAcknowledged()) 
				entry.remove(false);
			else
				entry.setValue(message, true);
			
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
