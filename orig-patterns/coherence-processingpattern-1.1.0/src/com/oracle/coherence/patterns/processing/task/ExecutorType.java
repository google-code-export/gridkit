/*
 * File: ExecutorType.java
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
* The {@link ExecutorType} enumerates the type an {@link Executor} can be of.
* 
* @author Christer Fahlgren 2009.09.30
* 
*/
public enum ExecutorType
    {
    /**
    * GRID means that all storage-enabled nodes are expected to have this
    * executor.
    */
    GRID,
    
    /**
    * QUEUEBASED means that tasks are queued up and that an executor will poll work from
    * the queue. This allows specific nodes to poll work from a queue.
    */
    QUEUEBASED
    }
