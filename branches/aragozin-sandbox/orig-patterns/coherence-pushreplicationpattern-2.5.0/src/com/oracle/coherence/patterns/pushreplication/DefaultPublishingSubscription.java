/*
 * File: DefaultPublishingSubscription.java
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
package com.oracle.coherence.patterns.pushreplication;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link DefaultPublishingSubscription} is a simple parameterized 
 * implementation of a {@link PublishingSubscription}.</p>
 * 
 * <p>NOTE: The {@link BatchPublisher} in this case must be {@link Serializable}.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class DefaultPublishingSubscription extends PublishingSubscription {

	/**
	 * <p>The {@link BatchPublisher} to use for the {@link PublishingSubscription}.</p>
	 */
	private BatchPublisher batchPublisher;

	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public DefaultPublishingSubscription() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param cacheName The name of the cache to which we're subscribing for updates.  
	 * @param subscriberName The name of the subscriber for this {@link PublishingSubscription}
	 * @param batchPublisher A {@link BatchPublisher} to publish {@link EntryOperation}s.
	 */
	public DefaultPublishingSubscription(String cacheName, 
								  		 String subscriberName,
								  		 BatchPublisher batchPublisher) {
		super(cacheName, subscriberName);

		assert batchPublisher != null;
		this.batchPublisher = batchPublisher;
	}


	/**
	 * {@inheritDoc}
	 */
	public BatchPublisher createBatchPublisher() {
		return batchPublisher;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		this.batchPublisher = (BatchPublisher)ExternalizableHelper.readObject(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		ExternalizableHelper.writeObject(out, batchPublisher);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
		this.batchPublisher = (BatchPublisher)reader.readObject(300);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
		writer.writeObject(300, batchPublisher);
	}
}
