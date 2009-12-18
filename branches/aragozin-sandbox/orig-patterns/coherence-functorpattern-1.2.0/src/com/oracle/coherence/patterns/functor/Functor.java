/*
 * File: Functor.java
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

import com.oracle.coherence.patterns.command.Command;
import com.oracle.coherence.patterns.command.Context;
import com.oracle.coherence.patterns.command.ExecutionEnvironment;

/**
 * <p>A {@link Functor} represents some action (and required parameters)
 * to be performed asynchronously against a {@link Context} (of type C) that 
 * returns some value of (type T).</p>
 * 
 * <p>{@link Functor}s are related to {@link Command}s, with the exception that
 * {@link Functor}s may return a value to the submitter where as {@link Command}s
 * may not.</p> 
 *
 * @param <C> The type of the {@link Context}
 * @param <T> The return type from the execution of the {@link Functor}
 * 
 * @author Brian Oliver
 * 
 * @see Context
 * @see Command
 */
public interface Functor<C extends Context, T> {

	/**
	 * <p>Executes the {@link Functor} using the provided 
	 * {@link ExecutionEnvironment} returning a value of type T.</p>
	 *  
	 * @param executionEnvironment The environment in which the {@link Command}
	 * 							   is being executed.
	 */
	public T execute(ExecutionEnvironment<C> executionEnvironment);	
	
}
