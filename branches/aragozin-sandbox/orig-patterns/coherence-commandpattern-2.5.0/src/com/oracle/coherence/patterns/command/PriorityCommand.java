/*
 * File: PriorityCommand.java
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

/**
 * <p>A {@link PriorityCommand} is a specialized {@link Command} that will be executed
 * prior to regular {@link Command}s.</p>
 * 
 * <p>The {@link PriorityCommand} is a marker interface.</p>
 * 
 * @author Brian Oliver
 *
 * @param <C>
 */
public interface PriorityCommand<C extends Context> extends Command<C> {

	//NOTE: This is a marker interface.
	
}
