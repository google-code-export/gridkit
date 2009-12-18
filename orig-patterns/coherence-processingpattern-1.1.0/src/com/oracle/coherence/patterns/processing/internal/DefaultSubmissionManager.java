/*
 * File: DefaultSubmissionManager.java
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

import com.oracle.coherence.patterns.processing.friends.SubmissionManager;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.extractor.ReflectionUpdater;
import com.tangosol.util.processor.ExtractorProcessor;
import com.tangosol.util.processor.UpdaterProcessor;

/**
* This is the implementation of the {@link SubmissionManager} that uses a
* NamedCache as the backing store of Submissions.
* 
* @author Christer Fahlgren 2009.09.30
*/
public class DefaultSubmissionManager
        implements SubmissionManager
    {
    // ----- constructors ---------------------------------------------------

    /**
     * Default constructor.
     * 
     * @param oCCFactory the {@link ConfigurableCacheFactory} to use.
     */
    public DefaultSubmissionManager(ConfigurableCacheFactory oCCFactory)
        {
        m_oCCFactory = oCCFactory;
        }

    
    // ----- Subsystem methods ----------------------------------------------
    
    /**
    * {@inheritDoc}
    */
    public void startup()
    {
        m_oSubmissions = m_oCCFactory.ensureCache(Submission.CACHENAME, null);
    }
    
    /**
    * {@inheritDoc}
    */
    public void shutdown()
    {
        m_oSubmissions = null;
    }
   
     
    // ----- SubmissionManager methods --------------------------------------

    /**
    * {@inheritDoc}
    */
    public Submission getSubmission(final SubmissionKey oKey)
        {
        return (Submission) m_oSubmissions
            .invoke(oKey, new ExtractorProcessor(IdentityExtractor.INSTANCE));
        }

    /**
    * {@inheritDoc}
    */
    public void removeSubmission(final SubmissionKey oSubmissionKey)
        {
        m_oSubmissions.remove(oSubmissionKey);
        }

    /**
    * {@inheritDoc}
    */
    public boolean ownSubmission(final SubmissionKey oKey,
        final Object oOwner)
        {
        final Object oResult = m_oSubmissions.invoke(oKey, 
                new UpdaterProcessor(new ReflectionUpdater("tryToOwn"), oOwner));
        return (oResult != null);
        }

    // ----- Members --------------------------------------------------------

    /**
    * The Submissions NamedCache.
    */
    private NamedCache m_oSubmissions;
    
    /**
    * The {@link ConfigurableCacheFactory} for this {@link SubmissionManager}.
    */  
    private ConfigurableCacheFactory m_oCCFactory;

    }
