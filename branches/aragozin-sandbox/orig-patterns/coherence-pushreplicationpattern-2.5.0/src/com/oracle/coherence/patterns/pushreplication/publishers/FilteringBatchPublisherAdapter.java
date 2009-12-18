/*
 * File: FilteringBatchPublisherAdapter.java
 * 
 * Copyright (c) 2008-2009. All Rights Reserved. Oracle Corporation.
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

import com.oracle.coherence.common.util.FilteringIterator;
import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.oracle.coherence.patterns.pushreplication.BatchPublisher;
import com.oracle.coherence.patterns.pushreplication.EntryOperation;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.Filter;

/**
 * <p>A {@link FilteringBatchPublisherAdapter} allows a {@link Filter} to be applied to
 * each {@link EntryOperation} before it is published by a {@link BatchPublisher}.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public final class FilteringBatchPublisherAdapter implements BatchPublisher, 
															 ExternalizableLite,
															 PortableObject {

	/**
	 * <p>The {@link BatchPublisher} that will have it's {@link EntryOperation}s 
	 * "filtered" using a provided {@link Filter}.</p>
	 */
	private BatchPublisher batchPublisher;
	
	
	/**
	 * <p>The {@link Filter} that will be applied to each {@link EntryOperation}
	 * before it is published with the adapted {@link BatchPublisher}.</p>
	 */
	private Filter filter;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public FilteringBatchPublisherAdapter() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param filter
	 * @param batchPublisher
	 */
	public FilteringBatchPublisherAdapter(Filter filter,
										  BatchPublisher batchPublisher) {
		this.filter = filter;
		this.batchPublisher = batchPublisher;
	}

	
	/**
	 * <p>Returns the {@link Filter} that will be applied to each {@link EntryOperation}
	 * prior to it being published using the {@link BatchPublisher}.</p>
	 */
	public Filter getFilter() {
		return filter;
	}

	
	/**
	 * <p>Returns the {@link BatchPublisher} that will be used to publish
	 * batches of filtered {@link EntryOperation}s.</p>
	 */
	public BatchPublisher getBatchPublisher() {
		return batchPublisher;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public boolean autostart() {
		return batchPublisher.autostart();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public long getBatchPublishingDelay() {
		return batchPublisher.getBatchPublishingDelay();
	}
	

	/**
	 * {@inheritDoc}
	 */
	public int getBatchSize() {
		return batchPublisher.getBatchSize();
	}

	
	/**
	 * {@inheritDoc}
	 */
	public long getRestartDelay() {
		return batchPublisher.getRestartDelay();
	}
	

	/**
	 * {@inheritDoc}
	 */
	public int getTotalConsecutiveFailuresBeforeSuspending() {
		return batchPublisher.getTotalConsecutiveFailuresBeforeSuspending();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void publishBatch(SubscriptionIdentifier subscriptionIdentifier,
							 Iterator<EntryOperation> entryOperations) {

		batchPublisher.publishBatch(subscriptionIdentifier, 
									new FilteringIterator<EntryOperation>(filter, entryOperations));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void start(SubscriptionIdentifier subscriptionIdentifier) {
		batchPublisher.start(subscriptionIdentifier);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void stop(SubscriptionIdentifier subscriptionIdentifier) {
		batchPublisher.stop(subscriptionIdentifier);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.filter = (Filter)ExternalizableHelper.readObject(in);
		this.batchPublisher = (BatchPublisher)ExternalizableHelper.readObject(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, filter);
		ExternalizableHelper.writeObject(out, batchPublisher);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.filter = (Filter)reader.readObject(1);
		this.batchPublisher = (BatchPublisher)reader.readObject(2);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(1, filter);
		writer.writeObject(2, batchPublisher);
	}
	
}
