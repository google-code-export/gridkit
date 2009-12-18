/*
 * File: ContextsManager.java
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
import com.tangosol.util.ValueExtractor;

/**
 * <p>A {@link ContextsManager} provides the mechanism by which
 * we may register and manage {@link Context}s.</p>
 *  
 * @author Brian Oliver
 */
public interface ContextsManager {

	/**
	 * <p>Registers a {@link Context} with the specified {@link Identifier}
	 * and {@link ContextConfiguration}.</p>
	 * 
	 * <p>The returned {@link Identifier} may then be used to
	 * manage the {@link Context}, including submitting {@link Command}s
	 * for execution.</p>
	 * 
	 * @param identifier
	 * @param context
	 * @param contextConfiguration
	 */
	public Identifier registerContext(Identifier identifier, 
					 				  Context context,
									  ContextConfiguration contextConfiguration);
	
	
	/**
	 * <p>Registers a {@link Context} with the specified {@link Identifier}
	 * using a {@link DefaultContextConfiguration}.</p>
	 * 
	 * <p>The returned {@link Identifier} may then be used to
	 * manage the {@link Context}, including submitting {@link Command}s
	 * for execution.</p>
	 * 
	 * @param identifier
	 * @param context
	 */
	public Identifier registerContext(Identifier identifier, 
					  				  Context context);

	
	/**
	 * <p>Registers a {@link Context} with the specified contextName 
	 * and {@link ContextConfiguration}.</p>
	 * 
	 * <p>The returned {@link Identifier} may then be used to
	 * manage the {@link Context}, including submitting {@link Command}s
	 * for execution.</p>
	 * 
	 * @param contextName
	 * @param context
	 * @param contextConfiguration
	 */
	public Identifier registerContext(String contextName, 
					 				  Context context,
									  ContextConfiguration contextConfiguration);
	
	
	/**
	 * <p>Registers a {@link Context} with the specified contextName 
	 * using a {@link DefaultContextConfiguration}.</p>
	 * 
	 * <p>The returned {@link Identifier} may then be used to
	 * manage the {@link Context}, including submitting {@link Command}s
	 * for execution.</p>
	 * 
	 * @param contextName
	 * @param context
	 */
	public Identifier registerContext(String contextName, 
									  Context context);

	
	/**
	 * <p>Registers a {@link Context} with a specified {@link ContextConfiguration}.</p>
	 * 
	 * <p>NOTE: A unique {@link Identifier} will be generated for the {@link Context}.</p>
	 * 
	 * <p>The returned {@link Identifier} may then be used to
	 * manage the {@link Context}, including submitting {@link Command}s
	 * for execution.</p>
	 * 
	 * @param context
	 * @param contextConfiguration
	 */
	public Identifier registerContext(Context context,
					  				  ContextConfiguration contextConfiguration);

	
	/**
	 * <p>Registers a {@link Context} using a {@link DefaultContextConfiguration}.</p>
	 * 
	 * <p>NOTE: A unique {@link Identifier} will be generated for the {@link Context}.</p>
	 * 
	 * <p>The returned {@link Identifier} may then be used to
	 * manage the {@link Context}, including submitting {@link Command}s
	 * for execution.</p>
	 * 
	 * @param context
	 */
	public Identifier registerContext(Context context);


	/**
	 * <p>Returns the most recent updated version of the {@link Context} with the 
	 * specified {@link Identifier}.</p>
	 * 
	 * @param identifier
	 */
	public Context getContext(Identifier identifier);
	
	
	/**
	 * <p>Extracts a value from the most recently updated version of the
	 * {@link Context} with the provided {@link ValueExtractor}.</p>
	 *  
	 * @param identifier
	 * @param valueExtractor
	 */
	public Object extractValueFromContext(Identifier identifier, ValueExtractor valueExtractor);	
}
