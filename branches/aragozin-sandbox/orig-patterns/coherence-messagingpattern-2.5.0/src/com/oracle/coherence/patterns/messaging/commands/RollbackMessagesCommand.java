/*
 * File: RollbackMessagesCommand.java
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
import com.oracle.coherence.common.ranges.Range;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.oracle.coherence.patterns.command.PriorityCommand;
import com.oracle.coherence.patterns.messaging.Destination;
import com.oracle.coherence.patterns.messaging.Message;
import com.oracle.coherence.patterns.messaging.Queue;
import com.oracle.coherence.patterns.messaging.Subscription;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>The {@link RollbackMessagesCommand} is a {@link PriorityCommand} that
 * will rollback and place a range of previously delivered messages 
 * (using their ids) back on the front of a {@link Queue}.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class RollbackMessagesCommand implements PriorityCommand<Destination>, ExternalizableLite, PortableObject {

	/**
	 * <p>The {@link Identifier} of the {@link Subscription} to rollback.</p>
	 */
	private SubscriptionIdentifier subscriptionIdentifier;
	
	
	/**
	 * <p>The {@link Range} of {@link Message} ids to rollback.</p>
	 */
	private Range messageRangeToRollback;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public RollbackMessagesCommand() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier The identifer of the subscriber
	 * @param messageRangeToRollback
	 */
	public RollbackMessagesCommand(SubscriptionIdentifier subscriptionIdentifier,
								   Range messageRangeToRollback) {
		this.subscriptionIdentifier = subscriptionIdentifier;
		this.messageRangeToRollback = messageRangeToRollback;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void execute(ExecutionEnvironment<Destination> executionEnvironment) {
		Destination destination = executionEnvironment.getContext();
		if (destination instanceof Queue) {
			//delegate the implementation to the underlying destination
			((Queue)destination).rollbackMessages(
					executionEnvironment,
					getSubscriptionIdentifier(),
					getMessageRangeToRollback()
			);
		} else {
			//an attempt has been made to rollback a message for a Topic
			//(this can't happen as rollback is a topic subscriber function, not a topic function)
			Logger.log(Logger.ERROR, "Attempted to rollback a message on %s. This is an invalid operation!", destination);
		}
	}

	
	/**
	 * <p>Returns the {@link SubscriptionIdentifier} of the {@link Subscription}
	 * for which we are rolling back {@link Message}s.</p>
	 */
	public SubscriptionIdentifier getSubscriptionIdentifier() {
		return subscriptionIdentifier;
	}
	
	
	/**
	 * <p>Returns the {@link Range} of message ids to rollback.</p>
	 */
	public Range getMessageRangeToRollback() {
		return messageRangeToRollback;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.subscriptionIdentifier = (SubscriptionIdentifier)ExternalizableHelper.readExternalizableLite(in);
		this.messageRangeToRollback = (Range)ExternalizableHelper.readObject(in);		
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeExternalizableLite(out, subscriptionIdentifier);
		ExternalizableHelper.writeObject(out, messageRangeToRollback);		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.subscriptionIdentifier = (SubscriptionIdentifier)reader.readObject(0);
		this.messageRangeToRollback = (Range)reader.readObject(1);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, subscriptionIdentifier);
		writer.writeObject(1, messageRangeToRollback);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("RollbackMessagesCommand{subscriptionIdentifier=%s, messageRangeToRollback=%s}", 
						 	 subscriptionIdentifier,
						 	 messageRangeToRollback);
	}
}
