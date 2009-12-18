/*
 * File: RequestMessageCommand.java
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
package com.oracle.coherence.patterns.messaging.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.oracle.coherence.patterns.messaging.Destination;
import com.oracle.coherence.patterns.messaging.Message;
import com.oracle.coherence.patterns.messaging.Queue;
import com.oracle.coherence.patterns.messaging.Subscriber;
import com.oracle.coherence.patterns.messaging.Subscription;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link RequestMessageCommand} is used by {@link Subscriber} 
 * implementations to request that a {@link Message} be allocated to a
 * {@link Subscription}.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class RequestMessageCommand implements Command<Destination>, ExternalizableLite, PortableObject {

	/**
	 * <p>The {@link Identifier} of the {@link Subscription} that
	 * requires a {@link Message}.</p>
	 */
	private SubscriptionIdentifier subscriptionIdentifier;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public RequestMessageCommand() {
	}

	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier
	 */
	public RequestMessageCommand(SubscriptionIdentifier subscriptionIdentifier) {
		this.subscriptionIdentifier = subscriptionIdentifier;
	}


	/**
	 * {@inheritDoc}
	 */
	public void execute(ExecutionEnvironment<Destination> executionEnvironment) {
		Destination destination = executionEnvironment.getContext();
		if (destination instanceof Queue) {
			//delegate the implementation to the underlying destination
			((Queue)destination).requestMessage(
					executionEnvironment,
					getSubscriptionIdentifier()
			);
		} else {
			//an attempt has been made to request a message from a Topic.  This is not possible!
			Logger.log(Logger.ERROR, "An attempt has been made to request a message from %s.  This is an invalid operation", destination);
		}
	}

	
	/**
	 * <p>The {@link Identifier} of the {@link Subscription} that requires
	 * the {@link Message}.</p>
	 */
	public SubscriptionIdentifier getSubscriptionIdentifier() {
		return subscriptionIdentifier;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.subscriptionIdentifier = (SubscriptionIdentifier)ExternalizableHelper.readObject(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, subscriptionIdentifier);
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
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("RequestMessageCommand{subscriptionIdentifier=%s}", subscriptionIdentifier);
	}
}
