/*
 * File: AbstractResumableTask.java
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

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
* An {@link AbstractResumableTask} helps implement the {@link ResumableTask} interface.
* 
* @author Christer Fahlgren 2009.09.30
*/
@SuppressWarnings("serial")
public abstract class AbstractResumableTask
        implements ResumableTask, ExternalizableLite, PortableObject
    {
    // ----- constructors ---------------------------------------------------

    /**
    * Default constructor, required by POF.
    */
    public AbstractResumableTask()
        {
        }

    /**
    * Constructor which takes the type of the task.
    * 
    * @param type   the type of the task
    */
    public AbstractResumableTask(final String type)
        {
        m_sType = type;
        }

    // ----- ResumableTask Methods -----------------------------------------

    /**
    * {@inheritDoc}
    */
    public String getType()
        {
        return m_sType;
        }

    // ----- ExternalizableLite Methods -------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final DataInput in) throws IOException
        {
        this.m_sType = ExternalizableHelper.readSafeUTF(in);
        }
    
    /**
    * {@inheritDoc}
    */
     public void writeExternal(final DataOutput out) throws IOException
         {
         ExternalizableHelper.writeSafeUTF(out, m_sType);
         }

    // ----- PortableObject Methods -----------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final PofReader oReader) throws IOException
        {
        m_sType = oReader.readString(0);
        }
   
    /**
    * {@inheritDoc}
    */
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        oWriter.writeString(0, m_sType);
        }

    // ----- Members -----------------------------------------
    
    /**
    * The type of the task.
    */
    private String m_sType;

    }
