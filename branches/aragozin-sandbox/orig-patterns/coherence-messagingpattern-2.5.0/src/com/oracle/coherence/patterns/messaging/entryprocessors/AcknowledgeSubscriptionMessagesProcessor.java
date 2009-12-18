/*
 * File: AcknowledgeSubscriptionMessagesProcessor.java
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

import com.oracle.coherence.common.ranges.Range;
import com.oracle.coherence.patterns.messaging.Message;
import com.oracle.coherence.patterns.messaging.Subscription;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * <p>The {@link AcknowledgeSubscriptionMessagesProcessor} is used 
 * record that a {@link Range} of {@link Message} have been acknowledged 
 * by a {@link Subscription}.</p>
 *  
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class AcknowledgeSubscriptionMessagesProcessor extends AbstractProcessor 
													  implements ExternalizableLite, 
													  PortableObject {

	/**
	 * <p>The {@link Range} of {@link Message} being acknowledged.</p>
	 */
	private Range messageRange;

	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public AcknowledgeSubscriptionMessagesProcessor() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param messageRange
	 */
	public AcknowledgeSubscriptionMessagesProcessor(Range messageRange) {
		this.messageRange = messageRange;
	}


	/**
	 * {@inheritDoc}
	 */
	public Object process(Entry entry) {
		if (entry.isPresent() && entry.getValue() instanceof Subscription) {
			Subscription subscription = (Subscription)entry.getValue();
			subscription.onAcknowledgeMessageRange(messageRange);		
			entry.setValue(subscription, true);
		}
		return null;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.messageRange = (Range)ExternalizableHelper.readObject(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, messageRange);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.messageRange = (Range)reader.readObject(0);
	}	
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, messageRange);
	}
}
