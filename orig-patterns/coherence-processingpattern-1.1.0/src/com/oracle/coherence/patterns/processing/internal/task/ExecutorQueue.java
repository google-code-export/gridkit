/*
 * File: ExecutorQueue.java
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

import com.oracle.coherence.patterns.processing.internal.SubmissionKey;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.ExternalizableHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.LinkedList;
import java.util.List;

/**
* <p>
* The {@link ExecutorQueue} models the queue for a particular executor. This queue
* enables {@link com.oracle.coherence.patterns.processing.executors.QueuePollExecutor}s 
* to rely on MapEvents to learn of new tasks to execute.
* </p>
* 
* @author Christer Fahlgren 2009.09.30
*/
@SuppressWarnings("serial")
public class ExecutorQueue
        implements ExternalizableLite, PortableObject
    {
    // ----- constructors -----------------------------------------

    /**
    * Default constructor as required by PortableObject and
    * ExternalizableLite.
    */
    public ExecutorQueue()
        {
        }

    /**
    * Constructor which takes the key of the Executor it belongs to.
    * 
    * @param identifier the executor key
    */
    public ExecutorQueue(final Identifier identifier)
        {
        m_oExecutorKey = identifier;
        initialize();
        }

    // ----- ExecutorQueue Methods ------------------------------------------

    /**
    * This method dequeues a task and returns the dequeued key. The dequeued
    * task gets transferred to the tasks in progress list.
    * 
    * @return the key of the task
    */
    public SubmissionKey dequeueTask()
        {
        if (m_oTasks == null)
            {
            initialize();
            }
        final SubmissionKey oTaskKey = m_oTasks.poll();
        if (oTaskKey != null)
            {
            m_oTasksInProgress.add(oTaskKey);
            }
        return oTaskKey;
        }

    /**
    * This method drains the queue and returns all the tasks. All tasks are
    * transferred to the tasks in progress list.
    * 
    * @return the list of Tasks to be queued
    */
    @SuppressWarnings("unchecked")
    public LinkedList<SubmissionKey> drainQueueToBeExecuted()
        {
        if (m_oTasks == null)
            {
            initialize();
            }
        final LinkedList<SubmissionKey> oResult = (LinkedList<SubmissionKey>) m_oTasks
            .clone();
        m_oTasksInProgress.addAll(m_oTasks);
        m_oTasks.clear();
        return oResult;
        }

    /**
    * Enqueue a particular task for this executor.
    * 
    * @param taskId the {@link SubmissionKey} of the Task to be enqueued.
    */
    public void enqueueTask(final SubmissionKey taskId)
        {
        if (m_oTasks == null)
            {
            initialize();
            }
        m_oTasks.add(taskId);
        }

    /**
    * Returns the key of the executor this queue belongs to.
    * 
    * @return the executor key.
    */
    public Identifier getExecutorKey()
        {
        return m_oExecutorKey;
        }

    /**
    * Returns the number of tasks in progress.
    * 
    * @return the number of tasks in progress
    */
    public int numberOfTasksInProgress()
        {
        return m_oTasksInProgress == null ? 0 : m_oTasksInProgress.size();
        }

    /**
    * Returns a list of {@link SubmissionKey}s to the tasks in progress.
    * 
    * @return the list of {@link SubmissionKey}s to the tasks in progress.
    */
    public List<SubmissionKey> getTasksInProgress()
        {
        return m_oTasksInProgress;
        }
    
    /**
    * Returns the size of the queue (excluding tasks in progress).
    * 
    * @return the size of the queue
    */
    public int size()
        {
        return m_oTasks == null ? 0 : m_oTasks.size();
        }
   
    /**
    * When a task is done, it is removed from the tasks in progress list.
    * 
    * @param oSubmissionKey the key to the task that was completed.
    */
    public void taskDone(final SubmissionKey oSubmissionKey)
        {
        m_oTasksInProgress.remove(oSubmissionKey);
        }

    /**
    * Convert the queue to a string.
    * 
    * @return a String representing the queue
    */
    @Override
    public String toString()
        {
        if (m_oTasks == null)
            {
            initialize();
            }
        String sResult = "Queue:" + m_oExecutorKey.toString();
        for (final SubmissionKey oKey : m_oTasks)
            {
            sResult += " k:" + oKey.toString();
            }
        if (numberOfTasksInProgress() > 0)
            {
            sResult += " InProgress:";
            for (final SubmissionKey oKey : m_oTasksInProgress)
                {
                sResult += " k:" + oKey.toString();
                }
            }
        return sResult;
        }

    /**
    * Internal initialize method.
    */
    private void initialize()
        {
        m_oTasks            = new LinkedList<SubmissionKey>();
        m_oTasksInProgress  = new LinkedList<SubmissionKey>();
        }

    // ----- ExternalizableLite Methods -------------------------------------
    
    /**
    * {@inheritDoc}
    */
     public void readExternal(final DataInput in) throws IOException
         {
         m_oExecutorKey      = (Identifier) ExternalizableHelper.readObject(in);
         m_oTasks            = (LinkedList<SubmissionKey>) ExternalizableHelper.readObject(in);
         m_oTasksInProgress  = (LinkedList<SubmissionKey>) ExternalizableHelper.readObject(in);
         }
     
    /**
    * {@inheritDoc}
    */
    public void writeExternal(final DataOutput out) throws IOException
        {
        ExternalizableHelper.writeObject(out, m_oExecutorKey);
        ExternalizableHelper.writeObject(out, m_oTasks);
        ExternalizableHelper.writeObject(out, m_oTasksInProgress);
        }

    // ----- PortableObject Methods -------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final PofReader oReader) throws IOException
        {
        m_oExecutorKey      = (Identifier) oReader.readObject(0);
        m_oTasks = new LinkedList<SubmissionKey>();
        oReader.readCollection(1, m_oTasks);
        m_oTasksInProgress  = new LinkedList<SubmissionKey>();
        oReader.readCollection(2, m_oTasksInProgress);
        }

    /**
     * {@inheritDoc}
     */
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        oWriter.writeObject(0, m_oExecutorKey);
        oWriter.writeCollection(1, m_oTasks);
        oWriter.writeCollection(2, m_oTasksInProgress);
        }
 
    // ----- Members --------------------------------------------------------

    /**
    * Key for the Executor this queue belongs to.
    */
    private Identifier                m_oExecutorKey;

    /**
    * A linked list of the keys in queue to be executed.
    */
    private LinkedList<SubmissionKey> m_oTasks;

    /**
    * A linked list of the keys of tasks that have been claimed for
    * processing.
    */
    private LinkedList<SubmissionKey> m_oTasksInProgress;
    }
