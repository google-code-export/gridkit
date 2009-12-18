/*
 * File: TopicSubscriptionConfiguration.java
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
 * <p>A {@link TopicSubscriptionConfiguration} provides specific configuration
 * parameters for {@link Subscription}s to a {@link Topic}.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class TopicSubscriptionConfiguration extends DefaultSubscriptionConfiguration {
	
	/**
	 * <p>The name of the {@link TopicSubscription}.</p>
	 */
	private String name;
	
	
	/**
	 * <p>Is the {@link Topic} {@link Subscription} durable.</p>
	 */
	private boolean isDurable;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public TopicSubscriptionConfiguration() {
	}
	
	
	/**
	 * <p>Private Constructor.</p>
	 * 
	 * @param name The name of the {@link TopicSubscription}
	 * @param isDurable
	 * @param leaseDuration
	 */
	private TopicSubscriptionConfiguration(String name, 
										   boolean isDurable,
										   long leaseDuration) {
		super(leaseDuration);
		
		this.name = name;
		this.isDurable = isDurable;
	}

	
	/**
	 * <p>Creates and returns a {@link TopicSubscriptionConfiguration} specifically
	 * for use by durable topic {@link Subscription}s.</p>
	 * 
	 * @param name
	 */
	public static final TopicSubscriptionConfiguration newDurableConfiguration(String name) {
		return new TopicSubscriptionConfiguration(name, true, LeasedSubscriptionConfiguration.STANDARD_LEASE_DURATION);
	}

	
	/**
	 * <p>Creates and returns a {@link TopicSubscriptionConfiguration} specifically
	 * for use by durable topic {@link Subscription}s.</p>
	 * 
	 * @param name
	 * @param leaseDuration
	 */
	public static final TopicSubscriptionConfiguration newDurableConfiguration(String name, long leaseDuration) {
		return new TopicSubscriptionConfiguration(name, true, leaseDuration);
	}

	
	/**
	 * <p>Creates and returns a {@link TopicSubscriptionConfiguration} specifically
	 * for use by non-durable topic {@link Subscription}s.</p>
	 */
	public static final TopicSubscriptionConfiguration newNonDurableConfiguration() {
		return new TopicSubscriptionConfiguration(null, false, LeasedSubscriptionConfiguration.STANDARD_LEASE_DURATION);
	}

	
	/**
	 * <p>Creates and returns a {@link TopicSubscriptionConfiguration} specifically
	 * for use by non-durable topic {@link Subscription}s.</p>
	 * 
	 * @param leaseDuration
	 */
	public static final TopicSubscriptionConfiguration newNonDurableConfiguration(long leaseDuration) {
		return new TopicSubscriptionConfiguration(null, false, leaseDuration);
	}
	
	
	/**
	 * <p>Returns the name of the {@link TopicSubscription}.</p>
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * <p>Returns if the {@link Topic} {@link Subscription} is to be durable.</p>
	 */
	public boolean isDurable() {
		return isDurable;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		this.name = ExternalizableHelper.readSafeUTF(in);
		this.isDurable = in.readBoolean();
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		ExternalizableHelper.writeSafeUTF(out, name);
		out.writeBoolean(isDurable);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
		this.name = reader.readString(100);
		this.isDurable = reader.readBoolean(101);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
		writer.writeString(100, name);
		writer.writeBoolean(101, isDurable);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("TopicSubscriptionConfiguration{%s, name=%s, isDurable=%s}",
							 super.toString(),
							 name == null ? "(n/a)" : name,
							 isDurable);
	}
}
