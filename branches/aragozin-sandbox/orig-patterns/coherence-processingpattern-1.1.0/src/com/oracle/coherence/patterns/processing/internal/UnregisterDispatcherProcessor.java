/*
 * File: UnregisterDispatcherProcessor.java
 * 
 * Copyright (c) 2009. All Rights Reserved. Oracle Corporation.
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
package com.oracle.coherence.patterns.processing.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.patterns.processing.dispatchers.Dispatcher;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * <p>Unregister a {@link Dispatcher} from the system.</p>
 * 
 * @author Noah Arliss
 */
@SuppressWarnings("serial")
public class UnregisterDispatcherProcessor extends AbstractProcessor 
										 implements PortableObject, ExternalizableLite {
	
	/**
	 * <p>The {@link Dispatcher} to register.</p>
	 */
	private Dispatcher dispatcher;
	
	
	/**
	 * Construct a new {@link UnregisterDispatcherProcessor}. 
	 * 
	 * @param dispatcher  the dispatcher to register
	 */
	public UnregisterDispatcherProcessor(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Object process(Entry entry) {
		if (entry.isPresent()) {
			entry.remove(false);
		}
		return null;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.dispatcher = (Dispatcher) reader.readObject(0);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, this.dispatcher);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.dispatcher = (Dispatcher) ExternalizableHelper.readObject(in);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, this.dispatcher);
	}
}
