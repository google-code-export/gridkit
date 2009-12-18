/*
 * File: DequeueTaskProcess.java
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
import com.oracle.coherence.patterns.processing.internal.task.ExecutorQueue;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
* Dequeues a task from the {@link ExecutorQueue}.
* 
* @author Christer Fahlgren 2009.09.30
*/
@SuppressWarnings("serial")
public class DequeueTaskProcessor
        extends AbstractProcessor
        implements PortableObject, ExternalizableLite
    {
    // ----- EntryProcessor Methods -----------------------------------------

    /**
    * {@inheritDoc}
    */
    public Object process(final Entry oEntry)
        {
        if (oEntry.isPresent())
            {
            ExecutorQueue oExecutor = (ExecutorQueue) oEntry.getValue();
            if (oExecutor == null)
                {
                oExecutor = new ExecutorQueue((Identifier) oEntry.getKey());
                }
            final Object result = oExecutor.dequeueTask();
            oEntry.setValue(oExecutor);
            return result;
            }
        return null;
        }

    // ----- ExternalizableLite Methods -----------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final DataInput oInput) throws IOException
        {
        }

    /**
    * {@inheritDoc}
    */
     public void writeExternal(final DataOutput oOutput) throws IOException
         {
         }

    // ----- PortableObject Methods -----------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final PofReader oReader) throws IOException
        {
        }

   
    /**
    * {@inheritDoc}
    */
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        }
    }
