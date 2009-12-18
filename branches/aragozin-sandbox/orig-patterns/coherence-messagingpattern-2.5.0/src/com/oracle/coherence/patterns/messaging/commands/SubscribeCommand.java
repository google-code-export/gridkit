/*
 * File: SubscribeCommand.java
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
import com.oracle.coherence.patterns.command.ExecutionEnvironment;
import com.oracle.coherence.patterns.command.PriorityCommand;
import com.oracle.coherence.patterns.messaging.Destination;
import com.oracle.coherence.patterns.messaging.Subscription;
import com.oracle.coherence.patterns.messaging.SubscriptionConfiguration;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>The {@link SubscribeCommand} is used to create a {@link Subscription} for 
 * a {@link Destination}.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class SubscribeCommand<C extends SubscriptionConfiguration> 
							    implements PriorityCommand<Destination>, ExternalizableLite, PortableObject {

	/**
	 * <p>The proposed {@link Identifier} of the {@link Subscription} to the {@link Destination}.</p>
	 */
	private SubscriptionIdentifier subscriptionIdentifier;

	
	/**
	 * <p>The {@link SubscriptionConfiguration} to use to create the subscription.</p>
	 */
	private C subscriptionConfiguration;
	
	
	/**
	 * <p>The (optional) {@link Subscription} to register.</p>
	 */
	private Subscription subscription;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public SubscribeCommand() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier The proposed identifier for the {@link Subscription}
	 * @param subscriptionConfiguration The proposed {@link SubscriptionConfiguration}
	 * @param subscription The object that will managed the state of the subscription
	 */
	public SubscribeCommand(SubscriptionIdentifier subscriptionIdentifier,
							C subscriptionConfiguration,
							Subscription subscription) {
		this.subscriptionIdentifier = subscriptionIdentifier;
		this.subscriptionConfiguration = subscriptionConfiguration;
		this.subscription = subscription;
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier The proposed identifier for the {@link Subscription}
	 * @param subscriptionConfiguration The proposed {@link SubscriptionConfiguration}
	 */
	public SubscribeCommand(SubscriptionIdentifier subscriptionIdentifier,
							C subscriptionConfiguration) {
		this(subscriptionIdentifier, subscriptionConfiguration, null);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void execute(ExecutionEnvironment<Destination> executionEnvironment) {
		//delegate the implementation to the underlying destination
		executionEnvironment.getContext().subscribe(
			executionEnvironment, 
			subscriptionIdentifier, 
			subscriptionConfiguration,
			subscription
		);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(DataInput in) throws IOException {
		this.subscriptionIdentifier = (SubscriptionIdentifier)ExternalizableHelper.readExternalizableLite(in);
		this.subscriptionConfiguration = (C)ExternalizableHelper.readExternalizableLite(in);
		this.subscription = (Subscription)ExternalizableHelper.readObject(in);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeExternalizableLite(out, subscriptionIdentifier);
		ExternalizableHelper.writeExternalizableLite(out, (ExternalizableLite)subscriptionConfiguration);
		ExternalizableHelper.writeObject(out, subscription);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(PofReader reader) throws IOException {
		this.subscriptionIdentifier = (SubscriptionIdentifier)reader.readObject(0);
		this.subscriptionConfiguration = (C)reader.readObject(1);
		this.subscription = (Subscription)reader.readObject(2);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, subscriptionIdentifier);
		writer.writeObject(1, subscriptionConfiguration);
		writer.writeObject(2, subscription);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("SubscribeCommand{subscriptionIdentifier=%s, subscriptionConfiguration=%s, subscription=%s}", 
							 subscriptionIdentifier, 
							 subscriptionConfiguration,
							 subscription);
	}
}
