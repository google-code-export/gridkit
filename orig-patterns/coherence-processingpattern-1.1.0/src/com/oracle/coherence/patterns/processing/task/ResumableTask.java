/*
 * File: ResumableTask.java
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
* 
* The {@link ResumableTask} interface is the main interface for a 
* general purpose task. A task has a type which is used to match it against an
* executor. The task is passed a {@link TaskExecutionEnvironment} with which
* it can interact.
* 
* When a task executes, it can return any object as a result. A special
* result is the {@link Yield} object which can be used to pause the execution
* and store an intermediate state for a later resumption of execution.
* 
* @author Christer Fahlgren 2009.09.30
*/
public interface ResumableTask
        extends Task
    {
    /**
    * run is called by the {@link Executor} to execute the task. {@link Yield} may be
    * returned to pause execution.
    * 
    * @param oEnvironment   is the {@link TaskExecutionEnvironment} used to interact
    *                       with the environment
    *                         
    * @return the result of the task
    */
    public Object run(TaskExecutionEnvironment oEnvironment);

    }
