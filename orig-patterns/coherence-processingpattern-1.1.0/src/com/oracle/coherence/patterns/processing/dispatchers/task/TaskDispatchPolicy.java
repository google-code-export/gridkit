/*
 * File: TaskDispatchPolicy.java
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

package com.oracle.coherence.patterns.processing.dispatchers.task;

import com.oracle.coherence.patterns.processing.task.Executor;
import com.oracle.coherence.patterns.processing.task.Task;

import java.io.Serializable;
import java.util.Collection;

/**
* The {@link TaskDispatchPolicy} is consulted by the {@link DefaultTaskDispatcher} to select
* which {@link Executor} will execute a particular Task.  
* 
* @author Christer Fahlgren 2009.09.30
*
*/
public interface TaskDispatchPolicy extends Serializable
    {

    /**
    * Method called by the {@link DefaultTaskDispatcher} to select an {@link Executor}.
    * 
    * @param task               is the {@link Task} to be executed.
    * @param oExecutorList      is the current list of registered executors.
    * 
    * @return the {@link Executor} that shall execute the task
    */
    Executor selectExecutor(Task task, Collection<Executor> oExecutorList);
    }
