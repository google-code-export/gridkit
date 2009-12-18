/*
 * File: DispatchController.java
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

import com.oracle.coherence.patterns.processing.friends.Subsystem;
import com.tangosol.net.ConfigurableCacheFactory;

/**
* <p>
* A {@link DispatchController} is responsible for asynchronously dispatching
* {@link PendingSubmission}s to one or more {@link Dispatcher}s for later
* processing.
* </p>
* 
* @author Noah Arliss 2009.04.30
*/
public interface DispatchController
        extends Runnable, Subsystem
    {

    /**
    * <p>
    * Requests that the {@link DispatchController} accept the specified
    * {@link PendingSubmission} and attempt to schedule it for dispatching
    * using a registered {@link Dispatcher} for processing.
    * </p>
    * 
    * @param oPendingSubmission The {@link PendingSubmission} to accept and
    *            schedule for dispatching.
    */
    public void accept(PendingSubmission oPendingSubmission);

    /**
    * <p>
    * Discards a currently scheduled {@link PendingSubmission} for
    * dispatching.
    * </p>
    * 
    * @param oPendingSubmission the {@link PendingSubmission} to discard
    */
    public void discard(PendingSubmission oPendingSubmission);

    /**
    * <p>
    * Hook called when a {@link Dispatcher} has been updated.
    * </p>
    * 
    * @param oDispatcher the {@link Dispatcher} that has been updated.
    */
    public void onDispatcherUpdate(Dispatcher oDispatcher);

    /**
    * <p> The dispatch controller is the JVM local object which
    * can keep track of the ConfigurableCacheFactory required to  
    * initialize dispatchers.
    * </p>
    * 
    * @return the ConfigurableCacheFactory for this JVM.
    */
    public ConfigurableCacheFactory getConfigurableCacheFactory();

    }
