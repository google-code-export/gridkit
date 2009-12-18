/*
 * File: DefaultTaskDispatcher.java
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

import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.patterns.processing.dispatchers.AbstractDispatcher;
import com.oracle.coherence.patterns.processing.dispatchers.DispatchController;
import com.oracle.coherence.patterns.processing.dispatchers.DispatchOutcome;
import com.oracle.coherence.patterns.processing.dispatchers.PendingSubmission;

import com.oracle.coherence.patterns.processing.internal.task.DefaultExecutorManager;
import com.oracle.coherence.patterns.processing.internal.task.LocalExecutorList;
import com.oracle.coherence.patterns.processing.internal.task.LocalExecutorList.ExecutorAlreadyExistsException;

import com.oracle.coherence.patterns.processing.task.Executor;
import com.oracle.coherence.patterns.processing.task.ResumableTask;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MultiplexingMapListener;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Iterator;

/**
* The {@link DefaultTaskDispatcher} is the main
* {@link com.oracle.coherence.patterns.processing.dispatchers.Dispatcher} for
* Tasks. It needs to be registered with the processing pattern.
* The Dispatching policy can be customized by supplying a 
* {@link TaskDispatchPolicy}.
* 
* @author Christer Fahlgren 2009.09.30
*/

@SuppressWarnings("serial")
public class DefaultTaskDispatcher
        extends AbstractDispatcher
    {
    // ----- constructors --------------------------------------------------------

    /**
    * The default constructor as required by PortableObject and
    * ExternalizableLite.
    */
    public DefaultTaskDispatcher()
        {
        m_oExecutorList = new LocalExecutorList();
        }

    /**
    * A constructor taking the name and the dispatch policy as input.
    * 
    * @param name               name of the dispatcher
    * @param taskdispatchpolicy the {@link TaskDispatchPolicy}
    */
    public DefaultTaskDispatcher(final String name,
                                 final TaskDispatchPolicy taskdispatchpolicy)
        {
        super(name);
        m_oTaskDispatchPolicy = taskdispatchpolicy;
        m_oExecutorList       = new LocalExecutorList();
        }

    // ----- Dispatcher interface  ------------------------------------------

    /**
    * {@inheritDoc}
    */
    public DispatchOutcome dispatch(final PendingSubmission oPendingProcess)
        {
        if (oPendingProcess.getPayload() instanceof ResumableTask)
            {
            // submit the pending process to the local executor to
            // execute
            if (dispatchTask(oPendingProcess))
                {
                return DispatchOutcome.ACCEPTED;
                }
            else
                {
                return DispatchOutcome.REJECTED;
                }

            }
        else
            {
            // we can't handle this type of pending process
            return DispatchOutcome.REJECTED;
            }
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void onShutdown(final DispatchController dispatchController)
        {
        super.onShutdown(dispatchController);
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void onStartup(final DispatchController dispatchController)
        {
        super.onStartup(dispatchController);
        try
            {
            initialize(dispatchController.getConfigurableCacheFactory());
            }
        catch (final ExecutorAlreadyExistsException oException)
            {
            oException.printStackTrace();
            }
        }

    
    // ----- ExternalizableLite interface -----------------------------------
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void readExternal(final DataInput in) throws IOException
        {
        super.readExternal(in);
        m_oTaskDispatchPolicy = (TaskDispatchPolicy) ExternalizableHelper
            .readObject(in);
        }

    /**
    * {@inheritDoc}
    */
     @Override
     public void writeExternal(final DataOutput out) throws IOException
         {
         super.writeExternal(out);
         ExternalizableHelper.writeObject(out, m_oTaskDispatchPolicy);
         }

     
    // ----- PortableObject interface ---------------------------------------
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void readExternal(final PofReader oReader) throws IOException
        {
        super.readExternal(oReader);
        m_oTaskDispatchPolicy = (TaskDispatchPolicy) oReader.readObject(100);
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        super.writeExternal(oWriter);
        oWriter.writeObject(100, m_oTaskDispatchPolicy);
        }

    
    // ----- DefaultTaskDispatcher Methods ----------------------------------
    
    /**
    * Dispatch a task depending on the supplied {@link TaskDispatchPolicy}.
    * 
    * @param oPendingSubmission the {@link PendingSubmission} to dispatch
    * 
    * @return whether dispatching was successful
    */
    private boolean dispatchTask(final PendingSubmission oPendingSubmission)
        {
        final ResumableTask task = (ResumableTask) oPendingSubmission.getPayload();
        final Executor executor = m_oTaskDispatchPolicy.selectExecutor(task, m_oExecutorList.getExecutorCollection());
        if (executor != null)
            {
            executor.executeTask(oPendingSubmission);
            return true;
            }
        else
            {
            return false;
            }
        }

    /**
    * Initialization of the {@link DefaultTaskDispatcher}.
    * 
    * @param oCCFactory the {@link ConfigurableCacheFactory} to use
    * 
    * @throws ExecutorAlreadyExistsException if an {@link Executor}
    *         already exists.
    */
    private void initialize(final ConfigurableCacheFactory oCCFactory) throws ExecutorAlreadyExistsException
        {
        m_oExecutorsCache = oCCFactory.ensureCache(DefaultExecutorManager.s_sCACHENAME, null);
        m_oExecutorsCache.addMapListener(new MultiplexingMapListener()
            {
                @Override
                protected void onMapEvent(final MapEvent mapEvent)
                    {
                    if (mapEvent.getId() == MapEvent.ENTRY_INSERTED)
                        {

                        final Executor oExecutor = (Executor) mapEvent.getNewValue();
                        try
                            {
                            m_oExecutorList.storeExecutor(oExecutor);
                            oExecutor.onStartup(oCCFactory);
                            Logger.log(Logger.INFO, "Starting Executor %s", oExecutor.toString());
                            
                            }
                        catch (final ExecutorAlreadyExistsException e)
                            {
                            e.printStackTrace();
                            }

                        }
                    else
                        if (mapEvent.getId() == MapEvent.ENTRY_UPDATED)
                            {
                            // Executors are immutable
                            }
                        else
                            if (mapEvent.getId() == MapEvent.ENTRY_DELETED)
                                {
                                final Executor executor = (Executor) mapEvent.getOldValue();
                                m_oExecutorList.removeExecutor(executor);
                                Logger.log(Logger.INFO, "Shutting down Executor %s", executor.toString());
                                
                                }
                    }
            });
        final Iterator<Executor> iter = m_oExecutorsCache.values().iterator();
        while (iter.hasNext())
            {
            final Executor oExecutor = iter.next();
            m_oExecutorList.storeExecutor(oExecutor);
            oExecutor.onStartup(oCCFactory);
            Logger.log(Logger.INFO, "Starting Executor %s", oExecutor.toString());
            }
        }

    // ----- Members --------------------------------------------------------
    
    /**
    * The policy for task dispatching.
    */
    private TaskDispatchPolicy m_oTaskDispatchPolicy;

    /**
    * The cache for executors.
    */
    NamedCache                 m_oExecutorsCache;

    /**
    * The list of Executors.
    */
    LocalExecutorList          m_oExecutorList;
    }
