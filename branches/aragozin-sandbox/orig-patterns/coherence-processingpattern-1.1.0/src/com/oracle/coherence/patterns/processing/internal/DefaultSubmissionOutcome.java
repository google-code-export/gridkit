/*
 * File: DefaultSubmissionOutcome.java
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

import com.oracle.coherence.patterns.processing.SubmissionOutcome;
import com.oracle.coherence.patterns.processing.SubmissionOutcomeListener;
import com.oracle.coherence.patterns.processing.SubmissionState;
import com.oracle.coherence.patterns.processing.friends.ProcessingFrameworkFactory;
import com.tangosol.util.UUID;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
* <p>
* Default implementation of the {@link SubmissionOutcome} interface.
* </p>
* 
* @author Noah Arliss 2009.04.30
* @author Christer Fahlgren 2009.09.30
*/
public class DefaultSubmissionOutcome
        implements SubmissionOutcome
    {
    // ----- constructors ---------------------------------------------------

    /**
    * Standard constructor taking the id of the {@link SubmissionResult} as a
    * parameter.
    * 
    * @param oResultUUID the id of the {@link SubmissionResult}
    */
    public DefaultSubmissionOutcome(final UUID oResultUUID)
        {
        this.m_oResultUUID = oResultUUID;
        }

    /**
    * Constructor that also registers a {@link SubmissionOutcomeListener}.
    * 
    * @param oResultUUID    the id of the {@link SubmissionResult}
    * @param oListener      the {@link SubmissionOutcomeListener} to 
    *                       callback to
    */
    public DefaultSubmissionOutcome(UUID oResultUUID,
            SubmissionOutcomeListener oListener)
        {
        this.m_oResultUUID                  = oResultUUID;
        this.m_oSubmissionOutcomeListener   = oListener;
        }

    // ----- SubmissionOutcome methods --------------------------------------

    /**
    * <p>
    * This method is called by a {@link com.tangosol.util.MapListener} in the
    * {@link DefaultProcessingSession} implementation when a
    * {@link SubmissionResult} has been created. It is used to notify a local
    * waiting thread of the arrival of a {@link SubmissionResult}.
    * </p>
    * 
    * @param oResult            the result of the execution
    * @param oState             the {@link SubmissionState}
    * @param lSubmissionTime    the time the submission was submitted
    * @param lLatency           the time it took until execution was started
    * @param lExecutionTime     the time it took to execute the submission
    */
    public void acceptProcessResult(final Object oResult,
            final SubmissionState oState, final long lSubmissionTime,
            final long lLatency, final long lExecutionTime)
        {
        synchronized (this)
            {
            this.m_oResult          = oResult;
            this.m_lSubmissionTime  = lSubmissionTime;
            this.m_lLatency         = lLatency;
            this.m_lExecutionTime   = lExecutionTime;
            this.m_oSubmissionState = oState;
            notify();
            if (m_oSubmissionOutcomeListener != null)
                {
                if (m_oSubmissionState == SubmissionState.FAILED)
                    {
                    m_oSubmissionOutcomeListener.onFailed(m_oResult);
                    }
                else
                    {
                    m_oSubmissionOutcomeListener.onDone(m_oResult);
                    }
                }
            }
        }

    /**
    * {@inheritDoc}
    */
    public Object get() throws InterruptedException, ExecutionException
        {
        synchronized (this)
            {
            if (!isDone())
                {
                wait();
                }
            // if the result has failed, re-throw the exception
            if (m_oResult instanceof Exception)
                {
                throw new ExecutionException(
                    "Execution Failed", (Exception) m_oResult);
                }
            else
                {
                return m_oResult;
                }
            }
        }

    /**
    * {@inheritDoc}
    */
    public Object get(final long timeout, final TimeUnit timeUnit)
            throws InterruptedException, ExecutionException, TimeoutException
        {
        synchronized (this)
            {
            if (!isDone())
                {
                timeUnit.timedWait(this, timeout);
                }
            
            if (m_oResult == null)
                {
                throw new TimeoutException();
                }
            else
                {
                // if the result has failed, re-throw the exception
                if (m_oResult instanceof Exception)
                    {
                    throw new ExecutionException(
                        "Execution Failed", (Exception) m_oResult);
                    }
                else
                    {
                    return m_oResult;
                    }
                }
            }
        }

    /**
    * {@inheritDoc}
    */
    public long getExecutionDuration()
        {
        return m_lExecutionTime;
        }

    /**
    * {@inheritDoc}
    */
    public Object getProgress()
        {
        return ProcessingFrameworkFactory.getInstance()
            .getSubmissionResultManager().getProgress(getResultUUID());
        }

    /**
    * {@inheritDoc}
    */
    public SubmissionState getSubmissionState()
        {
        return m_oSubmissionState;
        }

    /**
    * {@inheritDoc}
    */
    public long getSubmissionTime()
        {
        return m_lSubmissionTime;
        }

    /**
    * {@inheritDoc}
    */
    public long getWaitDuration()
        {
        return m_lLatency;
        }

    /**
    * {@inheritDoc}
    */
    public boolean isDone()
        {
        return m_oResult != null;
        }

    /**
    * Called when there is a progress notification.
    * 
    * @param object Object representing the progress
    */
    public void onProgress(final Object object)
        {
        if (m_oSubmissionOutcomeListener != null)
            {
            m_oSubmissionOutcomeListener.onProgress(object);
            }
        }

    /**
    * Called when execution has started.
    */
    public void onStarted()
        {
        if (m_oSubmissionOutcomeListener != null)
            {
            m_oSubmissionOutcomeListener.onStarted();
            }
        }

    /**
    * Called when execution was suspended.
    */
    public void onSuspended()
        {
        if (m_oSubmissionOutcomeListener != null)
            {
            m_oSubmissionOutcomeListener.onSuspended();
            }
        }

    /**
    * Returns the {@link UUID} that uniquely identifies the
    * {@link com.oracle.coherence.patterns.processing.internal.SubmissionResult}.
    * 
    * @return the id of the 
    *         {@link com.oracle.coherence.patterns.processing.internal.SubmissionResult}.
    */
    public UUID getResultUUID()
    	{
    	return m_oResultUUID;
    	}


    // ----- Members --------------------------------------------------------

    /**
    * The time it took to execute the submission.
    */
    private long                      m_lExecutionTime;

    /**
    * The time it took until it started executing from submission.
    */
    private long                      m_lLatency;

    /**
    * The time the submission was submitted for execution.
    */
    private long                      m_lSubmissionTime;

    /**
    * <p>
    * The result produced by processing the associated {@link Submission}.
    * </p>
    */
    private Object                    m_oResult;

    /**
    * The ID of the {@link SubmissionResult} object.
    */
    private final UUID                m_oResultUUID;

    /**
    * The listener for this {@link SubmissionOutcome}.
    */
    private SubmissionOutcomeListener m_oSubmissionOutcomeListener;

    /**
    * The {@link SubmissionState} for the Submission.
    */
    private SubmissionState           m_oSubmissionState;
    }
