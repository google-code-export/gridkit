/*
 * File: DefaultSubmissionResultManager.java
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

import com.oracle.coherence.patterns.processing.friends.SubmissionResultManager;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.util.UUID;
import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.extractor.ReflectionUpdater;
import com.tangosol.util.processor.ExtractorProcessor;
import com.tangosol.util.processor.UpdaterProcessor;

/**
* The {@link DefaultSubmissionResultManager} is an implementation of the
* {@link SubmissionResultManager} using a NamedCache to store 
* {@link SubmissionResult}s.
* 
* @author Christer Fahlgren 2009.09.30
*/
public class DefaultSubmissionResultManager
        implements SubmissionResultManager
    {
    // ----- constructors ---------------------------------------------------
 
    /**
    * Constructor.
    * 
    * @param oCCFactory the {@link ConfigurableCacheFactory} to use.
    */
    public DefaultSubmissionResultManager(ConfigurableCacheFactory oCCFactory)
        {
        m_oCCFactory = oCCFactory;
        }

    // ----- Subsystem methods ----------------------------------------------
    
    /**
    * {@inheritDoc}
    */
    public void startup()
        {
        m_oSubmissionResults = m_oCCFactory.ensureCache(SubmissionResult.CACHENAME, null);
    	}
    
    /**
    * {@inheritDoc}
    */
    public void shutdown()
        {
        m_oSubmissionResults = null;
        }
   
    // ----- SubmissionResultManager methods --------------------------------
    
    /**
    * {@inheritDoc}
    */
    public Object getProgress(final UUID oResultUUID)
        {
        final Object result = m_oSubmissionResults
                .invoke(oResultUUID, new ExtractorProcessor(
                        new ReflectionExtractor("getProgress")));
        return result;
        }

    /**
    * {@inheritDoc}
    */
    public SubmissionResult getSubmissionResult(final UUID oResultUUID)
        {
        return (SubmissionResult) m_oSubmissionResults
            .invoke(oResultUUID, new ExtractorProcessor(
                IdentityExtractor.INSTANCE));
        }

    /**
    * {@inheritDoc}
    */
    public Object loadCheckpoint(final UUID oResultUUID)
        {
        return m_oSubmissionResults.invoke(oResultUUID, 
                new ExtractorProcessor(new ReflectionExtractor("getResult")));
        }

    /**
    * {@inheritDoc}
    */
    public void processingFailed(final UUID oResultUUID,
            final Exception oException)
        {
        m_oSubmissionResults.invoke(oResultUUID, new UpdaterProcessor(
                new ReflectionUpdater("processingFailed"), oException));

        }

    /**
    * {@inheritDoc}
    */
    public void processingSucceeded(final UUID oResultUUID, 
            final Object oResult)
        {
        m_oSubmissionResults.invoke(oResultUUID, new UpdaterProcessor(
                new ReflectionUpdater("processingSucceeded"), oResult));
        }

    /**
    * {@inheritDoc}
    */
    public void reportProgress(final UUID oResultUUID, final Object oProgress)
        {
        m_oSubmissionResults.invoke(oResultUUID, new UpdaterProcessor(
                 new ReflectionUpdater("setProgress"), oProgress));
        }

    /**
    * {@inheritDoc}
    */
    public void startProcessing(final UUID oResultUUID)
        {
        m_oSubmissionResults.invoke(oResultUUID, new UpdaterProcessor(
                 new ReflectionUpdater("processingStarted"), null));
        }

    /**
    * {@inheritDoc}
    */
    public void storeCheckpoint(final UUID oResultUUID,
            final Object intermediateState)
        {
        m_oSubmissionResults.invoke(oResultUUID, new UpdaterProcessor(
                new ReflectionUpdater("setResult"), intermediateState));

        }

    /**
    * {@inheritDoc}
    */
    public void yield(final UUID oResultUUID, final Object oIntermediateState)
        {
        m_oSubmissionResults.invoke(oResultUUID, new UpdaterProcessor(
                new ReflectionUpdater("suspendExecution"), oIntermediateState));

        }

    // ----- Members --------------------------------------------------------

    /**
    * Reference to the NamedCache for the Submissionresults.
    */
    private NamedCache m_oSubmissionResults;
    
    /**
    * The {@link ConfigurableCacheFactory} for this {@link SubmissionResultManager}.
    */  
    private ConfigurableCacheFactory m_oCCFactory;
    }
