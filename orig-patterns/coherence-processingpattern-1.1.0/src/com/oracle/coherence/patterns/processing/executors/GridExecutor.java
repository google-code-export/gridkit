/*
 * File: GridExecutor.java
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
import com.oracle.coherence.patterns.processing.dispatchers.PendingSubmission;
import com.oracle.coherence.patterns.processing.friends.ProcessingFrameworkFactory;
import com.oracle.coherence.patterns.processing.friends.SubmissionManager;
import com.oracle.coherence.patterns.processing.friends.SubmissionResultManager;
import com.oracle.coherence.patterns.processing.task.AbstractExecutor;
import com.oracle.coherence.patterns.processing.task.ExecutorType;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.util.ExternalizableHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
* The {@link GridExecutor} executes
* {@link com.oracle.coherence.patterns.processing.task.ResumableTask}s on the
* node where the
* {@link com.oracle.coherence.patterns.processing.internal.Submission}
* primary copy gets stored.
* 
* @author Christer Fahlgren 2009.09.30
*/

@SuppressWarnings("serial")
public class GridExecutor
        extends AbstractExecutor
    {
    // ----- constructors ---------------------------------------
   
    /**
    * The default Constructor.
    */
    public GridExecutor()
        {
        }

    /**
    * Constructor taking parameters.
    * 
    * @param oExecutorIdentifier the unique {@link Identifier} for the
    *            {@link com.oracle.coherence.patterns.processing.task.Executor}
    * @param sName the display name of the executor
    * @param sTaskType the task type this Executor is handling
    * @param oSubmissionManager the {@link SubmissionManager} which the
    *            Executor uses.
    * @param oSubmissionResultManager the {@link SubmissionResultManager}
    *            which the Executor uses
    * @param nNumberOfThreads the number of threads that this executor uses
    *            to execute jobs on a particular node
    */
    public GridExecutor(final Identifier oExecutorIdentifier,
                        final String sName,
                        final String sTaskType,
                        final SubmissionManager oSubmissionManager,
                        final SubmissionResultManager oSubmissionResultManager,
                        final int nNumberOfThreads)
        {
        super(oExecutorIdentifier, sName, sTaskType, ExecutorType.GRID);
        initialize(oSubmissionManager, oSubmissionResultManager,
                   nNumberOfThreads, null);
        }

    // ----- Executor interface ---------------------------------------
    
    /**
    * {@inheritDoc}
    */
    public void executeTask(final PendingSubmission oPendingSubmission)
        {
        m_oExecutorService.execute(new TaskRunner(
            oPendingSubmission.getSubmissionKey(), getExecutorKey(),
            getExecutorType(), m_oSubmissionManager,
            m_oSubmissionResultManager,
            m_oCCFactory));

        }

    /**
    * {@inheritDoc}
    */
    public void onStartup(ConfigurableCacheFactory oCCFactory)
        {
        initialize(ProcessingFrameworkFactory.getInstance()
            .getSubmissionManager(), ProcessingFrameworkFactory.getInstance()
            .getSubmissionResultManager(), m_nNumberOfThreads, oCCFactory);

        }

    /**
     * {@inheritDoc}
     */
     public void onShutdown()
         {
         if (m_oExecutorService != null) 
             {
             m_oExecutorService.shutdown();
             }
         }

    // ----- ExternalizableLite interface -----------------------------------
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void readExternal(final DataInput in) throws IOException
        {
        super.readExternal(in);
        m_nNumberOfThreads = ExternalizableHelper.readInt(in);
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void writeExternal(final DataOutput out) throws IOException
        {
        super.writeExternal(out);
        ExternalizableHelper.writeInt(out, m_nNumberOfThreads);
        }

    // ----- PortableObject interface ---------------------------------------
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void readExternal(final PofReader oReader) throws IOException
        {
        super.readExternal(oReader);
        m_nNumberOfThreads = oReader.readInt(10);
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        super.writeExternal(oWriter);
        oWriter.writeInt(10, m_nNumberOfThreads);
        }

    // ----- GridExecutor methods -------------------------------------------
    
    /**
    * Initialize the GridExecutor.
    * 
    * @param oSubmissionManager         the {@link SubmissionManager} to use
    * @param oSubmissionResultManager   the {@link SubmissionResultManager} to
    *                                   use
    * @param nNumberOfThreads           the number of threads to use when 
    *                                   executing on a node
    * @param oCCFactory                 the {@link ConfigurableCacheFactory} to use                                  
    */
    private void initialize(final SubmissionManager oSubmissionManager,
        final SubmissionResultManager oSubmissionResultManager,
        final int nNumberOfThreads,
        ConfigurableCacheFactory oCCFactory)
        {
        m_oCCFactory                = oCCFactory;
        m_oSubmissionManager        = oSubmissionManager;
        m_oSubmissionResultManager  = oSubmissionResultManager;
        m_nNumberOfThreads          = nNumberOfThreads;
        m_oExecutorService          = Executors
                .newFixedThreadPool(m_nNumberOfThreads, ThreadFactories
                        .newThreadFactory(true, "GridExecutor", null));
        }
    
    // ----- Members ---------------------------------------

    /**
    * The number of threads to use when executing.
    */
    private int                               m_nNumberOfThreads;

    /**
    * The {@link ExecutorService} used to execute the tasks.
    */
    private transient ExecutorService         m_oExecutorService;

    /**
    * The {@link SubmissionManager} to use.
    */
    private transient SubmissionManager       m_oSubmissionManager;

    /**
    * The {@link SubmissionResultManager} to use.
    */
    private transient SubmissionResultManager m_oSubmissionResultManager;
    
    /**
    * The {@link ConfigurableCacheFactory} to use. 
    */
    private ConfigurableCacheFactory m_oCCFactory;

    }
