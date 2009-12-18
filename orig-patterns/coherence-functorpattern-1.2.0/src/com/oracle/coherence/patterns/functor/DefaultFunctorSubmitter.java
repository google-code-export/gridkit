/*
 * File: DefaultFunctorSubmitter.java
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
package com.oracle.coherence.patterns.functor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.identifiers.UUIDBasedIdentifier;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.CommandSubmitter;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.DefaultCommandSubmitter;
import com.oracle.coherence.patterns.functor.internal.SingleFunctorCommand;
import com.oracle.coherence.patterns.functor.internal.FunctorFuture;
import com.oracle.coherence.patterns.functor.internal.FunctorResult;
import com.oracle.coherence.patterns.functor.internal.SingleFunctorFuture;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MultiplexingMapListener;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.MapEventFilter;

/**
 * <p>The default implementation of a {@link FunctorSubmitter}.</p>
 * 
 * @author Brian Oliver
 *
 * @see FunctorSubmitter
 */
public class DefaultFunctorSubmitter implements CommandSubmitter, FunctorSubmitter {

	/**
	 * <p>The default {@link FunctorSubmitter}.</p>
	 */
	private final static FunctorSubmitter INSTANCE = new DefaultFunctorSubmitter();
	
	
	/**
	 * <p>The unique {@link Identifier} for the {@link FunctorSubmitter}.
	 * This is used to register listeners for the results of {@link Functor} 
	 * executions.</p>
	 */
	private Identifier identifier;
	
	
	/**
	 * <p>A map of {@link FunctorFuture}s for the {@link Functor}s 
	 * that were submitted by this {@link FunctorSubmitter} but are 
	 * yet to complete execution.</p> 
	 */
	private ConcurrentHashMap<Identifier, FunctorFuture> functorFutures;
	
	
	/**
	 * <p>The {@link CommandSubmitter} that will be used to submit {@link Functor}s
	 * for execution.</p>
	 */
	private CommandSubmitter commandSubmitter;
	
	
	/**
	 * <p>Standard Constructor (with an explicitly provided {@link Identifier}).</p>
	 * 
	 * @param identifier
	 * @param commandSubmitter
	 */
	public DefaultFunctorSubmitter(Identifier identifier, CommandSubmitter commandSubmitter) {
		this.identifier = identifier;
		this.functorFutures = new ConcurrentHashMap<Identifier, FunctorFuture>();
		this.commandSubmitter = commandSubmitter;
		
		//register a map listener on the functor results cache to handle the delivery
		//of Results back to this FunctorSubmitter from the Coherence member 
		//that executed the task.  We use the results cache as a mechanism to hand back 
		//results to the FunctorSubmitter from the CommandExecutors
		NamedCache functorResultsCache = CacheFactory.getCache(FunctorResult.CACHENAME);
		functorResultsCache.addMapListener(new MultiplexingMapListener() {
			protected void onMapEvent(MapEvent mapEvent) {
				if (mapEvent.getId() == MapEvent.ENTRY_INSERTED ||
					mapEvent.getId() == MapEvent.ENTRY_UPDATED) {
					
					//the FunctorResult is delivered to us as the mapEvent new value
					FunctorResult functorResult = (FunctorResult)mapEvent.getNewValue();
					
					//find the FunctorFuture for the FunctorResult just delivered (using the functorResultIdentifier)
					FunctorFuture functorFuture = functorFutures.get(mapEvent.getKey());
					
					//have the FunctorFuture accept the provided result
					if (functorFuture != null) {
						functorFuture.acceptFunctorResult(functorResult);
					}
					
					//remove the FunctorResult and FunctorFuture iff the result is complete
					if (functorResult.isComplete()) {
						mapEvent.getMap().remove(mapEvent.getKey());
						functorFutures.remove(mapEvent.getKey());
					}
				}				
			}
		}, new MapEventFilter(MapEventFilter.E_INSERTED | MapEventFilter.E_UPDATED,
						      new EqualsFilter("getFunctorSubmitterIdentifier", identifier)), false);		
	}

	
	/**
	 * <p>Standard Constructor (using a {@link DefaultCommandSubmitter}).</p>
	 */
	public DefaultFunctorSubmitter(Identifier identifier) {
		this(identifier, DefaultCommandSubmitter.getInstance());
	}

	
	/**
	 * <p>Standard Constructor (without an explicitly 
	 * defined {@link Identifier} and using a {@link DefaultCommandSubmitter}).</p>
	 */
	public DefaultFunctorSubmitter() {
		this(UUIDBasedIdentifier.newInstance());
	}
	
	
	/**
	 * <p>Returns the {@link Identifier} of the {@link FunctorSubmitter}.</p>
	 */
	public Identifier getIdentifier() {
		return identifier;
	}

	
	/**
	 * {@inheritDoc}
	 */	
	public <C extends Context> Identifier submitCommand(Identifier contextIdentifier, Command<C> command) {
		return commandSubmitter.submitCommand(contextIdentifier, command);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public <C extends Context> Identifier submitCommand(Identifier contextIdentifier, Command<C> command, boolean allowSubmissionWhenContextDoesNotExist) {
		return commandSubmitter.submitCommand(contextIdentifier, command, allowSubmissionWhenContextDoesNotExist);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public <C extends Context, T> Future<T> submitFunctor(Identifier contextIdentifier, 
														  Functor<C, T> functor) {
		
		return submitFunctor(contextIdentifier, functor, false);
	}


	/**
	 * {@inheritDoc}
	 */
	public <C extends Context, T> Future<T> submitFunctor(Identifier contextIdentifier, 
														  Functor<C, T> functor, 
														  boolean allowSubmissionWhenContextDoesNotExist) {
		
		//create an identifier for the functor result
		Identifier functorResultIdentifier = UUIDBasedIdentifier.newInstance();
		
		//create a future representing the result of the functor to be executed.
		SingleFunctorFuture<T> future = new SingleFunctorFuture<T>(identifier, functorResultIdentifier, this);
		
		//register the pending future for this functor execution
		functorFutures.put(functorResultIdentifier, future);
		
		//submit the functor (as a command) for execution
		Identifier commandIdentifier = 
				submitCommand(contextIdentifier, new SingleFunctorCommand<C, T>(identifier, functorResultIdentifier, functor), allowSubmissionWhenContextDoesNotExist);
		
		future.setFunctorIdentifier(commandIdentifier);
		
		return future;
	}


	/**
	 * {@inheritDoc}
	 */
	public <C extends Context> boolean cancelCommand(Identifier commandIdentifier) {
		return commandSubmitter.cancelCommand(commandIdentifier);
	}
	
	
	/**
	 * <p>Returns an instance of the {@link DefaultFunctorSubmitter}.</p>
	 */
	public static FunctorSubmitter getInstance() {
		return INSTANCE;
	}

}
