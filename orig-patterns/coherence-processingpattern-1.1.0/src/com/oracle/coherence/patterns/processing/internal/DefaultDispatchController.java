/*
 * File: DefaultDispatchController.java
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

package com.oracle.coherence.patterns.processing.internal;

import com.oracle.coherence.common.logging.Logger;

import com.oracle.coherence.patterns.processing.dispatchers.DispatchController;
import com.oracle.coherence.patterns.processing.dispatchers.DispatchOutcome;
import com.oracle.coherence.patterns.processing.dispatchers.Dispatcher;
import com.oracle.coherence.patterns.processing.dispatchers.PendingSubmission;
import com.oracle.coherence.patterns.processing.exceptions.NoDispatcherForSubmissionException;
import com.oracle.coherence.patterns.processing.friends.ProcessingFrameworkFactory;
import com.oracle.coherence.patterns.processing.friends.SubmissionManager;
import com.oracle.coherence.patterns.processing.friends.SubmissionResultManager;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MultiplexingMapListener;

import java.util.Iterator;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;

/**
* <p>
* A {@link DefaultDispatchController} is the standard implementation of a
* {@link DispatchController}.
* </p>
* 
* @author Noah Arliss 2009.04.30
*/
public class DefaultDispatchController
        implements DispatchController
    {
    // ----- constructors ---------------------------------------------------

    /**
    * <p>
    * Standard Constructor.
    * </p>
    * 
    * @param oCCFactory the {@link ConfigurableCacheFactory} to use
    */
    public DefaultDispatchController(ConfigurableCacheFactory oCCFactory)
        {
        m_oCCFactory = oCCFactory;
        }


    // ----- Subsystem methods ----------------------------------------------
    
    /**
    * {@inheritDoc}
    */
    public void startup()
        {
        m_oPendingSubmissions   = new DelayQueue<PendingSubmission>();
        m_oThread               = new Thread(this);
        m_oThread.start();
        }
    
    
    /**
    * {@inheritDoc}
    */
     public void shutdown()
         {
         m_oThread.interrupt();
         }


    // ----- DispatchController methods -------------------------------------

    /**
    * {@inheritDoc}
    */
    public void accept(final PendingSubmission oPendingSubmission)
        {
        final boolean result = m_oPendingSubmissions
            .offer(oPendingSubmission);
        assert (result == true);
        }

    /**
    * {@inheritDoc}
    */
    public void discard(final PendingSubmission oProcess)
        {
        m_oPendingSubmissions.remove(oProcess);
        }

    /**
    * {@inheritDoc}
    */
    public void onDispatcherUpdate(final Dispatcher dispatcher)
        {
        }
    
    /**
     * {@inheritDoc}
     */
     public ConfigurableCacheFactory getConfigurableCacheFactory()
         {
         return m_oCCFactory;
         }

    
    // ----- Runnable methods -----------------------------------------------

    /**
    * Take messages off the queue and dispatch them.
    */
    @SuppressWarnings("unchecked")
    public void run()
        {
        // First we need to get a hold of the registered Dispatchers.
        // This code must run here as it's not safe to run on a service thread
        final NamedCache dispatcherCache = m_oCCFactory
            .ensureCache(DefaultDispatcherManager.CACHENAME, null);

        // Get the current set of registered dispatchers
        m_oDispatcherList = new ConcurrentLinkedQueue<Dispatcher>();

        final Iterator iter = dispatcherCache.values().iterator();
        while (iter.hasNext())
            {
            final Dispatcher dispatcher = (Dispatcher) iter.next();
            dispatcher.onStartup(this);
            m_oDispatcherList.add(dispatcher);
            }

        // Register for updates to the dispatcher list
        dispatcherCache.addMapListener(new MultiplexingMapListener()
            {
                @Override
                protected void onMapEvent(final MapEvent mapEvent)
                    {
                    
                    if (mapEvent.getId() == MapEvent.ENTRY_INSERTED)
                        {
                        final Dispatcher dispatcher = (Dispatcher) mapEvent
                            .getNewValue();
                        Logger.log(Logger.INFO, "Starting dispatcher %s.",
                                   dispatcher.getName());

                        dispatcher.onStartup(DefaultDispatchController.this);
                        m_oDispatcherList.add(dispatcher);
                        }
                    else
                        {
                        if (mapEvent.getId() == MapEvent.ENTRY_UPDATED)
                            {
                            Logger.log(Logger.WARN, "Can't update dispatcher - dispatchers are immutable.");
                            }
                        else
                            {
                            if (mapEvent.getId() == MapEvent.ENTRY_DELETED)
                                {
                                final Dispatcher dispatcher = (Dispatcher) mapEvent
                                    .getOldValue();
                                if (dispatcher != null)
                                    {
                                    Logger
                                        .log(Logger.INFO,
                                             "Shutting down dispatcher %s.",
                                             dispatcher.getName());

                                    dispatcher
                                        .onShutdown(DefaultDispatchController.this);
                                    m_oDispatcherList.remove(dispatcher);
                                    }
                                }
                            }
                        }
                    }
            });

        // Dispatch submissions
        while (true)
            {
            try
                {
                final DefaultPendingSubmission oPendingSubmission = (DefaultPendingSubmission) m_oPendingSubmissions
                    .take();
                DispatchOutcome oResult = DispatchOutcome.REJECTED;
                final boolean fFirst = true;

                for (final Dispatcher dispatcher : m_oDispatcherList)
                    {
                    // Status = dispatcher.dispatch(process);
                    oResult = dispatcher.dispatch(oPendingSubmission);

                    if (oResult instanceof DispatchOutcome.Accepted)
                        {
                        break;
                        }
                    else
                        {
                        if (oResult instanceof DispatchOutcome.RetryLater)
                            {
                            final DispatchOutcome.RetryLater laterResult = (DispatchOutcome.RetryLater) oResult;

                            final long delay = laterResult.getDelay();
                            if (fFirst)
                                {
                                oPendingSubmission.setDelay(delay);
                                }
                            else
                                {
                                if (oPendingSubmission.getDelay(laterResult
                                    .getTimeUnit()) > delay)
                                    {
                                    oPendingSubmission.setDelay(delay);
                                    }
                                }
                            }
                        }
                    // TODO: Handle abort case
                    // if (result instanceof DispatchOutcome.Abort) {
                    // DispatchOutcome.Abort abortedResult =
                    // (DispatchOutcome.Abort)result;
                    // }
                    }
                if (oResult instanceof DispatchOutcome.RetryLater)
                    {
                    m_oPendingSubmissions.add(oPendingSubmission);
                    }
                else
                    {
                    if (oResult instanceof DispatchOutcome.Rejected)
                        {
                        // If no dispatcher took us, we will fail.
                        // Failing means returning exception and deleting the
                        // submission.
                        final SubmissionResultManager oResultmanager = ProcessingFrameworkFactory
                            .getInstance().getSubmissionResultManager();
                        oResultmanager
                            .processingFailed(
                                              oPendingSubmission
                                                  .getResultUUID(),
                                              new NoDispatcherForSubmissionException());
                        final SubmissionManager manager = ProcessingFrameworkFactory
                            .getInstance().getSubmissionManager();
                        manager.removeSubmission(oPendingSubmission
                            .getSubmissionKey());
                        }
                    }
                }
            catch (final InterruptedException oException)
                {
                // When shutdown is called, an InterruptException will occur.
                break;
                }
            }
        }

    // ----- Members --------------------------------------------------------

   
    /**
    * <p>
    * The list of {@link Dispatcher}s this controller will use to dispatch
    * {@link PendingSubmission}s for processing.
    * </p>
    */
    private volatile ConcurrentLinkedQueue<Dispatcher> m_oDispatcherList;

    /**
    * <p>
    * The queue of {@link PendingSubmission} that need to be dispatched.
    * </p>
    */
    private DelayQueue<PendingSubmission>        m_oPendingSubmissions;

    /**
    * The thread running the dispatching to dispatchers.
    */
    private Thread                               m_oThread;

    /**
    * The {@link ConfigurableCacheFactory} for this {@link DispatchController}.
    */
    private ConfigurableCacheFactory m_oCCFactory;

    }
