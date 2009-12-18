/*
 * File: Context.java
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

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PortableObject;

/**
 * <p>A {@link Context} is an object that captures the shared state that  
 * zero or more {@link Command}s may use during execution.</p>
 *  
 * <p>As {@link Context}s are cached objects (ie: placed in Coherence caches), 
 * they need to at least implement {@link Serializable}, or better still, 
 * implement {@link ExternalizableLite} or {@link PortableObject}.</p> 
 * 
 * <p>{@link Context}s don't need to implement any special methods.  The
 * {@link Context} interface simply marks that an object is considered a 
 * {@link Context} against which {@link Command}s may be executed.</p>
 * 
 * <p>To register {@link Context}s so that {@link Command}s may be executed
 * against them, use a {@link ContextsManager}.</p>
 * 
 * @see ContextsManager
 * @see Command
 *  
 * @author Brian Oliver
 */

public interface Context {
	
	/**
	 * <p>As this is a marker interface, there are no declarations.</p>
	 */
	
}
