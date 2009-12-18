/*
 * File: TaskRunner.java
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
import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.patterns.processing.SubmissionConfiguration;
import com.oracle.coherence.patterns.processing.dispatchers.DispatchController;
import com.oracle.coherence.patterns.processing.friends.ProcessingFrameworkFactory;
import com.oracle.coherence.patterns.processing.friends.SubmissionManager;
import com.oracle.coherence.patterns.processing.friends.SubmissionResultManager;
import com.oracle.coherence.patterns.processing.internal.DefaultPendingSubmission;
import com.oracle.coherence.patterns.processing.internal.Submission;
import com.oracle.coherence.patterns.processing.internal.SubmissionKey;
import com.oracle.coherence.patterns.processing.internal.SubmissionResult;
import com.oracle.coherence.patterns.processing.internal.task.DefaultExecutorManager;
import com.oracle.coherence.patterns.processing.task.ExecutorType;
import com.oracle.coherence.patterns.processing.task.ResumableTask;
import com.oracle.coherence.patterns.processing.task.TaskExecutionEnvironment;
import com.oracle.coherence.patterns.processing.task.Yield;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.UUID;
import com.tangosol.util.extractor.ReflectionUpdater;
import com.tangosol.util.processor.UpdaterProcessor;

/**
* <p>
* A {@link TaskRunner} is responsible for claiming ownership of, running and
* returning the result of an executed
* {@link com.oracle.coherence.patterns.processing.task.Task}.
* </p>
*/
public class TaskRunner
        implements Runnable, TaskExecutionEnvironment
    {
    // ----- constructors ---------------------------------------------------

	/**
    * Constructor for the {@link TaskRunner}.
    * 
    * @param oPendingTaskKey            the {@link SubmissionKey} to the
    *                                   {@link Submission} to be executed
    * @param oExecutorIdentifier        the {@link Identifier} for the
    *                                   {@link com.oracle.coherence.patterns.processing.task.Executor}
    *                                   executing this task
    * @param oExecutorType              the {@link ExecutorType} of the Executor
    * @param oSubmissionManager         the {@link SubmissionManager} to use
    * @param oSubmissionResultManager   the {@link SubmissionResultManager} to
    *                                   use
    * @param oCCFactory                 the {@link ConfigurableCacheFactory} to use                                  
    */
    public TaskRunner(final SubmissionKey oPendingTaskKey,
                      final Identifier oExecutorIdentifier,
                      final ExecutorType oExecutorType,
                      final SubmissionManager oSubmissionManager,
                      final SubmissionResultManager oSubmissionResultManager,
                      final ConfigurableCacheFactory oCCFactory)
        {
        m_oPendingTaskKey           = oPendingTaskKey;
        m_oExecutorKey              = oExecutorIdentifier;
        m_oExecutorType             = oExecutorType;
        m_oExecutorQueueCache       = oCCFactory.ensureCache(DefaultExecutorManager.s_sQUEUECACHENAME, null);
        m_oSubmissionResultManager  = oSubmissionResultManager;
        m_oSubmissionManager        = oSubmissionManager;
        m_bIsResuming				= false;
        }

    // ----- TaskExecutionEnvironment methods -------------------------------
    /**
    * {@inheritDoc}
    */
    public Object loadCheckpoint()
        {
        return m_oSubmissionResultManager.loadCheckpoint(m_oResultUUID);
        }

    /**
    * {@inheritDoc}
    */
    public void reportProgress(final Object oProgress)
        {
        m_oSubmissionResultManager.reportProgress(m_oResultUUID,
                                                  oProgress);
        }

    /**
     * {@inheritDoc}
     */
     public void saveCheckpoint(final Object intermediateState)
         {
         m_oSubmissionResultManager.storeCheckpoint(m_oResultUUID, intermediateState);
         }
     
     /**
      * {@inheritDoc}
      */
      public boolean isResuming()
          {
    	  return m_bIsResuming;
          }


    // ----- Runnable methods -----------------------------------------------

    /**
    * {@inheritDoc}
    */
    public void run()
        {
        final Submission oSubmission = m_oSubmissionManager
            .getSubmission(m_oPendingTaskKey);
        m_oResultUUID = oSubmission.getResultUUID();
        final SubmissionResult oSubmissionresult = m_oSubmissionResultManager
            .getSubmissionResult(m_oResultUUID);

        if (oSubmissionresult != null)
            {
            ResumableTask oTaskToExecute = null;
            if (oSubmission.getPayload() instanceof ResumableTask)
                {
                oTaskToExecute = (ResumableTask) oSubmission.getPayload();
                }
            if (oTaskToExecute == null)
                {
                throw new UnsupportedOperationException(String
                    .format("Can't execute %s as it's not a Task",
                            m_oPendingTaskKey));
                }
            else
                {
                try
                    {
                    if (m_oSubmissionManager
                        .ownSubmission(m_oPendingTaskKey, CacheFactory
                            .getCluster().getLocalMember().getId()))
                        {
                        switch (oSubmissionresult.getSubmissionState())
                            {
                            case SUBMITTED:
                                {
                                doExecuteTask(oTaskToExecute, oSubmission.getSubmissionConfiguration());
                                break;
                                }
                            case EXECUTING:
                                {
                                m_bIsResuming = true;
                                doExecuteTask(oTaskToExecute,
                                        oSubmission.getSubmissionConfiguration());
                                break;
                                }
                            case SUSPENDED:
                                {
                                m_bIsResuming = true;
                                doExecuteTask(oTaskToExecute,
                                             oSubmission.getSubmissionConfiguration());
                                break;
                                }
                            }
                        }
                    else
                        {
                        Logger
                            .log(
                                 Logger.WARN,
                                 "TaskRunner - Task was already owned - key %s ",
                                 m_oPendingTaskKey);
                        }

                    }
                catch (final Exception exception)
                    {
                    if (Logger.isEnabled(Logger.WARN))
                        {
                        Logger
                            .log(
                                 Logger.WARN,
                                 "TaskRunner - Failed to process %s due to:\n%s",
                                 m_oPendingTaskKey, exception);
                        }
                    setProcessingFailed(exception);
                    }

                }
            }
        else
            {
            Logger.log(Logger.WARN,
                       "TaskRunner - Task was already executed - submission result already"
                                       + "deleted - submissionkey %s ",
                       m_oPendingTaskKey);
            }
        }

    // ----- TaskRunner methods -----------------------------------------------

    /**
    * Check the result of the Execution.
    * 
    * @param oRequestData the RequestData object
    * @param oResult the result of the execution
    */
    private void checkResult(final SubmissionConfiguration oRequestData, final Object oResult)
        {
        if (oResult instanceof Yield)
            {
            yield(((Yield) oResult).getIntermediateState());
            final DispatchController controller = ProcessingFrameworkFactory
                .getInstance().getDispatchController();
            controller.accept(new DefaultPendingSubmission(
                m_oPendingTaskKey, m_oResultUUID, oRequestData,
                ((Yield) oResult).getDelay()));
            }
        else
            {
            setProcessingSucceeded(oResult);
            Logger.log(Logger.INFO, "Executed %s to produce %s",
                       m_oPendingTaskKey, oResult);
            // remove the pending process as it's now completed
            removeSubmission();
            }
        }

    /**
    * Execute the task.
    * 
    * @param oTask          the {@link ResumableTask} to execute
    * @param oConfiguration the {@link SubmissionConfiguration} for this
    *                       task
    */
    private void doExecuteTask(final ResumableTask oTask,
        final SubmissionConfiguration oConfiguration)
        {
        setProcessingStarted();
        final Object oResult = oTask.run(this);
        checkResult(oConfiguration, oResult);
        }

    
    /**
    * Removes the submission.
    */
    private void removeSubmission()
        {
        m_oSubmissionManager.removeSubmission(m_oPendingTaskKey);
        if (m_oExecutorType == ExecutorType.QUEUEBASED)
            {
            m_oExecutorQueueCache
                    .invoke(m_oExecutorKey, new UpdaterProcessor(
                            new ReflectionUpdater("taskDone"), m_oPendingTaskKey));
            }
        }

    /**
    * Sets the execution result to failed.
    * 
    * @param oException the exception result
    */
    private void setProcessingFailed(final Exception oException)
        {
        m_oSubmissionResultManager
            .processingFailed(m_oResultUUID, oException);
        }

    /**
    * Sets the Submission state to started.
    */
    private void setProcessingStarted()
        {
        m_oSubmissionResultManager.startProcessing(m_oResultUUID);
        }

    /**
    * Store the successful result and sets the SubmissionState to DONE.
    * 
    * @param oResult the successful result
    */
    private void setProcessingSucceeded(final Object oResult)
        {
        m_oSubmissionResultManager
            .processingSucceeded(m_oResultUUID, oResult);
        }

    /**
    * 
    * @param intermediateState is the intermediate state to store away when
    *            yielding
    */
    private void yield(final Object intermediateState)
        {
        m_oSubmissionResultManager.yield(m_oResultUUID, intermediateState);
        }

    // ----- Members -----------------------------------------------

    /**
    * The ExecutorQueueCache.
    */
    private final NamedCache              m_oExecutorQueueCache;

    /**
    * The {@link ExecutorType}.
    */
    private final ExecutorType            m_oExecutorType;

    /**
    * The Executor {@link Identifier}.
    */
    private final Identifier              m_oExecutorKey;

    /**
    * The {@link SubmissionKey} identifying the {@link Submission}.
    */
    private final SubmissionKey           m_oPendingTaskKey;

    /**
    * The SubmissionResult {@link UUID}.
    */
    private UUID                          m_oResultUUID;

    /**
    * The {@link SubmissionManager} to use.
    */
    private final SubmissionManager       m_oSubmissionManager;

    /**
    * The {@link SubmissionResultManager} to use.
    */
    private final SubmissionResultManager m_oSubmissionResultManager;
    
    /**
    * A boolean indicating whether we are resuming a task. 
    */
    private boolean m_bIsResuming;


    }
