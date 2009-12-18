/*
 * File: AbstractDispatcher.java
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
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.ExternalizableHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
* <p>
* A base implementation of a {@link Dispatcher}.
* </p>
* 
* @author Brian Oliver 2009.04.30
*/
@SuppressWarnings("serial")
public abstract class AbstractDispatcher
        implements Dispatcher, PortableObject, ExternalizableLite
    {
    // ----- constructors ---------------------------------------------------

    /**
    * <p>
    * Standard Constructor.
    * </p>
    */
    public AbstractDispatcher()
        {
        }

    /**
    * <p>
    * Constructor that includes the name of the {@link Dispatcher}.
    * </p>
    * 
    * @param sName the name of the {@link Dispatcher}
    */
    public AbstractDispatcher(final String sName)
        {
        this.m_sName = sName;
        }

    
    // ----- Dispatcher interface -------------------------------------------

    /**
    * <p>
    * Returns the {@link DispatchController} that owns this
    * {@link Dispatcher}.
    * </p>
    * 
    * @return the DispatchController
    */
    public DispatchController getDispatchController()
        {
        return m_oDispatchController;
        }

    /**
    * {@inheritDoc}
    */
    public String getName()
        {
        return m_sName;
        }

    /**
    * <p>
    * Returns if the {@link Dispatcher} is active (ie: has a
    * {@link DispatchController}).
    * </p>
    * 
    * @return whether the {@link DispatchController} is active - i.e. not null
    */
    public boolean isActive()
        {
        return m_oDispatchController != null;
        }

    /**
    * {@inheritDoc}
    */
    public void onShutdown(final DispatchController dispatchController)
        {
        this.m_oDispatchController = null;
        }

    /**
    * {@inheritDoc}
    */
    public void onStartup(final DispatchController dispatchController)
        {
        this.m_oDispatchController = dispatchController;
        }

    // ----- ExternalizableLite interface -----------------------------------
 
    /**
    * {@inheritDoc}
    */
    public void readExternal(DataInput in) throws IOException
        {
        m_sName = ExternalizableHelper.readSafeUTF(in);
        }

    /**
     * {@inheritDoc}
     */
     public void writeExternal(DataOutput out) throws IOException
         {
         ExternalizableHelper.writeSafeUTF(out, m_sName);
         }


    // ----- PortableObject interface --------------------------------------------------

     /**
      * {@inheritDoc}
      */
      public void readExternal(PofReader oReader) throws IOException
          {
          m_sName = oReader.readString(0);
          }

    /**
    * {@inheritDoc}
    */
    public void writeExternal(PofWriter oWriter) throws IOException
        {
        oWriter.writeString(0, m_sName);
        }

    // ----- members --------------------------------------------------------

    
    /**
    * <p>
    * The {@link DispatchController} that owns this {@link Dispatcher}.
    * </p>
    */
    private DispatchController m_oDispatchController;

    /**
    * <p>
    * The name of this {@link Dispatcher}.
    * </p>
    */
    private String             m_sName;
    }
