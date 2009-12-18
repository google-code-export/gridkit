/*
 * File: DefaultExecutorManager.java
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

package com.oracle.coherence.patterns.processing.internal.task;

import com.oracle.coherence.patterns.processing.internal.task.LocalExecutorList.ExecutorAlreadyExistsException;
import com.oracle.coherence.patterns.processing.task.Executor;
import com.oracle.coherence.patterns.processing.task.ExecutorManager;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.util.Filter;
import com.tangosol.util.filter.NotFilter;
import com.tangosol.util.filter.PresentFilter;

import com.tangosol.util.processor.ConditionalPut;
import com.tangosol.util.processor.ConditionalRemove;

/**
* The DefaultExecutorManager registers {@link Executor}s in the cache.
* 
* @author Christer Fahlgren 2009.09.30
* 
*/
public class DefaultExecutorManager
        implements ExecutorManager
    {
    
    // ----- constructors ---------------------------------------------------
     
    /**
    * @param oCCFactory the {@link ConfigurableCacheFactory} to use.
    */
    public DefaultExecutorManager(ConfigurableCacheFactory oCCFactory)
        {
        m_oCCFactory = oCCFactory;
        }
    
    
    // ----- Subsystem methods ----------------------------------------------
    
    /**
    * {@inheritDoc}
    */
    public void startup()
    {
        m_oExecutorCache = m_oCCFactory.ensureCache(s_sCACHENAME, null);
    
    }
    
    /**
    * {@inheritDoc}
    */
    public void shutdown()
    {
        m_oExecutorCache = null;
    }
  
    
    // ----- ExecutorManager Methods ----------------------------------------

    /**
    * {@inheritDoc}
    */
    public void registerExecutor(final Executor oExecutor)
        throws ExecutorAlreadyExistsException
        {
        Filter filter           = new NotFilter(PresentFilter.INSTANCE);
        m_oExecutorCache.invoke(oExecutor.getExecutorKey(),
                     new ConditionalPut(filter, oExecutor));    
        }

    /**
    * {@inheritDoc}
    */
    public void unregisterExecutor(final Executor oExecutor)
        {
        m_oExecutorCache.invoke(oExecutor.getExecutorKey(),
                     new ConditionalRemove(PresentFilter.INSTANCE));
        }
    
    
    // ----- Members --------------------------------------------------------

    /**
    * <p>
    * The name of the ExecutorDefinitionManager cache.
    * </p>
    */
    public static String s_sCACHENAME      = "coherence.patterns.processing.executors";

    /**
    * The name of the executor queues cache.
    */
    public static String s_sQUEUECACHENAME = "coherence.patterns.processing.executorqueues";

    /**
    * Executors named cache. 
    */
    private NamedCache m_oExecutorCache;
    
    /**
    * We need a reference to the ConfigurableCacheFactory.
    */
    private final ConfigurableCacheFactory m_oCCFactory;
    
    }
