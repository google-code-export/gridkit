/*
 * File: AddOwnerProcessor.java
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

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.UUID;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * <p>Add an owner to a {@link Submission}.</p>
 *  
 * @author Noah Arliss
 */
@SuppressWarnings("serial")
public class AddOwnerProcessor extends AbstractProcessor implements
		ExternalizableLite, PortableObject {
	
	/**
	 * <p>The {@link UUID} of the owner to add to a {@link Submission}.</p>
	 */
	private Object ownerId;
	
	
	/**
	 * <p>Construct a new {@link AddOwnerProcessor}.</p>
	 * 
	 * @param ownerId  The ownerId to add to a {@link Submission}.</p>
	 */
	public AddOwnerProcessor(Object ownerId) {
		this.ownerId = ownerId;
	}
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}.</p>
	 */
	public AddOwnerProcessor() {
		
	}
	
	
	/**
	 * <p>Return the ownerId to add to a {@link Submission}.</p>
	 * 
	 * @return  the ownerId to add to a {@link Submission}.</p>
	 */
	public Object getOwnerId() {
		return ownerId;
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.ownerId = (UUID) ExternalizableHelper.readObject(in);
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, this.ownerId);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.ownerId = (UUID) reader.readObject(0);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, this.ownerId);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Object process(Entry entry) {
		Submission submission = (Submission) entry.getValue();
		submission.addOwner(ownerId);
		entry.setValue(submission);
		
		return submission;
	}

}
