/*
 * File: SingleFunctorFuture.java
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
package com.oracle.coherence.patterns.functor.internal;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.functor.Functor;
import com.oracle.coherence.patterns.functor.FunctorSubmitter;

/**
 * <p>The implementation of the {@link FunctorFuture} and {@link Future} that will represent
 * the result returned from an asynchronously executed {@link Functor}.</p>
 * 
 * @author Brian Oliver
 * 
 * @param <T>
 */
public class SingleFunctorFuture<T> implements FunctorFuture, Future<T> {

	/**
	 * <p>The {@link Identifier} of the {@link FunctorSubmitter} that
	 * produced this {@link FunctorFuture}.</p>
	 */
	private Identifier functorSubmitterIdentifier;
	
	
	/**
	 * <p>The {@link Identifier} that will be used to identify the result
	 * of the {@link Functor} execution.</p>
	 */
	private Identifier functorResultIdentifier;
	
	
	/**
	 * <p>The {@link FunctorResult} from executing the {@link Functor}.</p>
	 */
	private SingleFunctorResult<T> functorResult;
	
	
	/**
	 * <p>The {@link FunctorSubmitter} that submitted the {@link Functor} for this future.</p>
	 */
	private FunctorSubmitter functorSubmitter;
	
	
	/**
	 * <p>The {@link Identifier} associated with the submitted {@link Functor} for this future.</p>
	 */
	private Identifier functorIdentifier;
	
	
	/**
	 * <p>Whether this FutureResult has been canceled or not.</p>
	 */
	private boolean isCanceled;

	
	/**
	 * <p>Standard Constructor.</p>
	 * 
	 * @param functorSubmitterIdentifier
	 * @param functorResultIdentifier
	 * @param functorSubmitter
	 */
	public SingleFunctorFuture(Identifier functorSubmitterIdentifier,
							   Identifier functorResultIdentifier,
							   FunctorSubmitter functorSubmitter) {
		this.functorSubmitterIdentifier = functorSubmitterIdentifier;
		this.functorResultIdentifier = functorResultIdentifier;
		this.functorSubmitter = functorSubmitter;
		this.functorResult = null;
		this.isCanceled = false;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Identifier getFunctorSubmitterIdentifier() {
		return functorSubmitterIdentifier;
	}
	

	/**
	 * {@inheritDoc}
	 */
	public Identifier getFunctorResultIdentifier() {
		return functorResultIdentifier;
	}
	
	/**
	 * Return the {@link Identifier} associated with the submitted {@link Functor} for this future.
	 * @return the {@link Identifier} associated with the submitted {@link Functor} for this future
	 */
	public Identifier getFunctorIdentifier() {
		return functorIdentifier;
	}
	
	/**
	 * Set the {@link Identifier} associated with the submitted {@link Functor} for this future.
	 * @param identifier the {@link Identifier} associated with the submitted {@link Functor} for this future
	 */
	public void setFunctorIdentifier(Identifier identifier) {
		this.functorIdentifier = identifier;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void acceptFunctorResult(FunctorResult functorResult) {
		synchronized(this) {
			this.functorResult = (SingleFunctorResult<T>)functorResult;
			notify();
		}
	}
	
	
	/**
	 * Attempts to cancel execution of the submitted task. This attempt will fail if the task has already completed, 
	 * already been canceled, or could not be canceled for some other reason. If successful, and this task has not 
	 * started when cancel is called, this task will never run. Attempts to interrupt an already running Functor is
	 * not currently supported and will have no effect.
	 * 
	 * @param mayInterruptIfRunning  if true, the cancel operation should attempt to stop the running Functor. This 
	 *                               functionality is not currently supported and will have no effect.
	 *                               
	 * @return false if the task could not be canceled, typically because it has already completed normally; 
	 *         true otherwise                               
	 */
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (isCanceled) {
			return true;
	    } else {
			synchronized(this) {
				if (functorResult != null && (functorResult.getException() != null || functorResult.getValue() != null)) {
					return false;
				}
			}	
			isCanceled = functorSubmitter.cancelCommand(functorIdentifier);
			return isCanceled;
	    }
	}

	
	/**
	 * {@inheritDoc}
	 */
	public T get() throws InterruptedException, ExecutionException {
		if (isCanceled) {
			throw new CancellationException();
		}
		
		synchronized(this) {
			if (!isDone()) {
				wait();
			}
		}
		
		//if the result has failed, re-throw the exception
		if (functorResult.getException() != null) 
			throw new ExecutionException("Execution Failed", functorResult.getException());
		else
			return functorResult.getValue();
	}

	
	/**
	 * {@inheritDoc}
	 */
	public T get(long time, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
		if (isCanceled) {
			throw new CancellationException();
		}
		
		synchronized(this) {
			if (!isDone()) {
				timeUnit.timedWait(this, time);
			}
			
			if (functorResult == null) {
				throw new TimeoutException();
			} else
			{
				//if the result has failed, re-throw the exception
				if (functorResult.getException() != null)
					throw new ExecutionException("Execution Failed", functorResult.getException());
				else
					return functorResult.getValue();
			}
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isCancelled() {
		return false;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isDone() {
		return functorResult != null;
	}		
}
