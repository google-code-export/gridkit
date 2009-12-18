/*
 * File: Subscription.java
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link ConfigurableSubscription} is a {@link Subscription} that
 * supports configuration through the use of a {@link SubscriptionConfiguration}.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public abstract class ConfigurableSubscription<C extends SubscriptionConfiguration> extends Subscription {
	
	/**
	 * <p>The {@link SubscriptionConfiguration} for the {@link ConfigurableSubscription}.</p>
	 */
	private C configuration;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public ConfigurableSubscription() {
	}


	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param subscriptionIdentifier
	 * @param configuration
	 */
	public ConfigurableSubscription(SubscriptionIdentifier subscriptionIdentifier,
									C configuration) {
		super(subscriptionIdentifier);
		this.configuration = configuration;
	}

	
	/**
	 * <p>Returns the {@link SubscriptionConfiguration} for the {@link ConfigurableSubscription}.</p>
	 */
	public C getConfiguration() {
		return configuration;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		this.configuration = (C)ExternalizableHelper.readObject(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		ExternalizableHelper.writeObject(out, configuration);
	}

	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
		this.configuration = (C)reader.readObject(100);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
		writer.writeObject(100, configuration);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("ConfigurableSubscription{%s, configuration=%s}",
							 super.toString(),
							 configuration);
	}	
}
