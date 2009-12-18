/*
 * File: TaskTypeSelectionPolicy.java
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

import java.util.Collection;

/**
* The {@link TaskTypeSelectionPolicy} is a policy that selects 
* the first {@link Executor} in the list that has the same task type.
* 
* @author Christer Fahlgren 2009.09.30
*/
@SuppressWarnings("serial")
public class TaskTypeSelectionPolicy
        implements TaskDispatchPolicy
    {
    // ----- TaskDispatchPolicy Methods -------------------------------------
    
    /** 
    * {@inheritDoc}
    */
    public Executor selectExecutor(Task task,
        Collection<Executor> oExecutorList)
        {
        for (Executor oExecutor : oExecutorList)
            {
            if (oExecutor.getTaskType().equals(task.getType()))
                {
                return oExecutor;
                }
            }
        return null;
        }
    }
