/*
 * File: CommandExecutorManager.java
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.common.threading.ThreadFactories;
import com.oracle.coherence.patterns.command.Context;
import com.tangosol.net.BackingMapManagerContext;

/**
 * <p>An {@link CommandExecutorManager} is responsible for managing and scheduling
 * the current {@link CommandExecutor}s for a Coherence Cluster Member.</p>
 *
 * @author Brian Oliver
 */
public final class CommandExecutorManager {

	/**
	 * <p>The map of {@link Context} {@link ContextIdentifier}s to associated {@link CommandExecutor}s
	 * that the {@link CommandExecutorManager} is managing.</p>
	 */
	private static ConcurrentHashMap<Identifier, CommandExecutor> commandExecutors;
	
	
	/**
	 * <p>An {@link ScheduledExecutorService} that we can use to perform asynchronous tasks, like
	 * managing {@link CommandExecutor}s.</p> 
	 */
	private static ScheduledExecutorService executorService;
	
	
	/**
	 * <p>A {@link ThreadGroup} for the {@link ExecutorService} so that {@link Thread}s
	 * are not allocated within Coherence owned {@link Thread}s.</p>
	 */
	private static ThreadGroup executorServiceThreadGroup;
	
	
	/**
	 * <p>Returns the {@link CommandExecutor} for the {@link Context} identified by the provided {@link Identifier}.
	 * If a {@link CommandExecutor} does not yet exist, one is created and associated with the specified 
	 * {@link BackingMapManagerContext}.</p>
	 * 
	 * @param contextIdentifier The {@link Identifier} of the {@link Context} for which we 
	 * 							are requesting a {@link CommandExecutor}.
	 * @param backingMapManagerContext The {@link BackingMapManagerContext} to which the {@link CommandExecutor}
	 * 								   is associated
	 * 
	 * @return {@link CommandExecutor}
	 */
	public static CommandExecutor ensureCommandExecutor(Identifier contextIdentifier, 
													    BackingMapManagerContext backingMapManagerContext) {
		
		CommandExecutor commandExecutor = commandExecutors.get(contextIdentifier);

		if (commandExecutor == null) {	
			if (Logger.isEnabled(Logger.DEBUG)) Logger.log(Logger.DEBUG, "Creating CommandExecutor for %s", contextIdentifier);

			//create and register the new CommandExecutor
			commandExecutor = new CommandExecutor(contextIdentifier, backingMapManagerContext);
			CommandExecutor previouslyRegisteredCommandExecutor = commandExecutors.putIfAbsent(contextIdentifier, commandExecutor);
			
			if (previouslyRegisteredCommandExecutor == null) {
				if (Logger.isEnabled(Logger.DEBUG)) Logger.log(Logger.DEBUG, "Created CommandExecutor for %s", contextIdentifier);				
			} else {
				//use the previously created CommandExecutor
				commandExecutor = previouslyRegisteredCommandExecutor;
				if (Logger.isEnabled(Logger.DEBUG)) Logger.log(Logger.DEBUG, "Using previously created CommandExecutor for %s", contextIdentifier);				
			}
		}
		
		return commandExecutor;
	}

	
	/**
	 * <p>Returns the {@link CommandExecutor} for the {@link Context} identified by the provided {@link Identifier}.</p>
	 * 
	 * @param contextIdentifier The {@link Identifier} of the {@link Context} for which we are requesting a {@link CommandExecutor}.
	 * 
	 * @return <code>null</code> if no such {@link CommandExecutor} exists with the specified {@link Identifier}.
	 */
	public static CommandExecutor getCommandExecutor(Identifier contextIdentifier) {
		CommandExecutor commandExecutor = commandExecutors.get(contextIdentifier);
		
		return commandExecutor;
	}
	
	
	/**
	 * <p>Removes the {@link CommandExecutor} for the {@link Context} with the specified {@link Identifier}.</p>
	 * 
	 * @param contextIdentifier
	 */
	public static CommandExecutor removeCommandExecutor(Identifier contextIdentifier) {
		if (Logger.isEnabled(Logger.DEBUG)) Logger.log(Logger.DEBUG, "Removing CommandExecutor for %s", contextIdentifier);
		
		return commandExecutors.remove(contextIdentifier);
	}
	
	
	/**
	 * <p>Schedules the provided {@link Runnable} to be executed at a specified time in the future.</p>
	 * 
	 * @param runnable The {@link Runnable} to execute 
	 * @param delay The minimum delay from now until the execution should commence
	 * @param timeUnit The {@link TimeUnit} for the specified delay
	 */
	public static void schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
		if (delay == 0)
			executorService.execute(runnable);
		else
			executorService.schedule(runnable, delay, timeUnit);
	}
	
	
	/**
	 * <p>Static initialization.</p>
	 */
	static {
		//TODO: get the size of the thread pool for our executorService from the system properties (or cache config?)
		
		commandExecutors = new ConcurrentHashMap<Identifier, CommandExecutor>();
		executorServiceThreadGroup = new ThreadGroup("CommandExecutorManager");
		executorService = Executors.newScheduledThreadPool(5, ThreadFactories.newThreadFactory(true, "CommandExecutor", executorServiceThreadGroup));
	}
}
