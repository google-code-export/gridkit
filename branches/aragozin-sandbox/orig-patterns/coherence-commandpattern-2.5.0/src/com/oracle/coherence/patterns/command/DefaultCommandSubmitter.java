/*
 * File: DefaultCommandSubmitter.java
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
import com.oracle.coherence.patterns.command.ContextConfiguration.ManagementStrategy;
import com.oracle.coherence.patterns.command.internal.CancelCommandProcessor;
import com.oracle.coherence.patterns.command.internal.CommandExecutionRequest;
import com.oracle.coherence.patterns.command.internal.ContextWrapper;
import com.oracle.coherence.patterns.command.internal.SubmissionOutcome;
import com.oracle.coherence.patterns.command.internal.SubmitCommandExecutionRequestProcessor;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;

/**
 * <p>The default implementation of a {@link CommandSubmitter}.</p>
 * 
 * @author Brian Oliver
 * 
 * @see CommandSubmitter
 */
public class DefaultCommandSubmitter implements CommandSubmitter {

	/**
	 * <p>The default {@link CommandSubmitter}.</p>
	 */
	private final static CommandSubmitter INSTANCE = new DefaultCommandSubmitter();
	
	
	/**
	 * <p>Standard Constructor</p>
	 */
	public DefaultCommandSubmitter() {
		//ensure with have appropriate indexes on the CommandExecutionRequests caches
		CacheFactory.getCache(CommandExecutionRequest.getCacheName(ManagementStrategy.DISTRIBUTED)).addIndex(
			new ReflectionExtractor("getContextIdentifier"), 
			false, 
			null);

		CacheFactory.getCache(CommandExecutionRequest.getCacheName(ManagementStrategy.COLOCATED)).addIndex(
				new ReflectionExtractor("getContextIdentifier"), 
				false, 
				null);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public <C extends Context> Identifier submitCommand(Identifier contextIdentifier, 
												        Command<C> command, 
												        boolean acceptCommandIfContextDoesNotExist) {

		//create a CommandExecutionRequest to wrap the provided Command
		//(we do this as we need to track internal state around a Command)
		CommandExecutionRequest commandExecutionRequest = new CommandExecutionRequest(contextIdentifier, command);
		
		//submit the command to the context
		NamedCache contextsCache = CacheFactory.getCache(ContextWrapper.CACHENAME);
		SubmissionOutcome submissionOutcome = (SubmissionOutcome)contextsCache.invoke(
			contextIdentifier, 
			new SubmitCommandExecutionRequestProcessor(
				commandExecutionRequest,
				acceptCommandIfContextDoesNotExist));
	
		//handle the outcome of the submission
		if (submissionOutcome instanceof SubmissionOutcome.UnknownContext) 
			throw new IllegalArgumentException(String.format("Can't submit Command %s to Context %s as the Context does not exist", command, contextIdentifier));			
		
		else	
			return ((SubmissionOutcome.Accepted)submissionOutcome);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public <C extends Context> Identifier submitCommand(Identifier contextIdentifier, Command<C> command) {
		return submitCommand(contextIdentifier, command, false);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public <C extends Context> boolean cancelCommand(Identifier commandIdentifier) {
		SubmissionOutcome.Accepted submissionOutcome = (SubmissionOutcome.Accepted)commandIdentifier;
		
		NamedCache commandsCache = CacheFactory.getCache(
			CommandExecutionRequest.getCacheName(submissionOutcome.getManagementStrategy()));

		Boolean result = (Boolean)commandsCache.invoke(
			submissionOutcome.getCommandExecutionRequestKey(), 
			new CancelCommandProcessor());
		
		return result.booleanValue();
	}

	
	/**
	 * <p>Returns an instance of the {@link DefaultCommandSubmitter}.</p>
	 */
	public static CommandSubmitter getInstance() {
		return INSTANCE;
	}
}
