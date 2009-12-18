/*
 * File: EntryOperation.java
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
import java.util.Map;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheStore;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.MapEvent;

/**
 * <p>An {@link EntryOperation} is an immutable object that 
 * captures some {@link Operation} which occurred on a Coherence 
 * {@link NamedCache} entry so that the said {@link Operation} 
 * and entry may be published to some device/location 
 * using a {@link Publisher}.</p>
 * 
 * <p>Copyright (c) 2008. All Rights Reserved. Oracle Corporation.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings({ "serial", "unchecked" })
public class EntryOperation implements Map.Entry,
									   ExternalizableLite, 
									   PortableObject {

	/**
	 * <p>The possible operations that may occur on a cache entry.</p>
	 */
	public enum Operation {
		/**
		 * <code>Insert</code> indicates that a {@link MapEvent#ENTRY_INSERTED}
		 * operation occurred.</p>
		 */
		Insert,

		
		/**
		 * <code>Update</code> indicates that a {@link MapEvent#ENTRY_UPDATED}
		 * operation occurred.</p>
		 */
		Update,

		
		/**
		 * <code>Delete</code> indicates that a {@link MapEvent#ENTRY_DELETED}
		 * operation occurred.</p>
		 */
		Delete,
		
		
		/**
		 * <code>Store</code> indicates that a {@link CacheStore#store(Object, Object)} 
		 * operation occurred.</p>
		 */
		Store,

		
		/**
		 * <code>Erase</code> indicates that a {@link CacheStore#erase(Object)} 
		 * operation occurred.</p>
		 */
		Erase
	}

	
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
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public EntryOperation() {
	}
	

	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param siteName
	 * @param clusterName
	 * @param cacheName
	 * @param operation
	 * @param key
	 * @param value
	 */
	public EntryOperation(String siteName,
						  String clusterName,
						  String cacheName,
						  Operation operation, 
						  Object key, 
						  Object value) {
		this.siteName = siteName;
		this.clusterName = clusterName;
		this.cacheName = cacheName;
		this.operation = operation;
		this.key = key;
		this.value = value;
	}
	
	
	/**
	 * <p>Returns the name of the site in which the {@link EntryOperation}
	 * originally occurred.</p>
	 */
	public String getSiteName() {
		return siteName;
	}
	
	
	/**
	 * <p>Returns the name of the cluster in which the {@link EntryOperation}
	 * originally occurred.</p>
	 */
	public String getClusterName() {
		return clusterName;
	}
	
	
	/**
	 * <p>Returns the name of the cache on which the {@link Operation} occurred.</p>
	 */
	public String getCacheName() {
		return cacheName;
	}
	

	/**
	 * <p>Returns the {@link Operation} that occurred.</p>
	 */
	public Operation getOperation() {
		return operation;
	}


	/**
	 * <p>Returns the key of the entry on which the {@link Operation} occurred.</p>
	 */
	public Object getKey() {
		return key;
	}
	
	
	/**
	 * <p>Returns the value of the entry on which the {@link Operation} occurred.</p>
	 */
	public Object getValue() {
		return value;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Object setValue(Object value) {
		throw new UnsupportedOperationException("Can't setValue(...) on an EntryOperation as they are immutable");
	}

	
	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return getKey().hashCode();
	}

	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EntryOperation))
			return false;
		EntryOperation other = (EntryOperation) obj;
		if (cacheName == null) {
			if (other.cacheName != null)
				return false;
		} else if (!cacheName.equals(other.cacheName))
			return false;
		if (clusterName == null) {
			if (other.clusterName != null)
				return false;
		} else if (!clusterName.equals(other.clusterName))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		if (siteName == null) {
			if (other.siteName != null)
				return false;
		} else if (!siteName.equals(other.siteName))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}


	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.siteName = ExternalizableHelper.readSafeUTF(in);
		this.clusterName = ExternalizableHelper.readSafeUTF(in);
		this.cacheName = ExternalizableHelper.readSafeUTF(in);
		this.operation = Operation.valueOf(ExternalizableHelper.readSafeUTF(in));
		this.key = ExternalizableHelper.readObject(in);
		this.value = ExternalizableHelper.readObject(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeSafeUTF(out, siteName == null ? "" : siteName);
		ExternalizableHelper.writeSafeUTF(out, clusterName == null ? "" : clusterName);
		ExternalizableHelper.writeSafeUTF(out, cacheName);
		ExternalizableHelper.writeSafeUTF(out, operation.name());
		ExternalizableHelper.writeObject(out, key);
		ExternalizableHelper.writeObject(out, value);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.siteName = reader.readString(0);
		this.clusterName = reader.readString(1);
		this.cacheName = reader.readString(2);
		this.operation = Operation.values()[reader.readInt(3)];
		this.key = reader.readObject(4);
		this.value = reader.readObject(5);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeString(0, siteName == null ? "" : siteName);
		writer.writeString(1, clusterName == null ? "" : clusterName);
		writer.writeString(2, cacheName);
		writer.writeInt(3, operation.ordinal());
		writer.writeObject(4, key);
		writer.writeObject(5, value);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("EntryOperation{siteName=%s, clusterName=%s, cacheName=%s, operation=%s, key=%s, value=%s}", siteName, clusterName, cacheName, operation, key, value);
	}
}
