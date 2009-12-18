/*
 * File: CoalescingBatchPublisherAdapter.java
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
import java.util.LinkedHashMap;

import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.oracle.coherence.patterns.pushreplication.BatchPublisher;
import com.oracle.coherence.patterns.pushreplication.EntryOperation;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link CoalescingBatchPublisherAdapter} will attempt to
 * coalesce multiple {@link EntryOperation}s for individual cache 
 * entries into a reduced set of {@link EntryOperation}s in order 
 * to reduce the size of batches being replicated.</p>
 * 
 * <p>For example; the sequence of updates for 
 * a cache entry such as [(store,a,1), (store,a,2), (store,a,3)]
 * will be coalesced into the single operation sequence [(store,a,3)].</p>
 * 
 * <p>NOTE 1: It only makes sense to use this adapter when intermediate
 * entry operations and ordering of entry operations with in a 
 * batch are irrelevant.</p>
 * 
 * <p>NOTE 2: Operations such as deletes/removes are also coalesced.</p>
 * 
 * <p>For example; the sequence [(store,a,1), (remove,a), (store,a,2)]
 * will be coalesced into the single operation sequence [(store,a,3)].</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class CoalescingBatchPublisherAdapter implements PortableObject, 
														ExternalizableLite,
														BatchPublisher {

	/**
	 * <p>The {@link BatchPublisher} to which the coalesced {@link EntryOperation}s
	 * will be published.</p>
	 */
	private BatchPublisher batchPublisher;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */	
	public CoalescingBatchPublisherAdapter() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param batchPublisher
	 */
	public CoalescingBatchPublisherAdapter(BatchPublisher batchPublisher) {
		this.batchPublisher = batchPublisher;
	}
	

	/**
	 * <p>Returns the {@link BatchPublisher} to which the coalesced {@link EntryOperation}s
	 * will be published..</p>
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

		//an ordered hash map in which to coalesce entry operations
		LinkedHashMap<Object, EntryOperation> coalescedEntryOperations = new LinkedHashMap<Object, EntryOperation>();
		
		//coalesce the entry operations
		for(;entryOperations.hasNext();) {
			EntryOperation entryOperation = entryOperations.next();
			coalescedEntryOperations.put(entryOperation.getKey(), entryOperation);
		}
		
		//publish the entry operations
		batchPublisher.publishBatch(
			subscriptionIdentifier,
			coalescedEntryOperations.values().iterator()
		);		
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
		this.batchPublisher = (BatchPublisher)ExternalizableHelper.readObject(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, batchPublisher);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.batchPublisher = (BatchPublisher)reader.readObject(1);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(1, batchPublisher);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return String.format("CoalescingBatchPublisher{%s}", batchPublisher);
	}	
}
