/*
 * File: DefaultSubscriptionConfiguration.java
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

import com.oracle.coherence.common.leasing.Lease;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A default implementation of a {@link LeasedSubscriptionConfiguration}.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class DefaultSubscriptionConfiguration implements LeasedSubscriptionConfiguration,
														 ExternalizableLite,
														 PortableObject {
	
	/**
	 * <p>The {@link Lease} duration.</p>
	 */
	private long leaseDuration;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public DefaultSubscriptionConfiguration() {
		this.leaseDuration = LeasedSubscriptionConfiguration.STANDARD_LEASE_DURATION;
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param leaseDuration
	 */
	public DefaultSubscriptionConfiguration(long leaseDuration) {
		this.leaseDuration = leaseDuration;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public long getLeaseDuration() {
		return leaseDuration;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.leaseDuration = ExternalizableHelper.readLong(in);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeLong(out, leaseDuration);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.leaseDuration = reader.readLong(0);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeLong(0, leaseDuration);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("DefaultSubscriptionConfiguration{leaseDuration=%d}",
							 leaseDuration);
	}
}
