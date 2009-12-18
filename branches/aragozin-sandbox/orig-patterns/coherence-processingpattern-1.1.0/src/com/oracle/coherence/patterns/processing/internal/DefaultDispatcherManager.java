/*
 * File: DefaultDispatcherManager.java
 * 
 * Copyright (c) 2009. All Rights Reserved. Oracle Corporation.
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

package com.oracle.coherence.patterns.processing.internal;

import com.oracle.coherence.patterns.processing.dispatchers.Dispatcher;
import com.oracle.coherence.patterns.processing.friends.DispatcherManager;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.util.Filter;
import com.tangosol.util.filter.NotFilter;
import com.tangosol.util.filter.PresentFilter;
import com.tangosol.util.processor.ConditionalPut;
import com.tangosol.util.processor.ConditionalRemove;

/**
* Default implementation of the {@link DispatcherManager} interface.
* 
* @author Noah Arliss 2009.04.30
*/
public class DefaultDispatcherManager
        implements DispatcherManager
    {
    // ----- constructors ---------------------------------------------------
 
    /**
    * Standard constructor.
    *  
    * @param oCCFactory the ConfigurableCacheFactory to use.
    */
    public DefaultDispatcherManager(ConfigurableCacheFactory oCCFactory)
        {
        m_oCCFactory = oCCFactory;
        }
    
    // ----- Subsystem methods ----------------------------------------------
    
    /**
    * {@inheritDoc}
    */
    public void startup()
        {
        m_oDispatcherCache  = m_oCCFactory.ensureCache(CACHENAME, null);       
        }
    
    /**
    * {@inheritDoc}
    */
    public void shutdown()
        {
        m_oDispatcherCache = null;
        }
   
  
    // ----- DispatcherManager methods --------------------------------------
    
    /**
    * {@inheritDoc}
    */
    public void registerDispatcher(final Dispatcher dispatcher)
        {
        Filter filter           = new NotFilter(PresentFilter.INSTANCE);
        m_oDispatcherCache.invoke(dispatcher.getName(), new ConditionalPut(filter, dispatcher));
        }

    /**
    * {@inheritDoc}
    */
    public void unregisterDispatcher(final Dispatcher dispatcher)
        {
        m_oDispatcherCache.invoke(dispatcher.getName(),         
                     new ConditionalRemove(PresentFilter.INSTANCE));
        }

    // ----- Static members -------------------------------------------------

    /**
    * <p>
    * The name of the {@link DispatcherManager} cache.
    * </p>
    */
    public static final String CACHENAME = "coherence.patterns.processing.dispatchers";
    
    /**
    * Dispatchers cache.
    */
    private NamedCache m_oDispatcherCache;   
    
    /**
    * The {@link ConfigurableCacheFactory} for this {@link DispatcherManager}.
    */
    private ConfigurableCacheFactory m_oCCFactory;

    }
