/*
 * File: LocalExecutorList.java
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

package com.oracle.coherence.patterns.processing.internal.task;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.processing.task.Executor;

import java.util.Collection;
import java.util.HashMap;

/**
* The LocalExecutorList keeps track of the {@link Executor} list on a
* particular JVM. It makes sure only a single instance exists. Also, {@link Executor}s
* are immutable, they can't change after having been registered.
* 
* @author Christer Fahlgren 2009.09.30 
*/
public class LocalExecutorList
    {
    // ----- constructors ---------------------------------------------------

    /**
    * Private constructor only used by the static initializer.
    */
    public LocalExecutorList()
        {
        m_oExecutors = new HashMap<Identifier, Executor>();
        }

    // ----- LocalExecutorList Methods --------------------------------------

    /**
    * Get an {@link Executor} based on the unique identifier.
    * 
    * @param oExecutorIdentifier    the {@link Identifier} of the
    *                               {@link Executor}.
    * 
    * @return the {@link Executor}
    */
    public synchronized Executor getExecutor(final Identifier oExecutorIdentifier)
        {
        return m_oExecutors.get(oExecutorIdentifier);
        }

    /**
    * Returns all {@link Executor}s in a Collection.
    * 
    * @return the collection of {@link Executor}s
    */
    public synchronized Collection<Executor> getExecutorCollection()
        {
        return m_oExecutors.values();
        }

    /**
    * Removes an {@link Executor}.
    * 
    * @param oExecutor the Executor to remove.
    */
    public synchronized void removeExecutor(final Executor oExecutor)
        {
        oExecutor.onShutdown();
        m_oExecutors.remove(oExecutor.getExecutorKey());
        }

    /**
    * Stores an {@link Executor} in the list.
    * 
    * @param oExecutor the {@link Executor} to store
    * 
    * @throws ExecutorAlreadyExistsException if the Executor already exists
    *             in the list
    */
    public synchronized void storeExecutor(final Executor oExecutor)
        throws ExecutorAlreadyExistsException
        {
        final Executor existing = m_oExecutors
            .get(oExecutor.getExecutorKey());
        if (existing == null)
            {
            m_oExecutors.put(oExecutor.getExecutorKey(), oExecutor);
            }
        else
            {
            throw new ExecutorAlreadyExistsException(
                "Executor already exists");
            }
        }

    /**
    * Exception thrown if the Executor already exists. Executors are
    * immutable.
    */
    @SuppressWarnings("serial")
    public class ExecutorAlreadyExistsException
        extends Exception
        {

        /**
         * Constructor taking details of the Exception.
         * 
         * @param sExceptionDetails a string with details
         */
        public ExecutorAlreadyExistsException(String sExceptionDetails)
            {
            super(sExceptionDetails);
            }

        }

    /**
     * The map of {@link Executor}s.
     */
    private final HashMap<Identifier, Executor> m_oExecutors;

    }
