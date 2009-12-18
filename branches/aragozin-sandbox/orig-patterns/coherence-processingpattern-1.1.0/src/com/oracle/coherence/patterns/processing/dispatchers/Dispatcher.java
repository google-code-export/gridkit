/*
 * File: Dispatcher.java
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

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PortableObject;

import java.io.Serializable;

/**
* <p>
* A {@link Dispatcher} is responsible for dispatching a
* {@link PendingSubmission} to one or more components in a (possibly
* distributed) system that are capable of processing the said
* {@link PendingSubmission}.
* </p>
* 
* @author Noah Arliss 2009.04.30
* @author Brian Oliver 2009.04.30
*/
public interface Dispatcher
        extends PortableObject, ExternalizableLite, Serializable
    {

    /**
    * <p>
    * Return the name of the {@link Dispatcher}. Each {@link Dispatcher} requires a unique
    * name at runtime. This name will be used for logging as well as for
    * registration and un-registration.
    * </p>
    * 
    * @return the name of the dispatcher.
    */
    public String getName();

    /**
    * <p>
    * Attempts to dispatch the specified {@link PendingSubmission} for
    * processing (by an appropriate implementation), returning a
    * {@link DispatchOutcome} that represents the outcome.
    * </p>
    * 
    * <p>
    * <strong>NOTE:</strong>This method should not attempt to actually
    * execute or process the supplied {@link PendingSubmission}, especially
    * if the said processing will take a long period of time. The purpose of
    * this method is strictly to dispatch a {@link PendingSubmission} for
    * processing, not do the actual processing.
    * </p>
    * 
    * @param oPendingProcess the {@link PendingSubmission} to process
    * 
    * @return A {@link DispatchOutcome} of the result of dispatching the
    *         {@link PendingSubmission}
    */
    public DispatchOutcome dispatch(PendingSubmission oPendingProcess);

    /**
    * <p>
    * Called by the {@link DispatchController} that owns the
    * {@link Dispatcher} after the said {@link Dispatcher} has been
    * registered but before {@link PendingSubmission}s are provided to the
    * {@link #dispatch(PendingSubmission)} method.
    * </p>
    * 
    * @param oDispatchController the {@link DispatchController} that controls
    *            this {@link Dispatcher}
    */
    public void onStartup(DispatchController oDispatchController);

    /**
    * <p>
    * Called by the {@link DispatchController} that owns the
    * {@link Dispatcher} before the said {@link Dispatcher} is removed
    * (unregistered).
    * </p>
    * 
    * @param dispatchController the {@link DispatchController} that controls
    *            this {@link Dispatcher}
    */
    public void onShutdown(DispatchController dispatchController);
    }
