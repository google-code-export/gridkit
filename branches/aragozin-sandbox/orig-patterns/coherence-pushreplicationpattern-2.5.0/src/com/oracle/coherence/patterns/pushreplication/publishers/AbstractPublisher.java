/*
 * File: AbstractPublisher.java
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
package com.oracle.coherence.patterns.pushreplication.publishers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import com.oracle.coherence.patterns.pushreplication.Publisher;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A simple abstract implementation of a {@link Publisher} that uses standard
 * Java {@link Serializable} for serialization/deserialization.</p>
 * 
 * <p>Use this class as the base class for any {@link Publisher}s.</p>
 * 
 * <p>NOTE: For production publishing, it is recommended that you extend the 
 * {@link AbstractBatchPublisher} instead of this class. Batch publishing
 * is far more efficient than one-at-a-time publishing.  Publishing with
 * {@link Publisher}s is only recommended for "local" publishing and not
 * for high-latency environments, like in a WAN.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public abstract class AbstractPublisher implements Publisher, ExternalizableLite, PortableObject {

	/**
	 * <p>The time to wait (in milliseconds) between publication failures
	 * and attempting to restart a publisher.</p>
	 */
	private long restartDelay;

	
	/**
	 * <p>The number of back-to-back publishing failures before the {@link Publisher}
	 * is suspended from publishing.</p>
	 */
	private int totalConsecutiveFailuresBeforeSuspending;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public AbstractPublisher() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param restartDelay
	 * @param totalConsecutiveFailuresBeforeSuspending
	 */
	public AbstractPublisher(long restartDelay,
							 int totalConsecutiveFailuresBeforeSuspending) {
		this.restartDelay = restartDelay;
		this.totalConsecutiveFailuresBeforeSuspending = totalConsecutiveFailuresBeforeSuspending;
	}


	/**
	 * <p>Standard Constructor (with an indefinite number of publishing failures permitted)</p>
	 * 
	 * @param restartDelay
	 */
	public AbstractPublisher(long restartDelay) {
		this(restartDelay, -1);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public long getRestartDelay() {
		return restartDelay;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public int getTotalConsecutiveFailuresBeforeSuspending() {
		return totalConsecutiveFailuresBeforeSuspending;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.restartDelay = ExternalizableHelper.readLong(in);
		this.totalConsecutiveFailuresBeforeSuspending = ExternalizableHelper.readInt(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeLong(out, restartDelay);
		ExternalizableHelper.writeInt(out, totalConsecutiveFailuresBeforeSuspending);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.restartDelay = reader.readLong(0);
		this.totalConsecutiveFailuresBeforeSuspending = reader.readInt(1);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeLong(0, restartDelay);
		writer.writeInt(1, totalConsecutiveFailuresBeforeSuspending);
	}
}
