/*
 * File: AbstractExecutor.java
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

import com.oracle.coherence.common.identifiers.Identifier;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.ExternalizableHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
* An abstract Executor class helping the implementation of an {@link Executor}.
* 
* @author Christer Fahlgren 2009.09.30
*/
@SuppressWarnings("serial")
public abstract class AbstractExecutor
        implements Executor, ExternalizableLite, PortableObject
    {
    // ----- constructors -------------------------------------

    /**
    * Default constructor.
    */
    public AbstractExecutor()
        {
        }

    /**
    * Constructor taking key, id, tasktype and {@link ExecutorType}.
    * 
    * @param oUniqueID      the unique id for the executor
    * @param sName          the readable name for the executor
    * @param sTaskType      the tasktype it can handle
    * @param oExecutorType  the {@link ExecutorType} of the Executor
    */
    public AbstractExecutor(final Identifier oUniqueID, final String sName,
                            final String sTaskType,
                            final ExecutorType oExecutorType)
        {
        m_oUniqueID     = oUniqueID;
        m_sName         = sName;
        m_sTaskType     = sTaskType;
        m_oExecutorType = oExecutorType;
        }

    // ----- Executor Methods -----------------------------------------------

    /**
    * {@inheritDoc}
    */
    public Identifier getExecutorKey()
        {
        return m_oUniqueID;
        }

    /**
    * {@inheritDoc}
    */
    public ExecutorType getExecutorType()
        {
        return m_oExecutorType;
        }

    /** 
    * {@inheritDoc} 
    */
    public String getTaskType()
        {
        return m_sTaskType;
        }

    /**
    * {@inheritDoc}
    */
    public String toString()
        {
        final String result = "Executor name:" + m_sName + " Type:" + m_sTaskType;
        return result;
        }

    // ----- ExternalizableLite Methods -------------------------------------

    /**
    * {@inheritDoc}
    * 
    */
    public void readExternal(final DataInput in) throws IOException
        {
        m_oUniqueID     = (Identifier) ExternalizableHelper.readObject(in);
        m_sName         = ExternalizableHelper.readSafeUTF(in);
        m_sTaskType     = ExternalizableHelper.readSafeUTF(in);
        m_oExecutorType = (ExecutorType) ExternalizableHelper.readObject(in);
        }

    /**
    * {@inheritDoc}
    * 
    */
     public void writeExternal(final DataOutput out) throws IOException
         {
         ExternalizableHelper.writeObject(out, m_oUniqueID);
         ExternalizableHelper.writeSafeUTF(out, m_sName);
         ExternalizableHelper.writeSafeUTF(out, m_sTaskType);
         ExternalizableHelper.writeObject(out, m_oExecutorType);
         }

    // ----- PortableObject Methods -----------------------------------------

    /**
    * {@inheritDoc}
    * 
    */
    public void readExternal(final PofReader oReader) throws IOException
        {
        m_oUniqueID     = (Identifier) oReader.readObject(0);
        m_sName         = oReader.readString(1);
        m_sTaskType     = oReader.readString(2);
        m_oExecutorType = (ExecutorType) oReader.readObject(3);
        }

    /**
     * {@inheritDoc}
     * 
     */
     public void writeExternal(final PofWriter oWriter) throws IOException
         {
         oWriter.writeObject(0, m_oUniqueID);
         oWriter.writeString(1, m_sName);
         oWriter.writeString(2, m_sTaskType);
         oWriter.writeObject(3, m_oExecutorType);
         }

     // ----- Members -------------------------------------------------------
    
    /**
    * The {@link ExecutorType} that this Executor is of.
    */
    private ExecutorType m_oExecutorType;

    /**
    * The executor id, also used as a key for the executors cache.
    */
    private Identifier   m_oUniqueID;

    /**
    * The name of the Executor.
    */
    private String       m_sName;

    /**
    * The task type it handles.
    */
    private String       m_sTaskType;
    }
