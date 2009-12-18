/*
 * File: DispatcherManager.java
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

import com.oracle.coherence.patterns.processing.dispatchers.Dispatcher;

/**
* A {@link DispatcherManager} is responsible for registering and
* unregistering {@link Dispatcher}s for the processing pattern.
* 
* @author Noah Arliss 2009.04.30
*/
public interface DispatcherManager
		extends Subsystem
    {

    /**
    * Register a {@link Dispatcher} with the system.
    * 
    * @param dispatcher the {@link Dispatcher} to register
    */
    public void registerDispatcher(Dispatcher dispatcher);

    /**
    * Unregister a {@link Dispatcher} with the system.
    * 
    * @param dispatcher the {@link Dispatcher} to unregister
    */
    public void unregisterDispatcher(Dispatcher dispatcher);
    }
