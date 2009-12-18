/*
 * File: TaskExecutionEnvironment.java
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

package com.oracle.coherence.patterns.processing.task;

/**
* <p>
* A TaskExecutionEnvironment is passed in to each {@link ResumableTask} as it is
* executing its run or resume method.
* </p>
* <p>
* Using the TaskExecutionEnvironment, a ResumableTask can find out if the task
* is resuming, and thus load a previously saved checkpoint to continue
* from a well-known state.
* </p>
* <p>
* Additionally, progress can be reported in the form of a Serializable Object.
* It is up to the Task and its consumer to define what Progress means.
* </p>
* 
* @author Christer Fahlgren 2009.09.30
*/
public interface TaskExecutionEnvironment
    {

    /**
    * <p>
    * A {@link ResumableTask} may want to store intermediate state, in 
    * case the {@link ResumableTask} is interruptible or fails while running.
    * </p>
    * 
    * @param oIntermediateState is the state to store
    */
    public void saveCheckpoint(Object oIntermediateState);

    /**
    * <p>
    * A {@link ResumableTask} may want to retrieve intermediate state, 
    * previously stored, in case the task is interruptible or fails while running.
    * </p>
    * 
    * @return the stored intermediate state 
    */
    public Object loadCheckpoint();

    /**
    * <p>
    * A {@link ResumableTask} may want to report progress. 
    * 
    * @param oProgress an object representing the progress
    */
    public void reportProgress(Object oProgress);

    /**
    * <p>
    * Returns true if the Task is resuming.
    * Assuming the Task is resuming, you could fetch checkpoint state
    * using the {@link #loadCheckpoint()} method.
    * </p>
    * 
    * @return true if the current task is resuming 
    */
     public boolean isResuming();

    }
