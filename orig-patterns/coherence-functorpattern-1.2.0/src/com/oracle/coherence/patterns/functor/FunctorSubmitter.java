/*
 * File: FunctorSubmitter.java
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

import java.util.concurrent.Future;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.CommandSubmitter;
import com.oracle.coherence.patterns.command.Context;

/**
 * <p>A {@link FunctorSubmitter} provides the mechanism by which
 * {@link Functor}s may be submitted for execution in a specified
 * identifiable {@link Context}.</p>
 *
 * <p>A {@link FunctorSubmitter} is a specialized {@link CommandSubmitter}.
 * All {@link FunctorSubmitter}s are capable of also submitting
 * {@link Command}s for execution.</p>
 * 
 * @author Brian Oliver
 *
 * @see Functor
 * @see CommandSubmitter
 */
public interface FunctorSubmitter extends CommandSubmitter {

	/**
	 * <p>Asynchronously submits the provided {@link Functor} for execution
	 * against the {@link Context} with the specified {@link Identifier},
	 * returning a {@link Future} that may be used to access the return value 
	 * from the {@link Functor}</p>
	 * 
	 * @param contextIdentifier
	 * @param functor
	 * 
	 * @throws IllegalArgumentException If no {@link Context} with the specified {@link Identifier} exists.
	 */
	public <C extends Context, T> Future<T> submitFunctor(Identifier contextIdentifier, Functor<C, T> functor);	

	
	/**
	 * <p>Asynchronously submits the provided {@link Functor} for execution
	 * against the {@link Context} with the specified {@link Identifier},
	 * returning a {@link Future} that may be used to access the return value 
	 * from the {@link Functor}</p>
	 * 
	 * <p>If the specified {@link Context} does not exist, but the allowSubmissionWhenContextDoesNotExist
	 * parameter is <code>true</code>, the provided {@link Functor} will be queued for
	 * execution when the {@link Context} is created.</p>
	 * 
	 * @param contextIdentifier
	 * @param functor
	 * @param allowSubmissionWhenContextDoesNotExist
	 * 
	 * @throws IllegalArgumentException If no {@link Context} with the specified {@link Identifier} exists 
	 * 									and allowSubmissionWhenContextDoesNotExist is <code>false</code>
	 */
	public <C extends Context, T> Future<T> submitFunctor(Identifier contextIdentifier, Functor<C, T> functor, boolean allowSubmissionWhenContextDoesNotExist);

}
