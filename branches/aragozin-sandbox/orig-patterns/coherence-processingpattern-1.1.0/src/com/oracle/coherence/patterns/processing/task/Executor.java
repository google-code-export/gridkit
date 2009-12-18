/*
 * File: Executor.java
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

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.processing.dispatchers.PendingSubmission;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.ConfigurableCacheFactory;

import java.io.Serializable;

/**
* <p>
* An Executor executes {@link Task} objects. An executor has a task type which
* is a string that is used to match tasks to executors.
* </p>
* 
* @author Christer Fahlgren 2009.09.30
*/
public interface Executor
    extends PortableObject, Serializable
    {

    /**
    * Executes the task embedded in the PendingSubmission.
    * 
    * @param oPendingSubmission contains the task to be executed
    */
    public void executeTask(PendingSubmission oPendingSubmission);

    /**
    * Returns a unique key for this {@link Executor}.
    * 
    * @return the unique key
    */
    public Identifier getExecutorKey();

    /**
    * Returns the {@link ExecutorType}.
    * 
    * @return the {@link ExecutorType}
    */
    public ExecutorType getExecutorType();

    /**
    * Returns the kind of tasks this {@link Executor} handles.
    * 
    * @return the task type as a string.
    */
    public String getTaskType();

    /**
    * Called when the {@link Executor} is unregistered.
    */
    public void onShutdown();

    /**
    * Called when the {@link Executor} is registered.
    * 
    * @param oCCFactory     the configurable cache factory to use when requesting any 
    *                       Caches from Coherence 
    */
    public void onStartup(ConfigurableCacheFactory oCCFactory);
    }
