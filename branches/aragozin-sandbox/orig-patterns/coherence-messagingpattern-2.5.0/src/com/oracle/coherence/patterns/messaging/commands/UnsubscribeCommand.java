/*
 * File: UnsubscribeCommand.java
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
import com.oracle.coherence.patterns.messaging.Subscription;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.processor.ExtractorProcessor;

/**
 * <p>The {@link UnsubscribeCommand} is used to unsubscribe a {@link Subscription} from  
 * a {@link Destination}, including recovering any unconsummed messages</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class UnsubscribeCommand implements PriorityCommand<Destination>, ExternalizableLite, PortableObject {

	/**
	 * <p>The {@link Identifier} of the {@link Subscription} to unsubscribe from the {@link Destination}.</p>
	 */
	private SubscriptionIdentifier subscriptionIdentifier;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public UnsubscribeCommand() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier The identifier of the subscriber
	 */
	public UnsubscribeCommand(SubscriptionIdentifier subscriptionIdentifier) {
		this.subscriptionIdentifier = subscriptionIdentifier;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void execute(ExecutionEnvironment<Destination> executionEnvironment) {
		if (Logger.isEnabled(Logger.QUIET)) Logger.log(Logger.QUIET, "Unsubscribing %s", subscriptionIdentifier);
		
		//get the visible message range from either environment or subscription (INC-165)
		Range visibleMessageRange = (Range)executionEnvironment.loadCheckpoint();
		if (visibleMessageRange == null) {
			visibleMessageRange = (Range)CacheFactory.getCache(Subscription.CACHENAME).invoke(
				subscriptionIdentifier, 
				new ExtractorProcessor("getVisibleMessageRange")
			);

			//save the visible message range as a checkpoint 
			//just in case we fail after this (INC-165)
			executionEnvironment.saveCheckpoint(visibleMessageRange);
		}
		
		//delegate the implementation to the underlying destination
		executionEnvironment.getContext().unsubscribe(
			executionEnvironment, 
			subscriptionIdentifier, 
			visibleMessageRange
		);
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
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("UnsubscribeCommand{subscriptionIdentifier=%s}", 
							 subscriptionIdentifier);
	}
}
