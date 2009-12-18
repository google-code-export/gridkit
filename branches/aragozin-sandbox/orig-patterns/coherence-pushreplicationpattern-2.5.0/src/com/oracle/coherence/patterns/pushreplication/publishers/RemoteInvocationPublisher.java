/*
 * File: RemoteInvocationPublisher.java
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.oracle.coherence.patterns.messaging.SubscriptionIdentifier;
import com.oracle.coherence.patterns.pushreplication.BatchPublisher;
import com.oracle.coherence.patterns.pushreplication.EntryOperation;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.AbstractInvocable;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link RemoteInvocationPublisher} is a {@link BatchPublisher} 
 * implementation that uses a Remote {@link InvocationService}  
 * in which to push an entire batch of {@link EntryOperation}s to a remote 
 * site, at which another {@link BatchPublisher} (called the remote 
 * batch publisher) will perform the actual publishing.</p>
 *  
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class RemoteInvocationPublisher extends AbstractBatchPublisher {
	
	/**
	 * <p>The name of the remote {@link InvocationService} that
	 * we'll use to send over batches of {@link EntryOperation}s
	 * to be published remotely.</p>
	 */
	private String remoteInvocationServiceName;
	
	
	/**
	 * <p>The {@link BatchPublisher} that will be used to 
	 * synchronously publish the batch of {@link EntryOperation}s
	 * at the remote site.</p> 
	 */
	private BatchPublisher remoteBatchPublisher;
	
	
	/**
	 * <p>The {@link InvocationService} that we'll use to 
	 * send batches of {@link EntryOperation}s for remote publishing.</p>
	 * 
	 * <p>NOTE: When this is <code>null</code> the service is not
	 * available (has failed).</p>
	 */
	private transient InvocationService remoteInvocationService;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public RemoteInvocationPublisher() {
	}

	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param remoteInvocationServiceName
	 * @param remoteBatchPublisher
	 * @param autostart
	 * @param batchPublishingDelay
	 * @param batchSize
	 * @param restartDelay
	 * @param totalConsecutiveFailuresBeforeSuspending
	 */
	public RemoteInvocationPublisher(String remoteInvocationServiceName,
									 BatchPublisher remoteBatchPublisher,
									 boolean autostart,
									 long batchPublishingDelay, 
									 int batchSize,
									 long restartDelay,
									 int totalConsecutiveFailuresBeforeSuspending) {
		super(autostart, batchPublishingDelay, batchSize, restartDelay, totalConsecutiveFailuresBeforeSuspending);
		this.remoteInvocationServiceName = remoteInvocationServiceName;
		this.remoteBatchPublisher = remoteBatchPublisher;
		this.remoteInvocationService = null;
	}


	/**
	 * {@inheritDoc}
	 */	
	public void start(SubscriptionIdentifier subscriptionIdentifier) {
		try {
			remoteInvocationService = (InvocationService)CacheFactory.getConfigurableCacheFactory().ensureService(remoteInvocationServiceName);
        	
			if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG))
				CacheFactory.log(String.format("Connected to Remote Invocation Service %s for Subscription %s", remoteInvocationServiceName, subscriptionIdentifier), CacheFactory.LOG_DEBUG);
	        
		} catch (RuntimeException runtimeException) {
			
			CacheFactory.log(String.format("Failed to connect to Remote Invocation Service %s for Subscription %s", remoteInvocationServiceName, subscriptionIdentifier), CacheFactory.LOG_WARN);        	
			remoteInvocationService = null;
			
			throw runtimeException;
		}
	}

	
	/**
	 * {@inheritDoc}
	 */	
	public void stop(SubscriptionIdentifier subscriptionIdentifier) {
		if (remoteInvocationService != null) {

			if (CacheFactory.isLogEnabled(CacheFactory.LOG_DEBUG))
				CacheFactory.log(String.format("Disconnected from Remote Invocation Service %s for Subscription %s", remoteInvocationServiceName, subscriptionIdentifier), CacheFactory.LOG_DEBUG);

			remoteInvocationService = null;
		}
	}

	
	/**
	 * {@inheritDoc}
	 */	
	public void publishBatch(SubscriptionIdentifier subscriptionHandle, 
							 Iterator<EntryOperation> entryOperations) {
		if (remoteInvocationService == null) {
			throw new IllegalStateException(String.format("Attempted to publish using disconnected Remote Invocation Service %s", remoteInvocationServiceName));
		} else {
			ArrayList<EntryOperation> lEntryOperations = new ArrayList<EntryOperation>(getBatchSize());
			for(;entryOperations.hasNext();)
				lEntryOperations.add(entryOperations.next());
			
			remoteInvocationService.query(
				new RemotePublishingAgent(subscriptionHandle, lEntryOperations, remoteBatchPublisher), 
				null
			);
		}
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		super.readExternal(in);
		this.remoteInvocationServiceName = ExternalizableHelper.readSafeUTF(in);
		this.remoteBatchPublisher = (BatchPublisher)ExternalizableHelper.readObject(in);
	}

		
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		super.writeExternal(out);
		ExternalizableHelper.writeSafeUTF(out, remoteInvocationServiceName);
		ExternalizableHelper.writeObject(out, remoteBatchPublisher);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		super.readExternal(reader);
		this.remoteInvocationServiceName = reader.readString(100);
		this.remoteBatchPublisher = (BatchPublisher)reader.readObject(101);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		super.writeExternal(writer);
		writer.writeString(100, remoteInvocationServiceName);
		writer.writeObject(101, remoteBatchPublisher);
	}
	
	
	/**
	 * <p>A {@link RemotePublishingAgent} is used to send an entire
	 * batch (as a ordered list) of {@link EntryOperation}s using an
	 * {@link InvocationService} to a remote site for publishing with
	 * the remote batch publisher.</p>
	 */
	public static class RemotePublishingAgent extends AbstractInvocable implements ExternalizableLite, PortableObject {

		private SubscriptionIdentifier subscriptionIdentifier;
		private List<EntryOperation> entryOperations;
		private BatchPublisher batchPublisher;
		
		
		/**
		 * <p>Required {@link ExternalizableLite}.</p>
		 */
		public RemotePublishingAgent() {
		}
		

		/**
		 * <p>Standard Constructor.</p>
		 * 
		 * @param subscriptionIdentifier
		 * @param entryOperations
		 * @param batchPublisher
		 */
		public RemotePublishingAgent(SubscriptionIdentifier subscriptionIdentifier,
									 List<EntryOperation> entryOperations,
									 BatchPublisher batchPublisher) {
			assert batchPublisher != null;
			this.subscriptionIdentifier = subscriptionIdentifier;
			this.entryOperations = entryOperations;
			this.batchPublisher = batchPublisher;
		}


		/**
		 * <p>REMEMBER: The following method is executed in the remote site!</p>
		 */
		public void run() {
			batchPublisher.start(subscriptionIdentifier);
			batchPublisher.publishBatch(subscriptionIdentifier, entryOperations.iterator());
			batchPublisher.stop(subscriptionIdentifier);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void readExternal(DataInput in) throws IOException {
			this.subscriptionIdentifier = (SubscriptionIdentifier)ExternalizableHelper.readObject(in);
			this.entryOperations = new LinkedList<EntryOperation>();
			ExternalizableHelper.readCollection(in, entryOperations, Thread.currentThread().getContextClassLoader());
			this.batchPublisher = (BatchPublisher)ExternalizableHelper.readObject(in);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(DataOutput out) throws IOException {
			ExternalizableHelper.writeObject(out, subscriptionIdentifier);
			ExternalizableHelper.writeCollection(out, entryOperations);
			ExternalizableHelper.writeObject(out, batchPublisher);
		}

		
		/**
		 * {@inheritDoc}
		 */
		public void readExternal(PofReader reader) throws IOException {
			this.subscriptionIdentifier = (SubscriptionIdentifier)reader.readObject(0);
			this.entryOperations = new LinkedList<EntryOperation>();
			reader.readCollection(1, entryOperations);
			this.batchPublisher = (BatchPublisher)reader.readObject(2);
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(PofWriter writer) throws IOException {
			writer.writeObject(0, subscriptionIdentifier);
			writer.writeCollection(1, entryOperations);
			writer.writeObject(2, batchPublisher);
		}
	}
}


