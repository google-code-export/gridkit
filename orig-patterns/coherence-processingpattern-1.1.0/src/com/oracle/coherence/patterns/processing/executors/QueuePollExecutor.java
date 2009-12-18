/*
 * File: QueuePollExecutor.java
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

package com.oracle.coherence.patterns.processing.executors;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.common.threading.ThreadFactories;
import com.oracle.coherence.patterns.processing.friends.SubmissionManager;
import com.oracle.coherence.patterns.processing.friends.SubmissionResultManager;
import com.oracle.coherence.patterns.processing.internal.SubmissionKey;
import com.oracle.coherence.patterns.processing.internal.processors.DequeueTaskProcessor;
import com.oracle.coherence.patterns.processing.internal.processors.DrainToBeExecutedProcessor;
import com.oracle.coherence.patterns.processing.internal.task.DefaultExecutorManager;
import com.oracle.coherence.patterns.processing.task.ExecutorType;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MultiplexingMapListener;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
* The {@link QueuePollExecutor} listens on a particular 
* {@link com.oracle.coherence.patterns.processing.internal.task.ExecutorQueue}
* belonging to a queue based
* {@link com.oracle.coherence.patterns.processing.task.Executor}. When it
* signals, it will pull a task from the queue and start executing it.
* 
* @author Christer Fahlgren 2009.09.30
*/
public class QueuePollExecutor
    {
    
	
	// ----- constructors ---------------------------------------------------

    /**
    * Constructor for MapEventExecutor.
    * 
    * @param oExecutorIdentifier        the {@link Identifier} for the Executor
    * @param oSubmissionManager         the {@link SubmissionManager} to use
    * @param oSubmissionResultManager   the {@link SubmissionResultManager} to use
    * @param nThreadCount				the number of threads to use
    * @param oCCFactory                 the {@link ConfigurableCacheFactory} to use
    */
    public QueuePollExecutor(
                            final Identifier oExecutorIdentifier,
                            final SubmissionManager oSubmissionManager,
                            final SubmissionResultManager oSubmissionResultManager,
                            int   nThreadCount,
                            ConfigurableCacheFactory oCCFactory)
        {
        m_oExecutorKey              = oExecutorIdentifier;
        m_oSubmissionManager        = oSubmissionManager;
        m_oSubmissionResultManager  = oSubmissionResultManager;
        m_oCCFactory                = oCCFactory;
        initialize(nThreadCount);
        }

    // ----- QueuePollExecutor methods --------------------------------------
    
    /**
    * Dequeues a task and submits it for execution.
    */
    private void dequeueTaskLocally()
        {
        final SubmissionKey key = (SubmissionKey) m_oExecutorQueueCache.invoke(m_oExecutorKey, new DequeueTaskProcessor());
        if (key != null)
            {
            m_oExecutorService.execute(new TaskRunner(
                key, m_oExecutorKey, ExecutorType.QUEUEBASED,
                m_oSubmissionManager, m_oSubmissionResultManager, m_oCCFactory));
            }

        }

    /**
    * Drains the queue and submits all Submissions for execution.
    */
    private void drainQueue()
        {
        final LinkedList<SubmissionKey> toBeExecuted = (LinkedList<SubmissionKey>) m_oExecutorQueueCache
            .invoke(m_oExecutorKey, new DrainToBeExecutedProcessor());
        for (final SubmissionKey key : toBeExecuted)
            {
            m_oExecutorService.execute(new TaskRunner(
                key, m_oExecutorKey, ExecutorType.QUEUEBASED,
                m_oSubmissionManager, m_oSubmissionResultManager, m_oCCFactory));
            }
        }

    /**
    * Initializes the {@link QueuePollExecutor}.
    * 
    * @param nThreadCount   the number of threads to use
    */
    private void initialize(int nThreadCount)
        {
        m_oExecutorQueueCache = CacheFactory.getCache(DefaultExecutorManager.s_sQUEUECACHENAME);

        m_oExecutorService = Executors.newFixedThreadPool(nThreadCount, ThreadFactories
                 .newThreadFactory(true, "MapEventExecutor", null));

        m_oExecutorQueueCache.addMapListener(new MultiplexingMapListener()
            {
                @Override
                protected void onMapEvent(final MapEvent mapEvent)
                    {
                    if (mapEvent.getId() == MapEvent.ENTRY_INSERTED)
                        {
                        dequeueTaskLocally();
                        }
                    else
                        {
                        if (mapEvent.getId() == MapEvent.ENTRY_UPDATED)
                            {
                            dequeueTaskLocally();
                            }
                        }

                    }
            }, m_oExecutorKey, true);
        }

    // ----- Members --------------------------------------------------------
    
    /**
    * The {@link Identifier} of the {@link QueueBasedExecutor} this MapEventExecutor executes
    * for.
    */
    private transient Identifier              m_oExecutorKey;

    /**
    * The
    * {@link com.oracle.coherence.patterns.processing.internal.task.ExecutorQueue}
    * cache.
    */
    private transient NamedCache              m_oExecutorQueueCache;

    /**
    * The {@link ExecutorService}.
    */
    private transient ExecutorService         m_oExecutorService;

    /**
    * The {@link SubmissionManager} in use.
    */
    private transient SubmissionManager       m_oSubmissionManager;

    /**
    * The {@link SubmissionResultManager} in use.
    */
    private transient SubmissionResultManager m_oSubmissionResultManager;
    
    /**
    * The {@link ConfigurableCacheFactory} to use. 
    */
    private ConfigurableCacheFactory m_oCCFactory;

    }
