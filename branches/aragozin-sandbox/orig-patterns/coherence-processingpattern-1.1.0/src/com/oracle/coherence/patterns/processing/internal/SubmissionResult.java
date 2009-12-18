/*
 * File: SubmissionResult.java
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

import com.oracle.coherence.patterns.processing.SubmissionState;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.UUID;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
* <p>
* A {@link SubmissionResult} captures the result of processing a
* {@link Submission}.
* </p>
* 
* @author Noah Arliss 2009.04.30
* @author Brian Oliver 2009.04.30
* @author Christer Fahlgren 2009.09.30
*/
@SuppressWarnings("serial")
public class SubmissionResult
        implements ExternalizableLite, PortableObject
    {
    // ----- constructors -----------------------------------------

    /**
    * <p>
    * Required for {@link ExternalizableLite} and {@link PortableObject}.
    * </p>
    */
    public SubmissionResult()
        {
        m_oSubmissionState  = SubmissionState.INITIAL;
        m_oProgress         = 0;
        }

    /**
    * <p>
    * Standard Constructor.
    * </p>
    * 
    * @param oID            the unique ID of the {@link SubmissionResult}, 
    *                       also used as key.
    * @param oSubmissionKey the {@link SubmissionKey} of the
    *                       {@link Submission} for which this 
    *                       {@link SubmissionResult} is the result
    * @param oSessionUUID   the {@link Identifier} of the
    *                       {@link com.oracle.coherence.patterns.processing.ProcessingSession}
    * @param oResult        the result of the {@link Submission}.
    */
    public SubmissionResult(final UUID oID,
                            final SubmissionKey oSubmissionKey,
                            final Identifier oSessionUUID,
                            final Object oResult)
        {
        this.m_oID              = oID;
        this.m_oSubmissionKey   = oSubmissionKey;
        this.m_oSubmitterUUID   = oSessionUUID;
        this.m_oResult          = oResult;
        this.m_oSubmissionState = SubmissionState.DONE;
        this.m_oProgress        = null;

        }

    /**
    * <p>
    * Standard Constructor.
    * </p>
    * 
    * @param oID                the unique ID of the {@link SubmissionResult}, 
    *                           also used as key.
    * @param oSubmissionKey     the {@link SubmissionKey} of the 
    *                           {@link Submission} for which this 
    *                           {@link SubmissionResult} is the result
    * @param oSessionid         the identifier of the session
    * @param oResult            the result of the {@link Submission}.
    * @param oSubmissionState   is the {@link SubmissionState} of the
    *                           processing for a Submission
    */

    public SubmissionResult(final UUID oID,
                            final SubmissionKey oSubmissionKey,
                            final Identifier oSessionid,
                            final Object oResult,
                            final SubmissionState oSubmissionState)
        {
        this.m_oID              = oID;
        this.m_oSubmissionKey   = oSubmissionKey;
        this.m_oSubmitterUUID   = oSessionid;
        this.m_oResult          = oResult;
        this.m_oSubmissionState = oSubmissionState;
        this.m_oProgress        = null;
        }

    // ----- SubmissionResult Methods -----------------------------------------

    /**
    * Gets the execution time.
    * 
    * @return the execution time
    */
    public long getExecutionTime()
        {
        return m_lExecutionTime;
        }

    /**
    * Returns the unique ID of the SubmissionResult, also used as key for the
    * grid.
    * 
    * @return the unique id
    */
    public UUID getID()
        {
        return m_oID;
        }

    /**
    * Gets the latency - i.e. the time it took from submission to execution.
    * 
    * @return the latency.
    */
    public long getLatency()
        {
        return m_lLatency;
        }

    /**
    * Gets the progress.
    * 
    * @return the progress
    */
    public Object getProgress()
        {
        return m_oProgress;
        }

    /**
    * Return the underlying result.
    * 
    * @return the underlying result
    */
    public Object getResult()
        {
        return m_oResult;
        }

    /**
    * <p>
    * Returns the submissionkey.
    * </p>
    * 
    * @return the {@link SubmissionKey}
    */
    public SubmissionKey getSubmissionKey()
        {
        return m_oSubmissionKey;
        }

    /**
    * Return the {@link UUID} of the
    * {@link com.oracle.coherence.patterns.processing.ProcessingSession}
    * expecting this result.
    * 
    * @return       the {@link UUID} of the
    *               {@link com.oracle.coherence.patterns.processing.ProcessingSession}
    *               expecting this result.
    */
    public SubmissionState getSubmissionState()
        {
        return m_oSubmissionState;
        }

    /**
    * Gets the submission time.
    * 
    * @return the submission time.
    */
    public long getSubmissionTime()
        {
        return m_lSubmissionTime;
        }

    /**
    * Return the {@link Identifier} of the
    * {@link com.oracle.coherence.patterns.processing.ProcessingSession}
    * expecting this result.
    * 
    * @return      the {@link Identifier} of the
    *              {@link com.oracle.coherence.patterns.processing.ProcessingSession}
    *              expecting this result.
    */
    public Identifier getSubmitterUUID()
        {
        return m_oSubmitterUUID;
        }

    /**
    * Return whether the SubmissionResult is in a final state.
    *
    * @return true if the SubmissionResult is in a final state
    */
    public boolean isFinalState()
        {
        return (m_oSubmissionState == SubmissionState.DONE)
               || (m_oSubmissionState == SubmissionState.FAILED);
        }

    /**
    * Sets the execution time.
    * 
    * @param lExecutionTime the execution time
    */
    public void setExecutionTime(final long lExecutionTime)
        {
        m_lExecutionTime = lExecutionTime;
        }

    /**
    * Sets the latency.
    * 
    * @param lLatency the latency
    */
    public void setLatency(final long lLatency)
        {
        m_lLatency = lLatency;
        }

    /**
    * Sets the progress.
    * 
    * @param oProgress the progress for the submission.
    */

    public void setProgress(final Object oProgress)
        {
        m_oProgress = oProgress;
        }

    /**
    * Set the result of this {@link SubmissionResult}.
    * 
    * @param oResult the result
    */
    public void setResult(final Object oResult)
        {
        this.m_oResult = oResult;
        }

    /**
    * Sets the state of the {@link SubmissionResult}.
    * 
    * @param state the new state
    */
    public void setSubmissionState(final SubmissionState state)
        {
        this.m_oSubmissionState = state;
        }

    /**
    * Set the submission time.
    * 
    * @param lSubmissionTime the submission time
    */
    public void setSubmissionTime(final long lSubmissionTime)
        {
        m_lSubmissionTime = lSubmissionTime;
        }

    /**
    * Sets the entry to processing has started - i.e. executing.
    * 
    * @param oDummy a dummy parameter because the reflection based 
    *               ValueUpdater expects a parameter. In this case
    *               we don't need the parameter though.
    */
    public void processingStarted(Object oDummy)
        {
        setSubmissionState(SubmissionState.EXECUTING);
        setLatency(System.currentTimeMillis() - getSubmissionTime());
        }

    /**
    * Sets execution to failure.
    * 
    * @param oFailureResult the failure result
    */
    public void processingFailed(Object oFailureResult)
        {
        setSubmissionState(SubmissionState.FAILED);
        setResult(oFailureResult);
        }

    /**
    * Sets execution to success.
    * 
    * @param oResult the successful result
    */
    public void processingSucceeded(Object oResult)
        {
        setSubmissionState(SubmissionState.DONE);
        setResult(oResult);
        setExecutionTime(System.currentTimeMillis() - getSubmissionTime()
                         - getLatency());
        }

    /**
    * Sets execution to success.
    * 
    * @param oResult the successful result
    */
    public void suspendExecution(Object oResult)
        {
        setSubmissionState(SubmissionState.SUSPENDED);
        setResult(oResult);
        }

    // ----- ExternalizableLite Methods -------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final DataInput in) throws IOException
        {
        this.m_oID              = (UUID) ExternalizableHelper.readObject(in);
        this.m_oSubmissionKey   = (SubmissionKey) ExternalizableHelper.readObject(in);
        this.m_oSubmitterUUID   = (Identifier) ExternalizableHelper.readObject(in);
        this.m_oResult          = ExternalizableHelper.readObject(in);
        this.m_oSubmissionState = (SubmissionState) Enum.valueOf(SubmissionState.class, ExternalizableHelper.readSafeUTF(in));
        this.m_oProgress        = ExternalizableHelper.readObject(in);
        this.m_lSubmissionTime  = ExternalizableHelper.readLong(in);
        this.m_lLatency         = ExternalizableHelper.readLong(in);
        this.m_lExecutionTime   = ExternalizableHelper.readLong(in);
        }
    
    /**
     * {@inheritDoc}
     */
     public void writeExternal(final DataOutput out) throws IOException
         {
         ExternalizableHelper.writeObject(out, this.m_oID);
         ExternalizableHelper.writeObject(out, this.m_oSubmissionKey);
         ExternalizableHelper.writeObject(out, this.m_oSubmitterUUID);
         ExternalizableHelper.writeObject(out, this.m_oResult);
         ExternalizableHelper.writeSafeUTF(out, m_oSubmissionState.name());
         ExternalizableHelper.writeObject(out, this.m_oProgress);
         ExternalizableHelper.writeLong(out, this.m_lSubmissionTime);
         ExternalizableHelper.writeLong(out, this.m_lLatency);
         ExternalizableHelper.writeLong(out, this.m_lExecutionTime);

         }


    // ----- PortableObject Methods -----------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final PofReader oReader) throws IOException
        {
        this.m_oID              = (UUID) oReader.readObject(0);
        this.m_oSubmissionKey   = (SubmissionKey) oReader.readObject(1);
        this.m_oSubmitterUUID   = (Identifier) oReader.readObject(2);
        this.m_oResult          = oReader.readObject(3);
        this.m_oSubmissionState = (SubmissionState) Enum.valueOf(SubmissionState.class, oReader.readString(4));
        this.m_oProgress        = oReader.readObject(5);
        this.m_lSubmissionTime  = oReader.readLong(6);
        this.m_lLatency         = oReader.readLong(7);
        this.m_lExecutionTime   = oReader.readLong(8);
        }

    
    /**
    * {@inheritDoc}
    */
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        oWriter.writeObject(0, m_oID);
        oWriter.writeObject(1, m_oSubmissionKey);
        oWriter.writeObject(2, m_oSubmitterUUID);
        oWriter.writeObject(3, m_oResult);
        oWriter.writeString(4, m_oSubmissionState.name());
        oWriter.writeObject(5, m_oProgress);
        oWriter.writeLong(6, m_lSubmissionTime);
        oWriter.writeLong(7, m_lLatency);
        oWriter.writeLong(8, m_lExecutionTime);
        }

    // ----- Members --------------------------------------------------------

    
    /**
    * The time in milliseconds it took to execute the Submission.
    */
    private long                m_lExecutionTime;

    /**
    * The time it took in milliseconds until execution started.
    */
    private long                m_lLatency;

    /**
    * The time this item was submitted, in milliseconds since the epoch.
    */
    private long                m_lSubmissionTime;

    /**
    * The progress of this particular Submission.
    */
    private Object              m_oProgress;

    /**
    * <p>
    * The {@link UUID} of the result, used as the key for the Result.
    * </p>
    */
    private UUID                m_oID;

    /**
    * <p>
    * The result produced by processing the associated {@link Submission}.
    * </p>
    */
    private Object              m_oResult;

    /**
    * <p>
    * The {@link SubmissionKey} of the {@link Submission} that produced the
    * {@link SubmissionResult}.
    * </p>
    */
    private SubmissionKey       m_oSubmissionKey;

    /**
    * <p>
    * The {@link SubmissionState} represents the current state of the
    * submission.
    */
    private SubmissionState     m_oSubmissionState;

    /**
    * <p>
    * The {@link Identifier} of the
    * {@link com.oracle.coherence.patterns.processing.ProcessingSession} that
    * is expecting this {@link SubmissionResult}.
    */
    private Identifier 			m_oSubmitterUUID;

    /**
    * <p>
    * The name of the Coherence Cache that will store
    * {@link SubmissionResult}s.
    * </p>
    */
    public static final String  CACHENAME = "coherence.patterns.processing.submissionresults";
    }
