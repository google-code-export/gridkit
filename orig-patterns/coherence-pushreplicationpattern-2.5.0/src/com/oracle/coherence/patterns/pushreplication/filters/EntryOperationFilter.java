/*
 * File: EntryOperationFilter.java
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
package com.oracle.coherence.patterns.pushreplication.filters;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;

import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.patterns.pushreplication.EntryOperation;
import com.oracle.coherence.patterns.pushreplication.EntryOperation.Operation;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.Filter;
import com.tangosol.util.filter.EntryFilter;

/**
 * <p>An {@link EntryOperationFilter} may be used to evaluate the content of an
 * {@link EntryOperation} using a specified criteria.</p>
 * 
 * <p>{@link EntryOperationFilter}s are designed to simplify the use of {@link Filter}s
 * when attempting to selectively publish {@link EntryOperation}s.</p> 
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class EntryOperationFilter implements Filter, ExternalizableLite, PortableObject {

	
	/**
	 * <p>One of the following {@link Operation}s must occur on an {@link EntryOperation}
	 * to satisfy this {@link EntryOperationFilter}.</p> 
	 */
	private HashSet<EntryOperation.Operation> operations;
	
	
	/**
	 * <p>The {@link Filter} that (also) must be satisfied on the {@link EntryOperation}
	 * to satisfy this {@link EntryOperationFilter}.</p>
	 */
	private Filter filter;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public EntryOperationFilter() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param filter The {@link Filter} that must be satisfied by the {@link EntryOperation} 
	 * 				 passed to this {@link EntryOperationFilter}
	 * 
	 * @param operations One or more {@link Operation}s must have occurred to 
	 * 					 satisfy this {@link EntryOperationFilter}.
	 */
	public EntryOperationFilter(Filter filter, Operation... operations) {
		this.filter = filter;
		this.operations = new HashSet<Operation>();
		for (Operation operation : operations) 
			this.operations.add(operation);
	}
	
	
	/**
	 * <p>Returns the set of {@link Operation}s, one of which must be satisfied
	 * (in addition to the {@link Filter}) for the {@link EntryOperationFilter} 
	 * to be satisfied.</p>
	 */
	public HashSet<EntryOperation.Operation> getOperations() {
		return operations;
	}
	

	/**
	 * <p>Returns the {@link Filter} that must be satisified (in addition to one
	 * of the {@link Operation}s) for the {@link EntryOperationFilter} to be satisfied.</p>
	 */
	public Filter getFilter() {
		return filter;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public boolean evaluate(Object object) {
		//ensure that we're dealing with an EntryOperation object
		if (object instanceof EntryOperation) {
			EntryOperation entryOperation = (EntryOperation)object;
			
			if (getOperations().contains(entryOperation.getOperation())) {
				if (filter == null) {
					return true;
				} else {
					try {
						
						if (filter instanceof EntryFilter) 
							return ((EntryFilter) filter).evaluateEntry(entryOperation);
						else
							return filter.evaluate(entryOperation);
						
					} catch (Exception exception) {
						Logger.log(Logger.ERROR, "%s failed due to %s", this.getClass().getName(), exception);
						return false;
					}
				}	
				
			} else {
				//the filter was not satisfied
				return false;
			}
			
		} else {
			//non-EntryOperation objects will fail the test
			return false;
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.filter = (Filter)ExternalizableHelper.readObject(in);
		this.operations = new HashSet<Operation>();
		for(int i = ExternalizableHelper.readInt(in); i > 0; i--)
			this.operations.add(Operation.values()[ExternalizableHelper.readInt(in)]);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, filter);
		ExternalizableHelper.writeInt(out, operations.size());
		for(Operation operation : operations)
			ExternalizableHelper.writeInt(out, operation.ordinal());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.filter = (Filter)reader.readObject(0);
		this.operations = new HashSet<Operation>();
		int size = reader.readInt(1);
		if (size > 0) {
			int[] operationsAsOrdinals = reader.readIntArray(2);
			for (int i : operationsAsOrdinals) 
				operations.add(Operation.values()[i]);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, filter);
		writer.writeInt(1, operations.size());
		if (!operations.isEmpty()) {
			int[] operationsAsOrdinals = new int[operations.size()];
			int i = 0;
			for(Operation operation : operations)
				operationsAsOrdinals[i++] = operation.ordinal();
			writer.writeIntArray(2, operationsAsOrdinals);
		}
	}
}
