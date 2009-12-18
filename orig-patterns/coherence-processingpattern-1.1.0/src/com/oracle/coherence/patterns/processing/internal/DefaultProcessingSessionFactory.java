/*
 * File: DefaultProcessingSessionFactory.java
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

import com.oracle.coherence.common.identifiers.Identifier;

import com.oracle.coherence.patterns.processing.ProcessingSession;
import com.oracle.coherence.patterns.processing.ProcessingSessionFactory;

import com.oracle.coherence.patterns.processing.friends.ProcessingFrameworkFactory;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;

/**
* The implementation of the ProcessingPatternFactory.
* 
* @author Christer Fahlgren 2009.09.30
*/
public class DefaultProcessingSessionFactory
        extends ProcessingSessionFactory
    {

    // ----- ProcessingSessionFactory Methods -------------------------------
    
    /**
    * {@inheritDoc}
    */
    @Override
    public ProcessingSession getSession(Identifier oIdentifier)
        {
        checkInvariants();
        return new DefaultProcessingSession(oIdentifier);
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void init(ConfigurableCacheFactory oCCFactory)
        {
        try
            {
            m_oProcessingFrameworkFactory = ProcessingFrameworkFactory.getInstance();
            m_oProcessingFrameworkFactory.init(oCCFactory);
            }
        catch (final Exception oException)
            {
                throw new IllegalStateException(oException);
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
         ProcessingFrameworkFactory.getInstance().shutdown();
         }
     
     /**
     * Checking if initialized, if not throw IllegalStateException.
     */
     public void checkInvariants()
         {
         if (m_oProcessingFrameworkFactory == null) throw new IllegalStateException();
         }
     
     
     // ----- Members -------------------------------------------------------
     
     /**
     * Holding the {@link ProcessingFrameworkFactory} to use.
     */
     ProcessingFrameworkFactory m_oProcessingFrameworkFactory;
    }
