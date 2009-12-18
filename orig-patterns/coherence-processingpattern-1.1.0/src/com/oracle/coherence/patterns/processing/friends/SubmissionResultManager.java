/*
 * File: SubmissionResultManager.java
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
package com.oracle.coherence.patterns.processing.friends;

import com.oracle.coherence.patterns.processing.internal.SubmissionResult;
import com.tangosol.util.UUID;

/**
* The {@link SubmissionResultManager} is an interface to manipulate
* {@link SubmissionResult} objects in the grid.
* 
* @author Christer Fahlgren 2009.09.30
* 
*/
public interface SubmissionResultManager
		extends Subsystem
    {

    /**
    * Get the {@link SubmissionResult} with a particular id.
    * 
    * @param oResultUUID the {@link UUID} of the {@link SubmissionResult}
    * 
    * @return the {@link SubmissionResult}
    */
    SubmissionResult getSubmissionResult(UUID oResultUUID);

    /**
    * This method marks the state of the {@link SubmissionResult} to be
    * EXECUTING.
    * 
    * @param oResultUUID is the {@link UUID} of the {@link SubmissionResult} object.
    */
    void startProcessing(UUID oResultUUID);

    /**
    * Sets the {@link SubmissionResult} to {@link com.oracle.coherence.patterns.processing.SubmissionState#DONE}. 
    * and stores the result.
    * 
    * @param oResultUUID   the id of the {@link SubmissionResult} object.
    * @param oResult       the result of the submission to store.
    */
    void processingSucceeded(UUID oResultUUID, Object oResult);

    /**
    * Sets the {@link SubmissionResult} to {@link com.oracle.coherence.patterns.processing.SubmissionState#FAILED}.
    * and stores the exception result.
    * 
    * @param oResultUUID   the {@link UUID} for the {@link SubmissionResult}
    * @param oException    the exception which occurred while processing
    */
    void processingFailed(UUID oResultUUID, Exception oException);

    /**
    * Report how much of the processing is completed.
    * 
    * @param oResultUUID   the id of the result
    * @param oProgress     an Object representing the progress.
    */
    void reportProgress(UUID oResultUUID, Object oProgress);

    /**
    * Store intermediate state of the processing in the result.
    * 
    * @param oResultUUID           the id of the result
    * @param oIntermediateState    the intermediate state to store.
    */
    void storeCheckpoint(UUID oResultUUID, Object oIntermediateState);

    /**
    * Load intermediate state of the processing.
    * 
    * @param oResultUUID the id of the result
    * 
    * @return the stored intermediate state 
    */
    Object loadCheckpoint(UUID oResultUUID);

    /**
    * Yield and set the state to {@link com.oracle.coherence.patterns.processing.SubmissionState#SUSPENDED}. 
    * Store intermediate state of the processing in the result.
    * 
    * @param oResultUUID        the id of the result
    * @param oIntermediateState the intermediate state to store.
    */
    void yield(UUID oResultUUID, Object oIntermediateState);

    /**
    * Returns the progress of this submission.
    * 
    * @param oResultUUID    the ID of the SubmissionResult whose progress we
    *                       want to know
    *                       
    * @return the progress as an Object
    */
    Object getProgress(UUID oResultUUID);
  }
