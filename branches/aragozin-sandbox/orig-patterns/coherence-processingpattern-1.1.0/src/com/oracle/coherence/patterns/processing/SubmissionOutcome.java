/*
 * File: SubmissionOutcome.java
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

package com.oracle.coherence.patterns.processing;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
* <p>
* A {@link SubmissionOutcome} acts as a future for the result of the Submission.
* It allows you to retrieve the status of the Submission as well as execution
* timing after the Submission is completed.
* </p>
* 
* @author Noah Arliss, Christer Fahlgren 2009.09.29
*/
public interface SubmissionOutcome
    {
    /**
    * <p>
    * Wait for the submitted payload to finish being processed and return the
    * result of the said processing.
    * </p>
    * 
    * @return the completed result
    * 
    * @throws InterruptedException if the processing was interrupted
    * @throws ExecutionException if the processing of the payload
    *         threw an exception
    */
    public Object get() throws InterruptedException, ExecutionException;

    /**
    * <p>
    * Wait the specified amount of time for the submitted payload to be
    * processed and return the result if available.
    * </p>
    * 
    * @param lTimeout  the maximum time to wait
    * @param oUnit     the unit of the timeout argument
    * 
    * @return the completed result
    * 
    * @throws ExecutionException
    * @throws InterruptedException
    * @throws ExecutionException if processing the payload threw an exception
    * @throws InterruptedException if the current thread was interrupted
    *       while waiting
    * @throws TimeoutException if the wait timed out
    */
    public Object get(long lTimeout, TimeUnit oUnit)
            throws InterruptedException, ExecutionException, TimeoutException;

    
    /**
    * Returns the current progress.
    * 
    * @return the progress
    */
    public Object getProgress();

    /**
    * Get the {@link SubmissionState} of this outcome.
    * 
    * @return the {@link SubmissionState}
    */
    public SubmissionState getSubmissionState();

    /**
    * Returns the time when the
    * {@link com.oracle.coherence.patterns.processing.internal.Submission}
    * was submitted in milliseconds after the epoch.
    * 
    * @return the submission time
    */
    public long getSubmissionTime();

    /**
    * Returns the delta time in milliseconds between submission and execution
    * started - i.e. the wait duration for the
    * {@link com.oracle.coherence.patterns.processing.internal.Submission}.
    * 
    * @return the wait duration in milliseconds
    */
    public long getWaitDuration();

    /**
    * Returns the time it took to execute the submission in milliseconds.
    * 
    * @return the time to execute in milliseconds
    */
    public long getExecutionDuration();

    /**
    * <p>
    * Return whether the submitted payload has finished being processed.
    * </p>
    * 
    * @return true if the submitted payload has finished being processed.
    */
    public boolean isDone();
    }
