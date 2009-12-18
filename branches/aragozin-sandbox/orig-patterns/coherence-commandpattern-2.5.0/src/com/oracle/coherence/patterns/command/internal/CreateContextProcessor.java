/*
 * File: CreateContextProcessor.java
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
package com.oracle.coherence.patterns.command.internal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ContextConfiguration;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.InvocableMap.EntryProcessor;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * <p>An {@link EntryProcessor} that will place an {@link Context}
 * into the {@link ContextWrapper#CACHENAME} Coherence Cache,
 * if it does not already exist.</p>
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class CreateContextProcessor extends AbstractProcessor implements ExternalizableLite, PortableObject {
	
	
	/**
	 * <p>The {@link Context} to be created.</p>
	 */
	private Context context;

	
	/**
	 * <p>The {@link ContextConfiguration} for the {@link Context}.</p>
	 */
	private ContextConfiguration contextConfiguration;
	

	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public CreateContextProcessor() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param context
	 */
	public CreateContextProcessor(Context context, 
								  ContextConfiguration contextConfiguration) {
		this.context = context;
		this.contextConfiguration = contextConfiguration;
	}


	/**
	 * {@inheritDoc}
	 */
	public Object process(Entry entry) {
		if (!entry.isPresent()) {
			
			Identifier contextIdentifier = (Identifier)entry.getKey();
			ContextWrapper contextWrapper = new ContextWrapper(contextIdentifier, context, contextConfiguration);			
			entry.setValue(contextWrapper);
		}
		
		return entry.getValue();
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.context = (Context)ExternalizableHelper.readObject(in);
		this.contextConfiguration = (ContextConfiguration)ExternalizableHelper.readObject(in);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, context);
		ExternalizableHelper.writeObject(out, contextConfiguration);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.context = (Context)reader.readObject(0);
		this.contextConfiguration = (ContextConfiguration)reader.readObject(1);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, context);
		writer.writeObject(1, contextConfiguration);
	}
}
