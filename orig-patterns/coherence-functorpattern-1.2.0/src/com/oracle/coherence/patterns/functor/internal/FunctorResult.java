/*
 * File: FunctorResult.java
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

import com.oracle.coherence.patterns.functor.Functor;

/**
 * <p>A {@link FunctorResult} represents the result of executing 
 * one or more {@link Functor}s.</p>
 * 
 * <p>For some types of {@link Functor} execution(s), a single value
 * may be returned.  For others, like streaming {@link Functor}s,
 * multiple values may be returned.</p>
 * 
 * @author Brian Oliver
 */
public interface FunctorResult {
	
	/**
	 * <p>The Coherence Cache that will hold {@link Functor} execution results.</p>
	 */
	public static final String CACHENAME = "coherence.functorpattern.results";

	
	/**
	 * <p>Returns if the execution has been completed and the
	 * associated result is completed.</p>
	 * 
	 * <p>NOTE: The execution of some functor(s) may return (or stream back) 
	 * multiple results. The purpose of this method is to determine when 
	 * there are no more results to be returned.</p>
	 */
	public boolean isComplete();
}
