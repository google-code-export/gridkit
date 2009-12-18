/*
 * File: LocalExecutorDispatcher.java
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

package com.oracle.coherence.patterns.processing.dispatchers.local;

import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.common.threading.ThreadFactories;
import com.oracle.coherence.patterns.processing.dispatchers.AbstractDispatcher;
import com.oracle.coherence.patterns.processing.dispatchers.DispatchController;
import com.oracle.coherence.patterns.processing.dispatchers.DispatchOutcome;
import com.oracle.coherence.patterns.processing.dispatchers.PendingSubmission;
import com.oracle.coherence.patterns.processing.friends.ProcessingFrameworkFactory;
import com.oracle.coherence.patterns.processing.friends.SubmissionManager;
import com.oracle.coherence.patterns.processing.friends.SubmissionResultManager;
import com.oracle.coherence.patterns.processing.internal.Submission;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.net.CacheFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
* <p>
* A {@link LocalExecutorDispatcher} is a
* {@link com.oracle.coherence.patterns.processing.dispatchers.Dispatcher}
* that will dispatch standard Java {@link Runnable}s and {@link Callable}s to
* be executed locally (in the JVM that is hosting
* {@link LocalExecutorDispatcher}) by an {@link ExecutorService}.
* </p>
* 
* @author Brian Oliver 2009.04.30
* @author Christer Fahlgren 2009.09.30
*/
@SuppressWarnings("serial")
public class LocalExecutorDispatcher
        extends AbstractDispatcher
    {
    // ----- constructors ---------------------------------------------------

    /**
    * <p>
    * Standard Constructor.
    * </p>
    * <p>
    * NOTE: Also required to support
    * {@link com.tangosol.io.ExternalizableLite} and
    * {@link com.tangosol.io.pof.PortableObject}.
    * </p>
    */
    public LocalExecutorDispatcher()
        {
        }

    /**
    * <p>
    * Constructor that includes the
    * {@link com.oracle.coherence.patterns.processing.dispatchers.Dispatcher}
    * name.
    * </p>
    * 
    * @param sName the name of this
    *            {@link com.oracle.coherence.patterns.processing.dispatchers.logging.LoggingDispatcher}
    *            .
    * @param oSubmissionResultManager the manager of submission results
    * @param oSubmissionManager the manager of submissions
    */
    public LocalExecutorDispatcher(final String sName,
            final SubmissionResultManager oSubmissionResultManager,
            final SubmissionManager oSubmissionManager)
        {
        super(sName);
        m_oSubmissionResultManager  = oSubmissionResultManager;
        m_oSubmissionManager        = oSubmissionManager;
        }

    // ----- Dispatcher interface -------------------------------------------

    /**
    * {@inheritDoc}
    */
    public DispatchOutcome dispatch(final PendingSubmission oPendingProcess)
        {
        if (oPendingProcess.getPayload() instanceof Runnable
            || oPendingProcess.getPayload() instanceof Callable)
            {

            // submit the pending process to the local executor to execute
            m_oExecutorService.execute(new ProcessRunner(
                oPendingProcess, m_oSubmissionResultManager,
                m_oSubmissionManager));
            return DispatchOutcome.ACCEPTED;

            }
        else
            {
            // we can't handle this type of pending process
            return DispatchOutcome.REJECTED;
            }
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void onShutdown(final DispatchController dispatchController)
        {
        if (m_oExecutorService != null)
            {
            m_oExecutorService.shutdown();
            }
        super.onShutdown(dispatchController);
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void onStartup(final DispatchController dispatchController)
        {
        super.onStartup(dispatchController);
        this.m_oExecutorService         = Executors
            .newSingleThreadExecutor(ThreadFactories
                .newThreadFactory(true, "LocalExecutorDispatcher", null));
        this.m_oSubmissionManager       = ProcessingFrameworkFactory.getInstance()
            .getSubmissionManager();
        this.m_oSubmissionResultManager = ProcessingFrameworkFactory
            .getInstance().getSubmissionResultManager();
        }

    // ----- ExternalizableLite interface  ----------------------------------
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void readExternal(final DataInput in) throws IOException
        {
        super.readExternal(in);
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void writeExternal(final DataOutput out) throws IOException
        {
        super.writeExternal(out);
        }

    // ----- PortableObject interface ---------------------------------------

    /**
    * {@inheritDoc}
    */
     @Override
     public void readExternal(final PofReader oReader) throws IOException
         {
         super.readExternal(oReader);
         }

    /**
    * {@inheritDoc}
    */
    @Override
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        super.writeExternal(oWriter);
        }

    /**
    * <p>
    * A {@link ProcessRunner} is responsible for claiming ownership of,
    * running and returning the result of an executed
    * {@link PendingSubmission}.
    * </p>
    */
    public static class ProcessRunner
            implements Runnable
        {
        // ----- constructors -----------------------------------------------
        
        /**
        * <p>
        * Constructor taking the {@link PendingSubmission}, the {@link SubmissionManager} and
        * the {@link SubmissionResultManager}.
        * </p>
        * 
        * @param oPendingSubmission       the submission to run
        * @param oSubmissionResultManager the {@link SubmissionResultManager}
        * @param oSubmissionManager       the {@link SubmissionManager} for this submission
        */
        public ProcessRunner(final PendingSubmission oPendingSubmission,
                final SubmissionResultManager oSubmissionResultManager,
                final SubmissionManager oSubmissionManager)
            {
            this.m_oSubmissionResultManager = oSubmissionResultManager;
            this.m_oSubmissionManager       = oSubmissionManager;
            this.m_oSubmission          = oPendingSubmission;
            }

        // ----- Runnable interface -----------------------------------------

        /**
        * {@inheritDoc}
        */
        @SuppressWarnings("unchecked")
        public void run()
            {
            // take ownership of the pending {@Submission} 
            //
            final Submission oSubmission = m_oSubmissionManager.getSubmission(m_oSubmission.getSubmissionKey());
            m_oSubmissionManager.ownSubmission(m_oSubmission.getSubmissionKey(), 
                                               CacheFactory.getCluster().getLocalMember().getId());
            m_oSubmissionResultManager.startProcessing(oSubmission.getResultUUID());

            // attempt to execute the payload.
            // this ProcessRunner only runs Runnable or Callables
            //
            try
                {
                Object oResult = null;
                if (oSubmission.getPayload() instanceof Runnable)
                    {
                    ((Runnable) oSubmission.getPayload()).run();
                    oResult = null;
                    }
                else
                    {
                    if (oSubmission.getPayload() instanceof Callable)
                        {
                        oResult = ((Callable) oSubmission.getPayload()).call();
                        }
                    else
                        {
                        throw new UnsupportedOperationException(
                            String
                                .format(
                                        "Can't execute %s as it's neither Runnable or Callable",
                                        m_oSubmission));
                        }
                    }

                m_oSubmissionResultManager.processingSucceeded(oSubmission.getResultUUID(), oResult);
                Logger.log(Logger.INFO, "Executed %s to produce %s",
                           m_oSubmission, oResult);
                }
            catch (final Exception oException)
                {
                if (Logger.isEnabled(Logger.WARN))
                    {
                    Logger.log(Logger.WARN,
                               "Failed to process %s due to:\n%s",
                               m_oSubmission, oException);
                    }
                m_oSubmissionResultManager.processingFailed(oSubmission
                    .getResultUUID(), oException);
                }

            // remove the pending process as it's now completed
            m_oSubmissionManager.removeSubmission(m_oSubmission.getSubmissionKey());
            }

        // ----- Members ----------------------------------------------------

        /**
        * <p>
        * The {@link PendingSubmission} that needs to be executed.
        * </p>
        */
        private final PendingSubmission       m_oSubmission;

        /**
        * The {@link SubmissionManager} is used to manage the submitted items
        * for execution.
        */
        private final SubmissionManager       m_oSubmissionManager;

        /**
        * The {@link SubmissionResultManager} is used to manage results of
        * execution as well as the state of the execution.
        */
        private final SubmissionResultManager m_oSubmissionResultManager;
        }

    // ----- Members --------------------------------------------------------

    /**
    * <p>
    * The {@link ExecutorService} that will manage and perform the execution
    * of {@link PendingSubmission}s dispatched by this
    * {@link com.oracle.coherence.patterns.processing.dispatchers.Dispatcher}
    * .
    * </p>
    */
    private transient ExecutorService         m_oExecutorService;

    /**
    * The {@link SubmissionManager} is used to manage the submitted items for
    * execution.
    */

    private transient SubmissionManager       m_oSubmissionManager;

    /**
    * The {@link SubmissionResultManager} is used to manage results of
    * execution as well as the state of the execution.
    */
    private transient SubmissionResultManager m_oSubmissionResultManager;
    }
