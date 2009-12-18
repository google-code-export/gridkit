/*
 * File: DefaultContextConfiguration.java
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
package com.oracle.coherence.patterns.command;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>The default implementation of a {@link ContextConfiguration}.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class DefaultContextConfiguration implements ContextConfiguration, ExternalizableLite, PortableObject {

	/**
	 * <p>The {@link ContextConfiguration.ManagementStrategy} for the {@link ContextConfiguration}.</p>
	 */
	private ManagementStrategy managementStrategy;
	
	
	/**
	 * <p>Standard Constructor (with default values).</p>
	 * 
	 * <p>The {@link ContextConfiguration.ManagementStrategy} is set to {@link ContextConfiguration.ManagementStrategy#DISTRIBUTED} by default.</p>
	 */
	public DefaultContextConfiguration() {
		this.managementStrategy = ManagementStrategy.DISTRIBUTED;
	}
	
	
	/**
	 * <p>Standard Constructor (developer provided values).</p>
	 * 
	 * @param managementStrategy
	 */
	public DefaultContextConfiguration(ManagementStrategy managementStrategy) {
		this.managementStrategy = managementStrategy;
	}


	/**
	 * {@inheritDoc}
	 */
	public ManagementStrategy getManagementStrategy() {
		return managementStrategy;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		managementStrategy = ManagementStrategy.values()[ExternalizableHelper.readInt(in)];
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeInt(out, managementStrategy.ordinal());
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		managementStrategy = ManagementStrategy.values()[reader.readInt(0)];
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeInt(0, managementStrategy.ordinal());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("DefaultContextConfiguration{managementStrategy=%s}",
						     managementStrategy);
	}
}
