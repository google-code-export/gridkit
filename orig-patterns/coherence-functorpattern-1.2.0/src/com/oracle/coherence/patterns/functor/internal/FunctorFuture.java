/*
 * File: FunctorFuture.java
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

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.functor.Functor;
import com.oracle.coherence.patterns.functor.FunctorSubmitter;

/**
 * <p>A {@link FunctorFuture} is used to asynchronously capture
 * and return {@link FunctorResult}s back to the submitter of 
 * one or more {@link Functor}s.</p>
 * 
 * @author Brian Oliver
 */
public interface FunctorFuture {
	
	/**
	 * <p>Returns the {@link Identifier} of the {@link FunctorSubmitter}
	 * that produced this {@link FunctorFuture}.</p>
	 */
	public Identifier getFunctorSubmitterIdentifier();

	
	/**
	 * <p>Returns the {@link Identifier} of the {@link FunctorResult}
	 * that will be produced for this {@link FunctorFuture}.</p>
	 */
	public Identifier getFunctorResultIdentifier();

	
	/**
	 * <p>Accepts the specified {@link FunctorResult}.</p>
	 */
	public void acceptFunctorResult(FunctorResult functorResult);
}
