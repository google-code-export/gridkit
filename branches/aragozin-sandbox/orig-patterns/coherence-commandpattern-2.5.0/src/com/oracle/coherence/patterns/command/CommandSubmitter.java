/*
 * File: CommandSubmitter.java
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
package com.oracle.coherence.patterns.command;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.ticketing.Ticket;

/**
 * <p>A {@link CommandSubmitter} provides the mechanisms by which
 * we submit {@link Command} execution within {@link Context}s.</p>
 *  
 * @author Brian Oliver
 */
public interface CommandSubmitter {
	
	/**
	 * <p>Asynchronously submits the provided {@link Command} for execution
	 * against the {@link Context} with the specified {@link Identifier} and 
	 * returns a {@link Ticket} that may be used to cancel the execution of the {@link Command}.</p>
	 * 
	 * @param contextIdentifier
	 * @param command
	 * 
	 * @return The {@link Identifier} that was issued to track the execution of the {@link Command}
	 *  
	 * @throws IllegalArgumentException If no {@link Context} with the specified {@link Identifier} exists.
	 */
	public <C extends Context> Identifier submitCommand(Identifier contextIdentifier, Command<C> command);	

	
	/**
	 * <p>Asynchronously submits the provided {@link Command} for execution
	 * against the {@link Context} with the specified {@link Identifier} and 
	 * returns a {@link Ticket} that may be used to cancel the execution of the {@link Command}.</p>
	 * 
	 * <p>If the specified {@link Context} does not exist, but the allowSubmissionWhenContextDoesNotExist
	 * parameter is <code>true</code>, the provided {@link Command} will be queued for
	 * execution when the {@link Context} is created.</p>
	 * 
	 * @param contextIdentifier
	 * @param command
	 * @param allowSubmissionWhenContextDoesNotExist
	 * 
	 * @return The {@link Identifier} that was issued to track the execution of the {@link Command}
	 * 
	 * @throws IllegalArgumentException If no {@link Context} with the specified {@link Identifier} exists 
	 * 									and allowSubmissionWhenContextDoesNotExist is <code>false</code>
	 */
	public <C extends Context> Identifier submitCommand(Identifier contextIdentifier, Command<C> command, boolean allowSubmissionWhenContextDoesNotExist);

	
	/**
	 * <p>Attempts to cancel the execution of the {@link Command} that was issued with the specified {@link Ticket}.</p>
	 * 
	 * <p>NOTE: If the {@link Command} has already been executed or has commenced execution, it may not be canceled.  
	 * Only {@link Command}s that have not been executed may be canceled.  All other requests are ignored.</p>
	 * 
	 * @param <C>
	 * @param commandIdentifier
	 */
	public <C extends Context> boolean cancelCommand(Identifier commandIdentifier);
}
