package com.oracle.coherence.patterns.pushreplication;
/*
 * File: EntryOperationBatch.java
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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.oracle.coherence.patterns.pushreplication.EntryOperation.Operation;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>An {@link EntryOperationBatch} is a batch of {@link EntryOperation}s.
 * It is used when the ordering for push replication is not deterministic.
 * 
 * <p>Copyright (c) 2009. All Rights Reserved. Oracle Corporation.</p>
 *
 * @author Noah Arliss
 */
@SuppressWarnings("serial")
public class EntryOperationBatch implements ExternalizableLite, PortableObject {
	
	/**
	 * <p>The name of the site in which this {@link EntryOperation} 
	 * originally occurred.</p>
	 */
	private String siteName;
	
	
	/**
	 * <p>The name of the cluster in which this {@link EntryOperation}
	 * originally occurred.</p>
	 */
	private String clusterName;
	
	
	/**
	 * <p>The name of the cache on which the {@link Operation} occurred.</p>
	 */
	private String cacheName;
	
	
	/**
	 * <p>The batch of entry operations.</p>
	 */
	private List<EntryOp> entryBatch;
	
	
	/**
	 * <p>Default constructor required for ExternalizableLite and PortableObject</p>
	 */
	public EntryOperationBatch() {
	}
	
	/**
	 * <p>Construct a new EntryOperation</p>
	 * 
	 * @param siteName     the site name
	 * @param clusterName  the cluster name
	 * @param cacheName    the cache name
	 */
	public EntryOperationBatch(String siteName, String clusterName, String cacheName) {
		this.siteName    = siteName;
		this.clusterName = clusterName;
		this.cacheName   = cacheName;
		
		entryBatch = new LinkedList<EntryOp>();
	}
	
	/**
	 * <p>Return the batch of entry operations.</p>
	 * 
	 * @return  the batch of entry operations
	 */
	public List<EntryOp> getEntryBatch() {
		return entryBatch;
	}
	
	/**
	 * <p>Return the siteName for this {@link EntryOperationBatch}.</p>
	 * 
	 * @return the siteName for this {@link EntryOperationBatch}.
	 */
	public String getSiteName() {
		return siteName;
	}
	
	/**
	 * <p>Return the clusterName for this {@link EntryOperationBatch}.</p>
	 * 
	 * @return the clusterName for this {@link EntryOperationBatch}.
	 */
	public String getClusterName() {
		return clusterName;
	}
	
	/**
	 * <p>Return the cacheName for this {@link EntryOperationBatch}.</p>
	 * 
	 * @return the cacheName for this {@link EntryOperationBatch}.
	 */
	public String getCacheName() {
		return cacheName;
	}
	
	
	/**
	 * <p>Add an entry operation to this batch.</p>
	 * 
	 * @param operation  the operation to add to this batch
	 */
	public void addEntryOperation(EntryOp operation) {
		entryBatch.add(operation);
	}

	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		siteName    = ExternalizableHelper.readSafeUTF(in);
		clusterName = ExternalizableHelper.readSafeUTF(in);
		cacheName   = ExternalizableHelper.readSafeUTF(in);
		
		entryBatch  = new LinkedList<EntryOp>();
		ExternalizableHelper.readCollection(in, entryBatch, 
				Thread.currentThread().getContextClassLoader());
	}

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeSafeUTF(out, siteName);
		ExternalizableHelper.writeSafeUTF(out, clusterName);
		ExternalizableHelper.writeSafeUTF(out, cacheName);
		ExternalizableHelper.writeCollection(out, entryBatch);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(PofReader reader) throws IOException {
		siteName    = reader.readString(0);
		clusterName = reader.readString(1);
		cacheName   = reader.readString(2);
		
		entryBatch = new LinkedList<EntryOp>();
		entryBatch = (List<EntryOp>) reader.readCollection(3, entryBatch);
	}

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeString(0, siteName);
		writer.writeString(1, clusterName);
		writer.writeString(2, cacheName);
		writer.writeCollection(3, entryBatch);
	}
	
	/**
	 * The set of unique data associated with each EntryOperation
	 * 
	 * @author Noah Arliss
	 */
	public static class EntryOp implements ExternalizableLite, PortableObject {

		/**
		 * <p>The {@link Operation} that occurred on the entry.</p>
		 */
		private Operation operation;
		
		
		/**
		 * <p>The key of the entry on which the {@link Operation} occurred.</p>
		 */
		private Object key;
		
		
		/**
		 * <p>The value of the entry on which the {@link Operation} occurred.</p>
		 */
		private Object value;
		
		
		/**
		 * <p>Return the operation for this {@link EntryOp}.</p>
		 * 
		 * @return the operation for this {@link EntryOp}
		 */
		public Operation getOperation() {
			return operation;
		}
		
		/**
		 * <p>Return the key for this {@link EntryOp}.</p>
		 * 
		 * @return the key for this {@link EntryOp}
		 */
		public Object getKey() {
			return key;
		}
		
		/**
		 * <p>Return the value for this {@link EntryOp}.</p>
		 * 
		 * @return the value for this {@link EntryOp}
		 */
		public Object getValue() {
			return value;
		}
		
		
		/**
		 * <p>Default constructor required for ExternalizableLite and PortableObject</p>
		 */
		public EntryOp() {
		}
		
		/**
		 * <p>Construct a new EntryOperation</p>
		 * 
		 * @param operation  the operation performed
		 * @param key        the key
		 * @param value      the value
		 */
		public EntryOp(Operation operation, Object key, Object value) {
			this.operation = operation;
			this.key       = key;
			this.value     = value;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public void readExternal(PofReader reader) throws IOException {
			operation = Operation.values()[reader.readInt(0)];
			key       = reader.readObject(1);
			value     = reader.readObject(2);
		}

		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(PofWriter writer) throws IOException {
			writer.writeInt(0, operation.ordinal());
			writer.writeObject(1, key);
			writer.writeObject(2, value);
		}

		/**
		 * {@inheritDoc}
		 */
		public void readExternal(DataInput in) throws IOException {
			operation = Operation.values()[ExternalizableHelper.readInt(in)];
			key       = ExternalizableHelper.readObject(in);
			value     = ExternalizableHelper.readObject(in);
		}

		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(DataOutput out) throws IOException {
			ExternalizableHelper.writeInt(out, operation.ordinal());
			ExternalizableHelper.writeObject(out, key);
			ExternalizableHelper.writeObject(out, value);
		}
		
	}
}
