/*
 * File: CommandExecutorMBean.java
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

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.ticketing.Ticket;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.internal.CommandExecutor.State;

/**
 * <p>The {@link CommandExecutorMBean} specifies the JMX monitoring 
 * and managability interface for {@link CommandExecutor}s.</p>
 * 
 * @author Brian Oliver
 */
public interface CommandExecutorMBean {
	
	/**
	 * <p>Returns the {@link Context} {@link Identifier} as a string.</p>
	 */
	public String getContextIdentity();
	
	
	/**
	 * <p>Returns the version number of the {@link Context} that the {@link CommandExecutor}
	 * is managing.</p>
	 */
	public long getContextVersion();
	
	
	/**
	 * <p>Returns the current id that is being used to issue {@link Ticket}s.</p>
	 * 
	 * <p>NOTE: If another {@link CommandExecutor} is created for same  
	 * {@link Context} with the same {@link Identifier} in the cluster (due 
	 * to recovery / load-balancing of {@link Context}s), a <b>new</b> {@link Ticket} 
	 * issuer id will be allocated to the new {@link CommandExecutor}.
	 * This ensures that for each instance of a {@link CommandExecutor}, the 
	 * {@link Ticket} issuer id will be different, thus allowing us to "order"
	 * {@link Command}s send to particular {@link CommandExecutor}s.</p>
	 */
	public long getTicketIssuerId();

	
	/**
	 * <p>Returns the current {@link State} of the {@link CommandExecutor}.</p>
	 */
	public String getStatus();
	
	
	/**
	 * <p>Returns the total number of {@link Command}s that are yet to be executed</p>
	 */
	public long getTotalCommandsPendingExecution();
	
	
	/**
	 * <p>Returns the total number of {@link Command}s for the {@link Context} 
	 * that have been executed (regardless of the owner)</p>
	 */
	public long getTotalCommandsExecuted();
	
	
	/**
	 * <p>Returns the total time (in milliseconds) that the 
	 * {@link Command}s executed thus far have taken to execute.</p>
	 */
	public long getTotalCommandExecutionDuration();

	
	/**
	 * <p>Returns the total time (in milliseconds) that the 
	 * {@link Command}s executed thus far have waited to execute (ie: the queuing time).</p>
	 */
	public long getTotalCommandExecutionWaitingDuration();
	
	
	/**
	 * <p>Returns the number of {@link Command}s that have been submitted locally to the
	 * current owner of the {@link Context} (ie: local {@link CommandExecutor}).</p>
	 */
	public long getLocalCommandsSubmitted();
	
	
	/**
	 * <p>Returns the number of {@link Command}s that have been executed locally by the
	 * current owner of the {@link Context} (ie: local {@link CommandExecutor}).</p>
	 */
	public long getLocalCommandsExecuted();
	
	
	/**
	 * <p>Returns the average execution time (in milliseconds)
	 * for the {@link Command}s executed locally by the current owner of the {@link Context}
	 * (ie: local {@link CommandExecutor}).</p>
	 */	
	public double getLocalAverageCommandExecutionDuration();

	
	/**
	 * <p>Returns the minimum time (in milliseconds) that a {@link Command} has taken
	 * to execute locally by the current owner of the {@link Context} (ie: the local {@link CommandExecutor})
	 */
	public double getLocalMinimumCommandExecutionDuration();

	
	/**
	 * <p>Returns the maximum time (in milliseconds) that a {@link Command} has taken
	 * to execute locally by the current owner of the {@link Context} (ie: the local {@link CommandExecutor})
	 */
	public double getLocalMaximumCommandExecutionDuration();

	
	/**
	 * <p>Returns the execution time (in milliseconds) for the last {@link Command} executed 
	 * locally by the current owner of the {@link Context} (ie: the local {@link CommandExecutor})
	 */
	public double getLocalLastCommandExecutionDuration();
	
	/**
	 * <p>Returns the local command execution service duration (in milliseconds). This includes
	 * the time to get the command as well as the time to execute the command.</p>
	 */
	public double getLocalCommandExecutionServiceDuration();
}
