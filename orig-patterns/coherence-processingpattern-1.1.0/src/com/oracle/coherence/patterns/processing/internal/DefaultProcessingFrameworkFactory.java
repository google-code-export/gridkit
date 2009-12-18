/*
 * File: DefaultProcessingFrameworkFactory.java
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

import com.oracle.coherence.patterns.processing.dispatchers.DispatchController;

import com.oracle.coherence.patterns.processing.friends.DispatcherManager;
import com.oracle.coherence.patterns.processing.friends.ProcessingFrameworkFactory;
import com.oracle.coherence.patterns.processing.friends.SubmissionManager;
import com.oracle.coherence.patterns.processing.friends.SubmissionResultManager;

import com.oracle.coherence.patterns.processing.internal.task.DefaultExecutorManager;

import com.oracle.coherence.patterns.processing.task.ExecutorManager;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;

/**
* The default implementation of the {@link ProcessingFrameworkFactory}.
* 
* @author Christer Fahlgren 2009.09.30
* 
*/
public class DefaultProcessingFrameworkFactory
        extends ProcessingFrameworkFactory
    {
    // ----- DefaultProcessingFrameworkFactory methods ----------------------

    /**
    * {@inheritDoc}
    */
    @Override
    public DispatchController getDispatchController()
        {
        return m_oDispatchController;
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public DispatcherManager getDispatcherManager()
        {
        return m_oDispatcherManager;
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public ExecutorManager getExecutorManager()
        {
        return m_oExecutionManager;
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public SubmissionManager getSubmissionManager()
        {
        return m_oSubmissionManager;
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public SubmissionResultManager getSubmissionResultManager()
        {
        return m_oSubmissionResultManager;
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void init(ConfigurableCacheFactory oCCFactory)
        {

        if (m_oSubmissionManager == null)
            {
            try
                {
              
                m_oSubmissionManager        = new DefaultSubmissionManager(oCCFactory);
                m_oSubmissionResultManager  = new DefaultSubmissionResultManager(oCCFactory);
                m_oDispatcherManager        = new DefaultDispatcherManager(oCCFactory);
                m_oDispatchController       = new DefaultDispatchController(oCCFactory);
                m_oExecutionManager         = new DefaultExecutorManager(oCCFactory);
                
            	m_oSubmissionManager.startup();
            	m_oSubmissionResultManager.startup();
            	m_oDispatcherManager.startup();
                m_oDispatchController.startup();
                m_oExecutionManager.startup();

                }
            catch (final Exception oException)
                {
                throw new IllegalStateException(oException);
                }
            }
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void init()
        {
        init(CacheFactory.getConfigurableCacheFactory());
        }
     
    /**
    * {@inheritDoc}
    */
    @Override
    public void shutdown()
        {
    	m_oSubmissionManager.shutdown();
    	m_oSubmissionResultManager.shutdown();
    	m_oDispatcherManager.shutdown();
        m_oDispatchController.shutdown();
        m_oExecutionManager.shutdown();
        
        m_oSubmissionManager 		= null;
        m_oSubmissionResultManager 	= null;
        m_oDispatcherManager 		= null;
        m_oDispatchController 		= null;
        m_oExecutionManager 		= null;
        }

    // ----- Members ---------------------------------------------------------

    /**
    * The {@link DispatchController} for this factory.
    */
    private DispatchController      m_oDispatchController;

    /**
    * The {@link DispatcherManager} for this factory.
    */
    private DispatcherManager       m_oDispatcherManager;

    /**
    * The {@link ExecutorManager} for this factory.
    */
    private ExecutorManager         m_oExecutionManager;

    /**
    * The {@link SubmissionManager} for this factory.
    */
    private SubmissionManager       m_oSubmissionManager;

    /**
    * The {@link SubmissionResultManager} for this factory.
    */
    private SubmissionResultManager m_oSubmissionResultManager;
    }
