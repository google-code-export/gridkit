/*
 * File: UpdateContextProcessor.java
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

import com.oracle.coherence.common.ticketing.Ticket;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.Context;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * <p>The {@link UpdateContextProcessor} is used to update the state
 * of a {@link Context} within a {@link ContextWrapper} (if and only if
 * the specified version matches the {@link Context} version) after
 * a {@link Command} has been executed.</p>
 *  
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class UpdateContextProcessor extends AbstractProcessor implements ExternalizableLite, PortableObject {

	/**
	 * <p>The version of the {@link Context} we are expecting.</p>
	 */
	private long contextVersion;
	
	
	/**
	 * <p>The new {@link Context} value (null means don't update)</p>
	 */
	private Context context;
	
	
	/**
	 * <p>The {@link Ticket} just executed.</p>
	 */
	private Ticket lastExecutedTicket;
	
	
	/**
	 * <p>The time (in milliseconds) that the last executed {@link Command} took to execute.</p>
	 */
	private long executionDuration;
	
	
	/**
	 * <p>The time (in milliseconds) that the last executed {@link Command} was waiting to be executed.</p>
	 */
	private long waitingDuration;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite} and {@link PortableObject}
	 */
	public UpdateContextProcessor() {
	}
	
	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param contextVersion
	 * @param context (if null, the context value won't be updated).
	 * @param lastExecutedTicket
	 * @param executionDuration The time (in milliseconds) that the last executed {@link Ticket} took to execute
	 * @param waitingDuration The time (in milliseconds) that the last executed {@link Ticket} waited before being executed
	 */
	public UpdateContextProcessor(long contextVersion, 
								  Context context, 
								  Ticket lastExecutedTicket,
								  long executionDuration,
								  long waitingDuration) {
		this.contextVersion = contextVersion;
		this.context = context;
		this.lastExecutedTicket = lastExecutedTicket;
		this.executionDuration = executionDuration;
		this.waitingDuration = waitingDuration;
	}
	
	
	/**
	 * {@inheritDoc} 
	 */
	public Object process(Entry entry) {
		if (entry.isPresent()) {
			ContextWrapper contextWrapper = (ContextWrapper)entry.getValue();
			if (contextWrapper.getContextVersion() == contextVersion) {
				contextWrapper.updateExecutionInformation(lastExecutedTicket, executionDuration, waitingDuration);
				if (context != null) 
					contextWrapper.setContext(context);
				entry.setValue(contextWrapper);
				
				return true;
			} else {
				return false;
			}
			
		} else {
			return false;
		}
	}

	
	/**
	 * {@inheritDoc} 
	 */
	public void readExternal(DataInput in) throws IOException {
		this.contextVersion = ExternalizableHelper.readLong(in);
		this.context = (Context)ExternalizableHelper.readObject(in);
		this.lastExecutedTicket = (Ticket)ExternalizableHelper.readExternalizableLite(in);
		this.executionDuration = ExternalizableHelper.readLong(in);
		this.waitingDuration = ExternalizableHelper.readLong(in);
	}
	
	
	/**
	 * {@inheritDoc} 
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeLong(out, contextVersion);
		ExternalizableHelper.writeObject(out, context);
		ExternalizableHelper.writeExternalizableLite(out, lastExecutedTicket);
		ExternalizableHelper.writeLong(out, executionDuration);
		ExternalizableHelper.writeLong(out, waitingDuration);
	}

	
	/**
	 * {@inheritDoc} 
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.contextVersion = reader.readLong(0);
		this.context = (Context)reader.readObject(1);
		this.lastExecutedTicket = (Ticket)reader.readObject(2);
		this.executionDuration = reader.readLong(3);
		this.waitingDuration = reader.readLong(4);
	}

	
	/**
	 * {@inheritDoc} 
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeLong(0, contextVersion);
		writer.writeObject(1, context);
		writer.writeObject(2, lastExecutedTicket);
		writer.writeLong(3, executionDuration);
		writer.writeLong(4, waitingDuration);
	}
}
