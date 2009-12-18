/*
 * File: Command.java
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

import java.io.Serializable;

import com.oracle.coherence.patterns.command.internal.CommandExecutor;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PortableObject;

/**
 * <p>A {@link Command} represents some action (together with required state)
 * that is to be asynchronously executed against a {@link Context}.</p>
 * 
 * <p>As {@link Command}s are cached objects (ie: placed in Coherence caches), 
 * they need to at least implement {@link Serializable}, or better still, implement
 * {@link ExternalizableLite} or {@link PortableObject}.</p> 
 * 
 * <p>To submit a {@link Command} for execution against a {@link Context} you 
 * should use a {@link CommandSubmitter}.</p>
 *  
 * @see Context
 * @see CommandSubmitter
 * @see CommandExecutor (internal)
 *
 * @author Brian Oliver
 */
public interface Command<C extends Context> {

	/**
	 * <p>Executes the {@link Command} using the provided 
	 * {@link ExecutionEnvironment}.</p>
	 *  
	 * @param executionEnvironment The environment in which the {@link Command}
	 * 							   is being executed.
	 */
	public abstract void execute(ExecutionEnvironment<C> executionEnvironment);	
	
}
