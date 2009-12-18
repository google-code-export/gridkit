/*
 * File: DefaultContextsManager.java
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
import com.oracle.coherence.common.identifiers.StringBasedIdentifier;
import com.oracle.coherence.common.identifiers.UUIDBasedIdentifier;
import com.oracle.coherence.patterns.command.internal.ContextWrapper;
import com.oracle.coherence.patterns.command.internal.CreateContextProcessor;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.ChainedExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.PresentFilter;
import com.tangosol.util.processor.ConditionalProcessor;
import com.tangosol.util.processor.ExtractorProcessor;

/**
 * <p>The default implementation of a {@link ContextsManager}.</p>
 * 
 * @author Brian Oliver
 * 
 * @see ContextsManager
 */
public class DefaultContextsManager implements ContextsManager {

	/**
	 * <p>The default {@link ContextsManager}.</p>
	 */
	private final static ContextsManager INSTANCE = new DefaultContextsManager();
	
	
	/**
	 * <p>Standard Constructor</p>
	 */
	public DefaultContextsManager() {
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Identifier registerContext(Identifier contextIdentifier, 
									  Context context,
									  ContextConfiguration contextConfiguration) {

		//attempt to create the context 
		NamedCache contextsCache = CacheFactory.getCache(ContextWrapper.CACHENAME);
		contextsCache.invoke(contextIdentifier, new CreateContextProcessor(context, contextConfiguration));
		
		return contextIdentifier;
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Identifier registerContext(Identifier contextIdentifier, 
									  Context context) {
		return registerContext(contextIdentifier, context, new DefaultContextConfiguration());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Identifier registerContext(String contextName, 
									  Context context, 
									  ContextConfiguration contextConfiguration) {
		return registerContext(StringBasedIdentifier.newInstance(contextName), 
							   context,
							   contextConfiguration);
	}

	
	/**
	 * {@inheritDoc}
	 */
	public Identifier registerContext(String contextName, 
									  Context context) {
		return registerContext(contextName, context, new DefaultContextConfiguration());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Identifier registerContext(Context context,
					 				  ContextConfiguration contextConfiguration) {
		return registerContext(UUIDBasedIdentifier.newInstance(), 
							   context,
							   contextConfiguration);
	}


	/**
	 * {@inheritDoc}
	 */
	public Identifier registerContext(Context context) {
		return registerContext(context, new DefaultContextConfiguration());
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Context getContext(Identifier identifier) {
		NamedCache contextsCache = CacheFactory.getCache(ContextWrapper.CACHENAME);
		return (Context)contextsCache.invoke(identifier, new ExtractorProcessor("getContext"));
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public Object extractValueFromContext(Identifier identifier, ValueExtractor valueExtractor) {
		NamedCache contextsCache = CacheFactory.getCache(ContextWrapper.CACHENAME);
		return contextsCache.invoke(identifier, 
									new ConditionalProcessor(
										PresentFilter.INSTANCE,
										new ExtractorProcessor(
											new ChainedExtractor(
												new ReflectionExtractor("getContext"),
												valueExtractor)
											)
										)
									);
	}
	
	
	/**
	 * <p>Returns an instance of the {@link DefaultContextsManager}.</p>
	 */
	public static ContextsManager getInstance() {
		return INSTANCE;
	}
}
