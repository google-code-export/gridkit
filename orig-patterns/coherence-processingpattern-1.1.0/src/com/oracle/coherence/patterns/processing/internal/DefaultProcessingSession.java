/*
 * File: DefaultProcessingSession.java
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
import com.oracle.coherence.patterns.processing.SubmissionConfiguration;
import com.oracle.coherence.patterns.processing.SubmissionOutcome;
import com.oracle.coherence.patterns.processing.SubmissionOutcomeListener;
import com.oracle.coherence.patterns.processing.SubmissionState;
import com.oracle.coherence.patterns.processing.internal.processors.CreateSubmissionResultProcessor;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.util.Filter;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MultiplexingMapListener;
import com.tangosol.util.UUID;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.MapEventFilter;
import com.tangosol.util.filter.NotFilter;
import com.tangosol.util.filter.PresentFilter;
import com.tangosol.util.processor.ConditionalPut;

import java.util.concurrent.ConcurrentHashMap;

/**
* <p>
* Default implementation of the {@link ProcessingSession} interface.
* </p>
* 
* @author Noah Arliss 2009.04.30
*/
public class DefaultProcessingSession
        implements ProcessingSession
    {
    // ----- constructors ---------------------------------------------------

    /**
    * <p>
    * Standard Constructor.
    * </p>
    * 
    * @param oIdentifier is the {@link Identifier} for this Session.
    */
    public DefaultProcessingSession(Identifier oIdentifier)
        {
        this.m_oSubmitterId         = oIdentifier;
        this.m_oSubmissionResults   = new ConcurrentHashMap<UUID, DefaultSubmissionOutcome>();
        m_oSubmissionResultsCache   = CacheFactory.getCache(SubmissionResult.CACHENAME);
        m_oSubmissionCache          = CacheFactory.getCache(Submission.CACHENAME);

        m_oSubmissionResultsCache.addMapListener(new MultiplexingMapListener()
                {
                    @Override
                    protected void onMapEvent(final MapEvent mapEvent)
                        {
                        if (mapEvent.getId() == MapEvent.ENTRY_INSERTED
                            || mapEvent.getId() == MapEvent.ENTRY_UPDATED)
                            {

                            // the FunctorResult is delivered to us as the
                            // mapEvent new value
                            final SubmissionResult oResult = (SubmissionResult) mapEvent
                                .getNewValue();

                            // find the FunctorFuture for the FunctorResult
                            // just delivered (using the
                            // functorResultIdentifier)
                            final DefaultSubmissionOutcome submissionOutcome = m_oSubmissionResults
                                .get(mapEvent.getKey());

                            if (oResult.isFinalState())
                                {

                                if (submissionOutcome != null)
                                    {
                                    submissionOutcome
                                        .acceptProcessResult(oResult
                                            .getResult(), oResult
                                            .getSubmissionState(), oResult
                                            .getSubmissionTime(), oResult
                                            .getLatency(), oResult
                                            .getExecutionTime());
                                    }
                                }
                            else
                                {
                                // If not final it could either be suspended
                                // or a
                                // progress update
                                if (oResult.getSubmissionState() == SubmissionState.SUSPENDED)
                                    {
                                    submissionOutcome.onSuspended();
                                    }
                                else
                                    {
                                    if (oResult.getSubmissionState() == SubmissionState.EXECUTING)
                                        {
                                        // We are still executing, thus it
                                        // must be
                                        // a progress result
                                        if (oResult.getProgress() == null)
                                            {
                                            submissionOutcome.onStarted();
                                            }
                                        else
                                            {
                                            submissionOutcome
                                                .onProgress(oResult
                                                    .getProgress());
                                            }
                                        }
                                    }
                                }

                            // remove the processResult and result
                            // iff the result is complete
                            if (submissionOutcome.isDone())
                                {
                                mapEvent.getMap().remove(mapEvent.getKey());
                                m_oSubmissionResults.remove(mapEvent.getKey());
                                }
                            }
                        }
                }, new MapEventFilter(
                MapEventFilter.E_INSERTED | MapEventFilter.E_UPDATED,
                new EqualsFilter("getSubmitterUUID", m_oSubmitterId)), false);
        }

    // ----- ProcessingSession methods --------------------------------------

    /**
    * {@inheritDoc}
    */
    public SubmissionOutcome submit(final Object oPayload,
            final SubmissionConfiguration oRequestData)
        {
        final Submission submission = new Submission(new UUID(), oPayload, 
                oRequestData, new UUID(), m_oSubmitterId);
        final DefaultSubmissionOutcome oResult = new DefaultSubmissionOutcome(
            submission.getResultUUID());
        storeInCache(submission, oResult);
        return oResult;
        }

    /**
    * Stores the Submission in the NamedCache and creates the
    * SubmissionResult in the SubmissionResults cache.
    * 
    * @param submission     the Submission to store
    * @param oResult        the SubmissionOutcome
    */
    private void storeInCache(final Submission submission,
            final DefaultSubmissionOutcome oResult)
        {
        m_oSubmissionResults.put(submission.getResultUUID(), oResult);
        final SubmissionKey oKey = submission.generateKey();

        // Submitting the result in the cache first in order to avoid race
        // between submission and result (as a job is triggered on entering a
        // submission)
        m_oSubmissionResultsCache.invoke(submission.getResultUUID(),
                                         new CreateSubmissionResultProcessor(
                                             oKey, submission
                                                 .getSessionIdentifier()));

        Filter filter = new NotFilter(PresentFilter.INSTANCE);
        m_oSubmissionCache.invoke(oKey,
                                  new ConditionalPut(filter, submission));
        }

    /**
    * {@inheritDoc}
    */
    public SubmissionOutcome submit(final Object oPayload,
            final SubmissionConfiguration oRequestData,
            final SubmissionOutcomeListener oListener)
        {
        final Submission submission 			= new Submission(new UUID(), oPayload,
                oRequestData, new UUID(), m_oSubmitterId);
        final DefaultSubmissionOutcome oResult 	= new DefaultSubmissionOutcome(
                submission.getResultUUID(), oListener);
        storeInCache(submission, oResult);
        return oResult;
        }

    // ----- Members --------------------------------------------------------

    /**
    * The Submission Cache.
    */
    private final NamedCache                                        m_oSubmissionCache;

    /**
    * <p>
    * A map of {@link SubmissionResult}s for the payloads that were submitted
    * by this {@link DefaultProcessingSession} but are yet to be processed.
    * </p>
    */
    private final ConcurrentHashMap<UUID, DefaultSubmissionOutcome> m_oSubmissionResults;

    /**
    * The SubmissionResults Cache.
    */
    private final NamedCache                                        m_oSubmissionResultsCache;

    /**
    * <p>
    * The {@link UUID} for this {@link DefaultProcessingSession} instance.
    * 
    * This is used to register listeners for the results from a
    * {@link Submission}s.
    * </p>
    */
    private final Identifier                               			m_oSubmitterId;
    }
