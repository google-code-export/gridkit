/*
 * File: ContextWrapper.java
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
import com.oracle.coherence.common.ticketing.Ticket;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ContextConfiguration;
import com.oracle.coherence.patterns.command.DefaultContextsManager;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

/**
 * <p>A {@link ContextWrapper} is used to wrap and provide internal 
 * execution information about an individual {@link Context}, including
 * how to identify the {@link Context}, the cluster member on which
 * the {@link Context} was originally created and the {@link Ticket} of
 * the last successfully executed {@link Command} (so we can know where
 * to recover from in case of fail-over, load-balancing or restart.</p>
 * 
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class ContextWrapper implements ExternalizableLite, PortableObject {

	/**
	 * <p>The Coherence cache in which {@link ContextWrapper}s will be placed.</p>
	 */
	public static final String CACHENAME = "coherence.commandpattern.contexts";
	
	
	/**
	 * <p>The {@link Identifier} of the {@link Context}.</p>
	 */
	private Identifier contextIdentifier;
	
	
	/**
	 * <p>The {@link Context} that is being wrapped.</p>
	 */
	private Context context;
	
	
	/**
	 * <p>The {@link ContextConfiguration} for the {@link Context} being wrapped.</p>
	 */
	private ContextConfiguration contextConfiguration;
	
	
	/**
	 * <p>The version of a {@link ContextWrapper} is used to isolate and manage
	 * the Coherence Cluster Member that "owns" a {@link Context}.  The principle
	 * is to provide "versioning" of a {@link Context} when it moves between Members
	 * (due to scale out or partition load-balancing).</p>
	 */
	private long contextVersion;
		
	
	/**
	 * <p>The {@link Ticket} of the last successfully executed {@link Command}.</p>
	 */
	private Ticket lastExecutedTicket;


	/**
	 * <p>The total number of {@link Command}s that have been executed.</p>
	 */
	private long totalCommandsExecuted;
	
	
	/**
	 * <p>The total amount of time (in milliseconds) that {@link Command}s 
	 * have taken to execute.  This is the sum of
	 * individual {@link Command} execution times.</p>
	 */
	private long totalCommandExecutionDuration;
	
	
	/**
	 * <p>The total amount of time (in milliseconds) that {@link Command}s 
	 * have waited before they have been executed.  This is the sum of
	 * individual {@link Command} waiting times (in the queue).</p>
	 */
	private long totalCommandExecutionWaitingDuration;
	
	
	/**
	 * <p>Required for {@link ExternalizableLite}.</p>
	 */
	public ContextWrapper() {
	}
	

	/**
	 * <p>Standard Constructor.  Used by the {@link DefaultContextsManager}
	 * class when creating {@link Context}s.</p>
	 * 
	 * @param contextIdentifier
	 * @param context
	 */
	public ContextWrapper(Identifier contextIdentifier, 
						  Context context,
						  ContextConfiguration contextConfiguration) {
		this.contextIdentifier = contextIdentifier;
		this.context = context;
		this.contextConfiguration = contextConfiguration;
		this.contextVersion = 0;
		this.lastExecutedTicket = Ticket.NONE;
		this.totalCommandsExecuted = 0;
		this.totalCommandExecutionDuration = 0;
		this.totalCommandExecutionWaitingDuration = 0;
	}


	/**
	 * <p>Returns the unique {@link Identifier} that is used to represent the {@link Context}.</p>
	 */
	public Identifier getContentIdentifier() {
		return contextIdentifier;
	}


	/**
	 * <p>Return the {@link Context} being wrapped (ie: managed).</p>
	 */
	public Context getContext() {
		return context;
	}
	

	/**
	 * <p>Replace the {@link Context} being wrapped (ie: managed).</p>
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	
	/**
	 * <p>Returns the {@link ContextConfiguration} for the {@link Context} being wrapped.</p>
	 */
	public ContextConfiguration getContextConfiguration() {
		return contextConfiguration;
	}


	/**
	 * <p>Return the current version number of the {@link ContextWrapper}.</p>
	 */
	public long getContextVersion() {
		return contextVersion;
	}

	
	/**
	 * <p>Increments, stores and returns the next version of the {@link Context}.</p>
	 */
	public long nextVersion() {
		return ++contextVersion;
	}
	
	
	/**
	 * <p>Returns the {@link Ticket} of the last successfully executed {@link Command}
	 * for the {@link Context}.</p>
	 */
	public Ticket getLastExecutedTicket() {
		return lastExecutedTicket;
	}


	/**
	 * <p>Returns the total number of {@link Command}s that have been executed
	 * for the {@link Context}.</p>
	 */
	public long getTotalCommandsExecuted() {
		return totalCommandsExecuted;
	}
	
	
	/**
	 * <p>Returns the total time (in milliseconds) that the 
	 * {@link Command}s executed thus far have taken to execute.</p>
	 */
	public long getTotalCommandExecutionDuration() {
		return totalCommandExecutionDuration;
	}
	
	
	/**
	 * <p>Returns the total time (in milliseconds) that the 
	 * {@link Command}s executed thus far have waited to execute (ie: the queuing time).</p>
	 */
	public long getTotalCommandExecutionWaitingDuration() {
		return totalCommandExecutionWaitingDuration;
	}

	
	/**
	 * <p>Updates the execution information for the {@link Context} 
	 * tracked by the {@link ContextWrapper} after a {@link Command}
	 * has been executed.</p>
	 * 
	 * @param ticket
	 * @param executionDuration
	 * @param waitingDuration
	 */
	public void updateExecutionInformation(Ticket ticket, 
										   long executionDuration,
										   long waitingDuration) {
		this.lastExecutedTicket = ticket;
		this.totalCommandsExecuted++;
		this.totalCommandExecutionDuration += executionDuration;
		this.totalCommandExecutionWaitingDuration += waitingDuration;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(DataInput in) throws IOException {
		this.contextIdentifier = (Identifier)ExternalizableHelper.readObject(in);
		this.context = (Context)ExternalizableHelper.readObject(in);
		this.contextConfiguration = (ContextConfiguration)ExternalizableHelper.readObject(in);
		this.contextVersion = ExternalizableHelper.readLong(in);
		this.lastExecutedTicket = (Ticket)ExternalizableHelper.readExternalizableLite(in);
		this.totalCommandsExecuted = ExternalizableHelper.readLong(in);
		this.totalCommandExecutionDuration = ExternalizableHelper.readLong(in);
		this.totalCommandExecutionWaitingDuration = ExternalizableHelper.readLong(in);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(DataOutput out) throws IOException {
		ExternalizableHelper.writeObject(out, contextIdentifier);
		ExternalizableHelper.writeObject(out, context);
		ExternalizableHelper.writeObject(out, contextConfiguration);
		ExternalizableHelper.writeLong(out, contextVersion);
		ExternalizableHelper.writeExternalizableLite(out, lastExecutedTicket);
		ExternalizableHelper.writeLong(out, totalCommandsExecuted);
		ExternalizableHelper.writeLong(out, totalCommandExecutionDuration);
		ExternalizableHelper.writeLong(out, totalCommandExecutionWaitingDuration);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void readExternal(PofReader reader) throws IOException {
		this.contextIdentifier = (Identifier)reader.readObject(0);
		this.context = (Context)reader.readObject(1);
		this.contextConfiguration = (ContextConfiguration)reader.readObject(2);
		this.contextVersion = reader.readLong(3);
		this.lastExecutedTicket = (Ticket)reader.readObject(4);
		this.totalCommandsExecuted = reader.readLong(5);
		this.totalCommandExecutionDuration = reader.readLong(6);
		this.totalCommandExecutionWaitingDuration = reader.readLong(7);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeObject(0, contextIdentifier);
		writer.writeObject(1, context);
		writer.writeObject(2, contextConfiguration);
		writer.writeLong(3, contextVersion);
		writer.writeObject(4, lastExecutedTicket);
		writer.writeLong(5, totalCommandsExecuted);
		writer.writeLong(6, totalCommandExecutionDuration);
		writer.writeLong(7, totalCommandExecutionWaitingDuration);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return String.format("ContextWrapper{contextIdentifier=%s, context=%s, contextConfiguration=%s, contextVersion=%d, lastExecutedTicket=%s, totalCommandsExecuted=%d, " +
			     	 		 "totalCommandExecutionDuration=%d, totalCommandExecutionWaitingDuration=%d}", 
							 contextIdentifier, 
							 context,
							 contextConfiguration,
							 contextVersion,
							 lastExecutedTicket,
							 totalCommandsExecuted,
							 totalCommandExecutionDuration,
							 totalCommandExecutionWaitingDuration);
	}
}
