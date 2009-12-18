/*
 * File: PendingSubmission.java
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

package com.oracle.coherence.patterns.processing.dispatchers;

import com.oracle.coherence.patterns.processing.SubmissionConfiguration;
import com.oracle.coherence.patterns.processing.internal.SubmissionKey;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
* <p>
* A {@link PendingSubmission} represents the payload (with associated
* {@link  com.oracle.coherence.patterns.processing.SubmissionConfiguration}
* ) of a {@link com.oracle.coherence.patterns.processing.internal.Submission}
* that has yet to be dispatched for processing using a {@link Dispatcher}.
* </p>
* 
* @author Noah Arliss 2009.04.30
* @author Brian Oliver 2009.04.30
* @author Christer Fahlgren 2009.09.30
*/
public interface PendingSubmission
        extends Delayed
    {

    /**
    * <p>
    * Return the {@link SubmissionKey} associated with the
    * {@link PendingSubmission}.
    * </p>
    * 
    * @return the {@link SubmissionKey} associated with the
    *         {@link PendingSubmission}.
    */
    public SubmissionKey getSubmissionKey();

    /**
    * <p>
    * Return the payload of the {@link PendingSubmission}.
    * </p>
    * 
    * @return the payload of the {@link PendingSubmission}
    */
    public Object getPayload();

    /**
    * <p>
    * Return the {@link  com.oracle.coherence.patterns.processing.SubmissionConfiguration} associated with the
    * {@link PendingSubmission}.
    * </p>
    * 
    * @return the {@link com.oracle.coherence.patterns.processing.SubmissionConfiguration} associated with the
    *         {@link PendingSubmission}
    */
    public SubmissionConfiguration getSubmissionConfiguration();

    /**
    * <p>
    * Return the {@link TimeUnit} used to represent the time for the
    * {@link Delayed} interface.
    * </p>
    * 
    * @return the {@link TimeUnit} for the {@link PendingSubmission}
    */
    public TimeUnit getTimeUnit();

    /**
    * Returns a hash code value for this {@link PendingSubmission}.
    * 
    * @return the hash code value.
    */
    public int hashCode();

    /**
    * Indicates whether some other object is "equal to" this
    * {@link PendingSubmission}.
    * 
    * @param that the object to compare
    * 
    * @return true if that is equal to this {@link PendingSubmission}
    */
    public boolean equals(Object that);

    /**
    * Returns the creation time (System.currentTimeMillis) of this
    * PendingSubmission.
    * 
    * @return the creation time
    */
    public long getCreationTime();
    }
