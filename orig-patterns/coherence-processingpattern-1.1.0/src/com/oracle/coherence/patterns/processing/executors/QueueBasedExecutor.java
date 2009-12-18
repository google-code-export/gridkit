/*
 * File: QueueBasedExecutor.java
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

import com.oracle.coherence.patterns.processing.dispatchers.PendingSubmission;
import com.oracle.coherence.patterns.processing.internal.processors.EnqueueTaskProcessor;
import com.oracle.coherence.patterns.processing.internal.task.DefaultExecutorManager;
import com.oracle.coherence.patterns.processing.task.AbstractExecutor;
import com.oracle.coherence.patterns.processing.task.ExecutorType;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import java.io.Serializable;

/**
* The {@link QueueBasedExecutor} simply enqueues the tasks on an
* {@link com.oracle.coherence.patterns.processing.internal.task.ExecutorQueue}
* so that another object, who listens to MapEvents for the queue, gets
* notified when there is a new task to be executed.
* 
* @author Christer Fahlgren 2009.09.30
*/

@SuppressWarnings("serial")
public class QueueBasedExecutor
        extends AbstractExecutor
        implements Serializable
    {
    // ----- constructors ---------------------------------------------------

    /**
    * Default Constructor.
    */
    public QueueBasedExecutor()
        {
        }

    /**
    * Constructor taking parameters.
    * 
    * @param oExecutorIdentifier    the unique {@link Identifier} for this
    *                               {@link QueueBasedExecutor}
    * @param sDisplayName           the Display Name for this
    *                               {@link QueueBasedExecutor}
    * @param sTaskType              the task type this executor can handle
    */
    public QueueBasedExecutor(final Identifier oExecutorIdentifier,
                              final String sDisplayName,
                              final String sTaskType)
        {
        super(oExecutorIdentifier, sDisplayName, sTaskType, ExecutorType.QUEUEBASED);
        }

    // ----- Executor methods -----------------------------------------------

    /**
    * {@inheritDoc}
    */
    public void executeTask(final PendingSubmission oPendingSubmission)
        {
        m_oExecutorQueueCache.invoke(getExecutorKey(),
                                     new EnqueueTaskProcessor(
                                         oPendingSubmission
                                             .getSubmissionKey()));
        }

    /**
    * {@inheritDoc}
    */
    public void onShutdown()
        {
        }

    /**
    * {@inheritDoc}
    */
    public void onStartup(ConfigurableCacheFactory oCCFactory)
        {
        m_oCCFactory = oCCFactory;
        initialize();
        }

    // ----- QueueBasedExecutor methods ---------------------------------------

    /**
    * Initialize the {@link QueueBasedExecutor}.
    */
    private void initialize()
        {
        m_oExecutorQueueCache = m_oCCFactory.ensureCache(DefaultExecutorManager.s_sQUEUECACHENAME, null);
        }
    
    // ----- Members ---------------------------------------

    /**
    * The non-serializable reference to the {@link ExecutorQueue} Cache.
    */
    private transient NamedCache m_oExecutorQueueCache;

    /**
    * The {@link ConfigurableCacheFactory} to use. 
    */
    private ConfigurableCacheFactory m_oCCFactory;

    }
