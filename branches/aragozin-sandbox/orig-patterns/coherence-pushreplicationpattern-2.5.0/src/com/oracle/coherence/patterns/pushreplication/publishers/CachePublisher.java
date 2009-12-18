/*
 * File: CachePublisher.java
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

import com.oracle.coherence.patterns.pushreplication.EntryOperation;
import com.oracle.coherence.patterns.pushreplication.Publisher;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.InvocableMap.Entry;

/**
 * <p>A {@link CachePublisher} is a specialized {@link Publisher} that is 
 * explicitly designed to publish and perform {@link EntryOperation}s on a 
 * {@link NamedCache}, and during the process, through the use of a
 * custom {@link ConflictResolver}, resolve any potential conflicts 
 * with underlying or existing entries in the said {@link NamedCache}.</p>
 *
 * @author Brian Oliver
 */
public interface CachePublisher extends Publisher {

	/**
	 * <p>Returns the name of the cache on which we'll be performing
	 * {@link EntryOperation}s.</p>
	 */
	public String getCacheName();

	
	/**
	 * <p>Returns the {@link NamedCache} on which we'll be performing
	 * {@link EntryOperation}s.</p>
	 * 
	 * <p>NOTE:  This method may return <code>null</code> or a
	 * {@link NamedCache} that is not active (ie: not {@link NamedCache#isActive()})
	 * in which case the {@link Publisher} is considered offline and can't
	 * be used for publishing.</p>
	 */
	public NamedCache getNamedCache();
	
	
	/**
	 * <p>Returns the {@link ConflictResolver} that will be used to 
	 * resolve and perform {@link EntryOperation}s on the {@link Entry}s
	 * in the {@link NamedCache} provided by {@link #getCacheName()}.</p>
	 */
	public ConflictResolver getConflictResolver();
	
	
	/**
	 * <p>An {@link ConflictResolver} is responsible for performing 
	 * an actual {@link EntryOperation} on a specified cache {@link Entry}.</p>
	 * 
	 * <p>By providing a custom {@link ConflictResolver} implementation, 
	 * it is possible to detect and perform all kinds of programmatic conflict 
	 * resolution during the process of publishing an {@link EntryOperation}.</p>
	 * 
	 * <p>NOTE: As {@link ConflictResolver}s are often used for performing 
	 * {@link EntryOperation}s on remote caches, {@link ConflictResolver}s 
	 * typically need to be serializable.  This may be achieved by either 
	 * supporting standard Java Serialization or better still, implementing
	 * {@link ExternalizableLite} or {@link PortableObject}.</p>
	 */
	public interface ConflictResolver {
		
		/**
		 * <p>Perform {@link EntryOperation} on the provided {@link Entry}.</p>
		 * 
		 * <p>NOTE 1: The implementor of this interface is responsible for the outcome
		 * of the execution of the {@link EntryOperation} on the {@link Entry}.
		 * Through manipulating the provided {@link Entry}, implementations may ignore the
		 * {@link EntryOperation} (by not updating the {@link Entry}), update the existing 
		 * {@link Entry} (through calling {@link Entry#setValue(Object)}), perform some
		 * merge/custom conflict resolution (and the call {@link Entry#setValue(Object)}) 
		 * or remove the {@link Entry} (using {@link Entry#remove(boolean)}.</p>
		 * 
		 * <p>NOTE 2: Any exception thrown by this method will cause the publishing
		 * for this {@link CachePublisher} to stop. ie: fail-fast.  You must be careful
		 * to ensure that internal exceptions are dealt with carefully and only escape 
		 * this method if you <strong>want</strong> the associated {@link CachePublisher}
		 * to be suspended.</p>
		 * 
		 * @param entryOperation
		 * @param entry
		 */
		public void perform(EntryOperation entryOperation, InvocableMap.Entry entry);
	}
	
	
	/**
	 * <p>The {@link BruteForceConflictResolver} is an implementation of
	 * a {@link ConflictResolver} that simply performs a provided {@link EntryOperation}, 
	 * (ie: brute force) and completely ignores the current state of any underlying {@link Entry}.</p>
	 */
	@SuppressWarnings("serial")
	public static class BruteForceConflictResolver implements ConflictResolver, ExternalizableLite, PortableObject {
		
		/**
		 * <p>Standard Constructor. 
		 * (also required for {@link ExternalizableLite} and {@link PortableObject} support).</p>
		 */
		public BruteForceConflictResolver() {
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void perform(EntryOperation entryOperation, Entry entry) {
			switch (entryOperation.getOperation()) {
			case Delete:
			case Erase:
				entry.remove(false);
				break;
				
			case Insert:
			case Store:
			case Update:
				entry.setValue(entryOperation.getValue());
				break;
			}
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void readExternal(DataInput in) throws IOException {
			//there are no attributes - so there is nothing to do here
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void readExternal(PofReader reader) throws IOException {
			//there are no attributes - so there is nothing to do here
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(DataOutput out) throws IOException {
			//there are no attributes - so there is nothing to do here
		}
		
		
		/**
		 * {@inheritDoc}
		 */
		public void writeExternal(PofWriter writer) throws IOException {
			//there are no attributes - so there is nothing to do here
		}
	}
}
