/*
 * File: DefaultPendingSubmission.java
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

import com.oracle.coherence.patterns.processing.SubmissionConfiguration;
import com.oracle.coherence.patterns.processing.dispatchers.PendingSubmission;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.util.UUID;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
* <p>
* The default implementation of a {@link PendingSubmission}.
* </p>
* 
* @author Noah Arliss 2009.04.30
*/
public class DefaultPendingSubmission
        implements PendingSubmission
    {
    // ----- constructors ---------------------------------------------------

    /**
    * <p>
    * Standard Constructor.
    * </p>
    * 
    * @param submissionKey  the {@link UUID} of the {@link Submission} that is
    *                       pending
    * @param oResultUUID   the result {@link UUID} for the submission
    * @param oRequestData  the {@link DefaultSubmissionConfiguration} of the {@link Submission}
    * @param lDelay        the delay before execution of the submission
    */
    public DefaultPendingSubmission(final SubmissionKey submissionKey,
                                    final UUID oResultUUID,
                                    final SubmissionConfiguration oRequestData,
                                    final long lDelay)
        {
        this.m_oSubmissionKey   = submissionKey;
        this.m_oRequestData     = oRequestData;
        this.m_oResultUUID      = oResultUUID;
        this.m_lDispatchDelay   = lDelay;
        this.m_oTimeUnit        = TimeUnit.MILLISECONDS;
        this.m_lCreationTime    = System.currentTimeMillis();
        }
    // ----- PendingSubmission methods --------------------------------------

    /**
    * {@inheritDoc}
    */
    public long getCreationTime()
        {
        return m_lCreationTime;
        }

    /**
    * {@inheritDoc}
    */
    public long getDelay(final TimeUnit oUnit)
        {
        return oUnit.convert((m_lCreationTime + m_lDispatchDelay)
                             - System.currentTimeMillis(), m_oTimeUnit);
        }

    /**
    * {@inheritDoc}
    */
    public Object getPayload()
        {
        final NamedCache cache = CacheFactory.getCache(Submission.CACHENAME);
        return ((Submission) cache.get(getSubmissionKey())).getPayload();
        }

    /**
    * {@inheritDoc}
    */
    public SubmissionConfiguration getSubmissionConfiguration()
        {
        return m_oRequestData;
        }

    /**
    * Gets the result UUID.
    * 
    * @return the result UUID
    */
    public UUID getResultUUID()
        {
        return m_oResultUUID;
        }

    /**
    * {@inheritDoc}
    */
    public SubmissionKey getSubmissionKey()
        {
        return m_oSubmissionKey;
        }

    /**
    * {@inheritDoc}
    */
    public TimeUnit getTimeUnit()
        {
        return m_oTimeUnit;
        }

    // ----- Delayed methods ------------------------------------------------

    /**
    * {@inheritDoc}
    */
    public int compareTo(final Delayed o)
        {
        final PendingSubmission otherSubmission = (PendingSubmission) o;
        return (int) ((m_lCreationTime + m_lDispatchDelay) - (otherSubmission
                .getCreationTime() + otherSubmission
                        .getDelay(TimeUnit.MILLISECONDS)));
        }

    // ----- DefaultPendingSubmission methods -------------------------------

    /**
    * {@inheritDoc}
    */
    @Override
    public boolean equals(final Object oOther)
        {
        if (this == oOther)
            {
            return true;
            }
        if (oOther == null)
            {
            return false;
            }
        if (getClass() != oOther.getClass())
            {
            return false;
            }
        final DefaultPendingSubmission otherSubmission = (DefaultPendingSubmission) oOther;
        if (m_oSubmissionKey == null)
            {
            if (otherSubmission.m_oSubmissionKey != null)
                {
                return false;
                }
            }
        else
            {
            if (!m_oSubmissionKey.equals(otherSubmission.m_oSubmissionKey))
                {
                return false;
                }
            }
        return true;
        }


    /**
    * {@inheritDoc}
    */
    @Override
    public int hashCode()
        {
        final int prime = 31;
        int nResult = 1;
        nResult = prime
                  * nResult
                  + ((m_oSubmissionKey == null) ? 0 : m_oSubmissionKey
                      .hashCode());
        return nResult;
        }

    /**
    * <p>
    * Used internally to set the the amount of time to wait in before
    * retrying a {@link PendingSubmission} when a 
    * {@link com.oracle.coherence.patterns.processing.dispatchers.Dispatcher} has
    * requested a
    * {@link com.oracle.coherence.patterns.processing.dispatchers.DispatchOutcome.RetryLater}
    * .
    * </p>
    * 
    * @param dispatchDelay  the amount of time to wait in milliseconds before
    *                       retrying a {@link PendingSubmission}
    */
    public void setDelay(final long dispatchDelay)
        {
        this.m_lDispatchDelay = dispatchDelay;
        }

    /**
    * <p>
    * Set the TimeUnit for the dispatchDelay.
    * </p>
    * 
    * @param timeUnit the TimeUnit for the dispatchDelay}
    */
    public void setTimeUnit(final TimeUnit timeUnit)
        {
        this.m_oTimeUnit = timeUnit;
        }

    
    // ----- Members --------------------------------------------------------

    /**
    * The time the pending submission was created.
    */
    private final long          m_lCreationTime;

    /**
    * <p>
    * The time to wait before dispatching the {@link PendingSubmission} to a
    * {@link com.oracle.coherence.patterns.processing.dispatchers.Dispatcher}.
    * </p>
    */
    private long                m_lDispatchDelay;

    /**
    * <p>
    * The {@link SubmissionConfiguration} associated with the {@link PendingSubmission}.
    * </p>
    */
    private final SubmissionConfiguration   m_oRequestData;

    /**
    * <p>
    * The result UUID for the submission.
    * </p>
    */
    private final UUID          m_oResultUUID;

    /**
    * <p>
    * The {@link SubmissionKey} of the {@link Submission} that is pending.
    * </p>
    */
    private final SubmissionKey m_oSubmissionKey;

    /**
    * <p>
    * The {@link TimeUnit} for the dispatch delay.
    * </p>
    */
    private TimeUnit            m_oTimeUnit;
    }
