/*
 * File: ExecutionEnvironment.java
 * 
 * Copyright (c) 2008-2009. All Rights Reserved. Oracle Corporation.
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
package com.oracle.coherence.patterns.command;

import java.io.Externalizable;
import java.io.Serializable;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.ticketing.Ticket;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PortableObject;

/**
 * <p>An {@link ExecutionEnvironment} is provided to a {@link Command}
 * when it is being executed.  The {@link ExecutionEnvironment} is used
 * to provide the "environment" for the {@link Command}, including;</p>
 * 
 * <ol>
 * 		<li>The {@link Context} in which the execution is taking place</li>
 * 
 * 		<li>An {@link Identifier} that uniquely represents the 
 * 			specific execution request. This may be used to assist in 
 * 			ensuring idempotent {@link Command} execution when an
 * 			execution is recovering.</li>
 * 
 * 		<li>A {@link Ticket} to determine the order in which the 
 * 			specific {@link Command} execution is taking place.</li>
 * 
 * 		<li>A flag to determine if the execution is being recovered.</li>
 * </ol>
 *
 * @author Brian Oliver
 */
public interface ExecutionEnvironment<C extends Context> {

	/**
	 * <p>Returns the {@link Identifier} of the {@link Context} in which
	 * the execution is taking place.</p>
	 */
	public Identifier getContextIdentifier();
	
	
	/**
	 * <p>Returns the {@link Context} in which an execution is taking place.</p>
	 */
	public C getContext();
	

	/**
	 * <p>Use this method to update the {@link Context} (as necessary) during
	 * the execution of a {@link Command}.</p>
	 * 
	 * @param context
	 * 
	 * @throws IllegalStateException If the context value provided is <code>null</code>
	 */
	public void setContext(C context);
	
	
	/**
	 * <p>Returns the {@link ContextConfiguration} for the {@link Context} in
	 * which the execution is taking place.</p>
	 */
	public ContextConfiguration getContextConfiguration();
	
	
	/**
	 * <p>Returns the unique {@link Ticket} (within the scope of the
	 * {@link Context}) issued to the execution taking place with this
	 * {@link ExecutionEnvironment}.</p>
	 * 
	 * <p>This {@link Ticket} may be used to order and compare previous
	 * executions against the same {@link Context}.</p>
	 */
	public Ticket getTicket();
	
	
	/**
	 * <p>Returns if the {@link ExecutionEnvironment} is recovering due to 
	 * either a Coherence Cluster Recovery or Repartitioning event.</p>
	 */
	public boolean isRecovering();

	
	/**
	 * <p>Determine if state has been previously saved (called a "checkpoint") for
	 * the currently executing {@link Command}.</p>
	 */
	public boolean hasCheckpoint();
	
	
	/**
	 * <p>Immediately save the specified state as a "checkpoint" during the execution
	 * of a {@link Command} so that it may later be retrieved (via {@link #loadCheckpoint()}).</p>
	 * 
	 * <p>Using "checkpoints" can simplify the recovery of previous attempts at executing
	 * a {@link Command}.</p>
	 *  
	 * <p>NOTE: The state must be serializable in some manner, ie: with {@link Serializable}, 
	 * {@link Externalizable}, {@link ExternalizableLite} and/or {@link PortableObject}.</p> 
	 *  
	 * @param state
	 */
	public void saveCheckpoint(Object state);
	
	
	/**
	 * <p>Load the previously saved "checkpoint" state for the currently executing {@link Command}
	 * that was stored using {@link #saveCheckpoint(Object)}.</p>
	 * 
	 * @return <code>null</code> if there is no previously saved "checkpoint" state.
	 */
	public Object loadCheckpoint();
	
	
	/**
	 * <p>Immediately removes any previously saved "checkpoint" state for the currently executing
	 * {@link Command}.</p>
	 * 
	 * <p>NOTE: This is functionally equivalent to {@link #saveCheckpoint(Object)} with <code>null</code>.</p>
	 */
	public void removeCheckpoint();
}
