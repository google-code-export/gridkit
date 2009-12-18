/*
 * File: BatchPublisherAdapter.java
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
import java.util.Iterator;

import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.oracle.coherence.patterns.pushreplication.BatchPublisher;
import com.oracle.coherence.patterns.pushreplication.EntryOperation;
import com.oracle.coherence.patterns.pushreplication.Publisher;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link BatchPublisherAdapter} adapts {@link Publisher} implementations
 * so that they may be used as {@link BatchPublisher}s.  Ideally however
 * all publishers should implement the {@link BatchPublisher} interface 
 * instead of the {@link Publisher} interface.  Batch publishing can be
 * far more efficient than one-at-a-time publishing.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public final class BatchPublisherAdapter extends AbstractBatchPublisher {

	/**
	 * <p>The {@link Publisher} that we are adapting to  
	 * provide a {@link BatchPublisher} implementation.</p>
	 */
	private Publisher publisher;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public BatchPublisherAdapter() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param publisher
	 * @param autostart
	 * @param batchPublishingDelay
	 * @param batchSize
	 * @param restartDelay
	 * @param totalConsecutiveFailuresBeforeSuspending
	 */
	public BatchPublisherAdapter(Publisher publisher, 
								 boolean autostart,
								 long batchPublishingDelay, 
								 int batchSize,
								 long restartDelay,
								 int totalConsecutiveFailuresBeforeSuspending) {
		super(autostart, batchPublishingDelay, batchSize, restartDelay, totalConsecutiveFailuresBeforeSuspending);
		this.publisher = publisher;
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param publisher
	 * @param autostart
	 * @param batchPublishingDelay
	 * @param batchSize
	 */
	public BatchPublisherAdapter(Publisher publisher,
								 boolean autostart,
								 long batchPublishingDelay, 
								 int batchSize) {
		super(autostart, batchPublishingDelay, batchSize, publisher.getRestartDelay(), publisher.getTotalConsecutiveFailuresBeforeSuspending());
		this.publisher = publisher;
	}
	
	
	/**
	 * <p>Standard Constructor (that automatically starts,  
	 * defaults to a batch publishing delay of 0 milliseconds,
	 * a batch size of 1 and indefinite consecutive failures)</p>
	 * 
	 * @param publisher
	 */
	public BatchPublisherAdapter(Publisher publisher) {
		this(publisher, true, 0, 1, publisher.getRestartDelay(), publisher.getTotalConsecutiveFailuresBeforeSuspending());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void start(SubscriptionIdentifier subscriptionIdentifier) {
		publisher.start(subscriptionIdentifier);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void stop(SubscriptionIdentifier subscriptionIdentifier) {
		publisher.stop(subscriptionIdentifier);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public long getRestartDelay() {
		return publisher.getRestartDelay();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void publishBatch(SubscriptionIdentifier subscriptionIdentifier, 
							 Iterator<EntryOperation> entryOperations) {
		//publish each of the entry operations in order
		//(typically this list will only contain 1 item as that is
		//the maximum size of a batch).
		for (;entryOperations.hasNext();)
			publisher.publish(entryOperations.next());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		this.publisher = (Publisher)ExternalizableHelper.readObject(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		ExternalizableHelper.writeObject(out, publisher);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
		this.publisher = (Publisher)reader.readObject(100);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
		writer.writeObject(100, publisher);
	}
}
