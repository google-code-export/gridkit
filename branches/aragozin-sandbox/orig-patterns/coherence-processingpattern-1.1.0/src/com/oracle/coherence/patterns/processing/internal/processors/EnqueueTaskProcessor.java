/*
 * File: EnqueueTaskProcessor.java
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

package com.oracle.coherence.patterns.processing.internal.processors;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.processing.internal.SubmissionKey;
import com.oracle.coherence.patterns.processing.internal.task.ExecutorQueue;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
* The EnqueueTaskProcessor enqueues a
* {@link com.oracle.coherence.patterns.processing.internal.Submission} for a
* particular Node specific
* {@link com.oracle.coherence.patterns.processing.task.Executor}.
* 
* @author Christer Fahlgren 2009.09.30
* 
*/
@SuppressWarnings("serial")
public class EnqueueTaskProcessor
        extends AbstractProcessor
        implements ExternalizableLite, PortableObject
    {
    // ----- constructors ---------------------------------------------------
    
    /**
    * Default Constructor.
    */
    public EnqueueTaskProcessor()
        {
        m_oSubmissionKey = null;
        }

    /**
    * Constructor taking the key to the
    * {@link com.oracle.coherence.patterns.processing.internal.Submission}
    * that shall be enqueued.
    * 
    * @param oSubmissionKey the {@link SubmissionKey}
    */
    public EnqueueTaskProcessor(final SubmissionKey oSubmissionKey)
        {
        m_oSubmissionKey = oSubmissionKey;
        }

    // ----- EntryProcessor Methods -----------------------------------------

    /**
    * {@inheritDoc}
    */
    public Object process(final Entry oEntry)
        {
        ExecutorQueue oQueue = (ExecutorQueue) oEntry.getValue();
        if (oQueue == null)
            {
            oQueue = new ExecutorQueue((Identifier) oEntry.getKey());
            }
        oQueue.enqueueTask(m_oSubmissionKey);
        oEntry.setValue(oQueue);
        return null;
        }

    // ----- ExternalizableLite Methods -------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final DataInput oInput) throws IOException
        {
        m_oSubmissionKey = (SubmissionKey) ExternalizableHelper.readObject(oInput);
        }
    
    /**
     * {@inheritDoc}
     */
     public void writeExternal(final DataOutput oOutput) throws IOException
         {
         ExternalizableHelper.writeObject(oOutput, m_oSubmissionKey);
         }

     // ----- PortableObject Methods ----------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final PofReader oReader) throws IOException
        {
        m_oSubmissionKey = (SubmissionKey) oReader.readObject(0);
        }


    /**
    * {@inheritDoc}
    */
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        oWriter.writeObject(0, m_oSubmissionKey);
        }

    // ----- Members --------------------------------------------------------

    /**
    * The key to be enqueued.
    */
    private SubmissionKey m_oSubmissionKey;
    }
