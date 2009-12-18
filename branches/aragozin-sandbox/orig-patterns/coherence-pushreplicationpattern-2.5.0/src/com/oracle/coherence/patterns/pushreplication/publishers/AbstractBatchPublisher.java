/*
 * File: AbstractBatchPublisher.java
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

import com.oracle.coherence.patterns.pushreplication.BatchPublisher;
import com.oracle.coherence.patterns.pushreplication.EntryOperation;
import com.oracle.coherence.patterns.pushreplication.Publisher;
import com.oracle.coherence.patterns.pushreplication.PublishingService;
import com.oracle.coherence.patterns.pushreplication.PublishingSubscription;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A simple base class for implementations of a {@link BatchPublisher} that
 * uses {@link ExternalizableLite} (or {@link PortableObject}s) as a mechanism for 
 * serialization/deserialization.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public abstract class AbstractBatchPublisher implements BatchPublisher, ExternalizableLite, PortableObject {
	
	
	/**
	 * <p>Should the {@link PublishingService} for this {@link BatchPublisher}
	 * automatically start when the {@link BatchPublisher} is registered
	 * (with a {@link PublishingSubscription}).</p>
	 */
	private boolean autostart;
	
	
	/**
	 * <p>The number of milliseconds to wait between attempts to batch
	 * publish EntryOperations.  Setting this to a value of zero, will
	 * essentially put the publisher into a continuous operations mode.
	 * That is, it will attempt to continuously publish 
	 * {@link EntryOperation}s until there is nothing to publish.</p>
	 */
	private long batchPublishingDelay;
	
	
	/**
	 * <p>The maximum number of {@link EntryOperation}s that may occur
	 * in any single batch being published.</p> 
	 */
	private int batchSize;
	
	
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
	public AbstractBatchPublisher() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param autostart
	 * @param batchPublishingDelay
	 * @param batchSize
	 * @param restartDelay
	 * @param totalConsecutiveFailuresBeforeSuspending
	 */
	public AbstractBatchPublisher(boolean autostart,
								  long batchPublishingDelay, 
								  int batchSize, 
								  long restartDelay,
								  int totalConsecutiveFailuresBeforeSuspending) {
		this.autostart = autostart;
		this.batchPublishingDelay = batchPublishingDelay;
		this.batchSize = batchSize;
		this.restartDelay = restartDelay;
		this.totalConsecutiveFailuresBeforeSuspending = totalConsecutiveFailuresBeforeSuspending;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public boolean autostart() {
		return autostart;
	}
	

	/**
	 * {@inheritDoc}
	 */
	public long getBatchPublishingDelay() {
		return batchPublishingDelay;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public int getBatchSize() {
		return batchSize;
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
		this.autostart = in.readBoolean();
		this.batchPublishingDelay = ExternalizableHelper.readLong(in);
		this.batchSize = ExternalizableHelper.readInt(in);
		this.restartDelay = ExternalizableHelper.readLong(in);
		this.totalConsecutiveFailuresBeforeSuspending = ExternalizableHelper.readInt(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		out.writeBoolean(autostart);
		ExternalizableHelper.writeLong(out, batchPublishingDelay);
		ExternalizableHelper.writeInt(out, batchSize);
		ExternalizableHelper.writeLong(out, restartDelay);
		ExternalizableHelper.writeInt(out, totalConsecutiveFailuresBeforeSuspending);
	}


	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.autostart = reader.readBoolean(0);
		this.batchPublishingDelay = reader.readLong(1);
		this.batchSize = reader.readInt(2);
		this.restartDelay = reader.readLong(3);
		this.totalConsecutiveFailuresBeforeSuspending = reader.readInt(4);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeBoolean(0, autostart);
		writer.writeLong(1, batchPublishingDelay);
		writer.writeInt(2, batchSize);
		writer.writeLong(3, restartDelay);
		writer.writeInt(4, totalConsecutiveFailuresBeforeSuspending);
	}
}
