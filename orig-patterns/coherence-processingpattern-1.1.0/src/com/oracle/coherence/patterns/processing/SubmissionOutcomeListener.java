/*
 * File: SubmissionOutcomeListener.java
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

/**
* A {@link SubmissionOutcomeListener} is a listener interface that can be
* registered enabling applications to get a notification for the progress of the
* Execution of the
* {@link com.oracle.coherence.patterns.processing.internal.Submission}.
* 
* @author Christer Fahlgren 2009.09.29
* 
*/
public interface SubmissionOutcomeListener
    {
    /**
    * Called when the
    * {@link com.oracle.coherence.patterns.processing.internal.Submission}
    * has successfully executed.
    * 
    * @param oResult the result of execution
    */
    void onDone(Object oResult);

    /**
    * 
    * Called when the
    * {@link com.oracle.coherence.patterns.processing.internal.Submission}
    * failed to successfully execute.
    * 
    * @param oResult the error result (usually an exception)
    */
    void onFailed(Object oResult);

    /**
    * Called when the execution of the
    * {@link com.oracle.coherence.patterns.processing.internal.Submission} is
    * reporting progress.
    * 
    * @param oProgress the progress expressed as an object
    */
    void onProgress(Object oProgress);

    /**
    * Called when execution of a
    * {@link com.oracle.coherence.patterns.processing.internal.Submission}
    * starts.
    */
    void onStarted();

    /**
    * Called when execution of a
    * {@link com.oracle.coherence.patterns.processing.internal.Submission} is
    * voluntarily suspended.
     */
    void onSuspended();

    }
