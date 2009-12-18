/*
 * File: LoggingDispatcher.java
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

package com.oracle.coherence.patterns.processing.dispatchers.logging;

import com.oracle.coherence.common.logging.Logger;
import com.oracle.coherence.patterns.processing.dispatchers.AbstractDispatcher;
import com.oracle.coherence.patterns.processing.dispatchers.DispatchOutcome;
import com.oracle.coherence.patterns.processing.dispatchers.PendingSubmission;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.util.ExternalizableHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
* <p>
* A simple
* {@link com.oracle.coherence.patterns.processing.dispatchers.Dispatcher}
* that logs {@link PendingSubmission} information to the Coherence Configured
* Logger.
* </p>
* 
* @author Brian Oliver 2009.04.30
*/
@SuppressWarnings("serial")
public class LoggingDispatcher
        extends AbstractDispatcher
    {
    // ----- constructors --------------------------------------------------------

    /**
    * <p>
    * Standard Constructor (logs at the level {@link Logger#INFO}).
    * </p>
    */
    public LoggingDispatcher()
        {
        this.m_nLogLevel = Logger.INFO;
        }

    /**
    * <p>
    * Standard Constructor (to log at a specified level).
    * </p>
    * 
    * @param nLogLevel the log level to use
    */
    public LoggingDispatcher(final int nLogLevel)
        {
        this.m_nLogLevel = nLogLevel;
        }

    /**
    * <p>
    * Constructor that includes the
    * {@link com.oracle.coherence.patterns.processing.dispatchers.Dispatcher}
    * name.
    * </p>
    * 
    * @param name the name of this {@link LoggingDispatcher}.
    */
    public LoggingDispatcher(final String name)
        {
        super(name);
        }

    // ----- Dispatcher interface -------------------------------------------

    /**
    * {@inheritDoc}
    */
    public DispatchOutcome dispatch(final PendingSubmission oPendingProcess)
        {
        Logger.log(m_nLogLevel, "[LoggingDispatcher] UUID=%s, Payload=%s",
                   oPendingProcess.getSubmissionKey(), oPendingProcess
                       .getPayload());
        return DispatchOutcome.CONTINUE;
        }

    // ----- ExternalizableLite ---------------------------------------------

    /**
    * {@inheritDoc}
    */
    @Override
    public void readExternal(final DataInput in) throws IOException
        {
        super.readExternal(in);
        this.m_nLogLevel = ExternalizableHelper.readInt(in);
        }

    /**
    * {@inheritDoc}
    */
    @Override
    public void writeExternal(final DataOutput out) throws IOException
        {
        super.writeExternal(out);
        ExternalizableHelper.writeInt(out, this.m_nLogLevel);
        }
    
    // ----- PortableObject interface ---------------------------------------

    /**
     * {@inheritDoc}
     */
     @Override
     public void readExternal(final PofReader oReader) throws IOException
         {
         super.readExternal(oReader);
         this.m_nLogLevel = oReader.readInt(100);
         }

    /**
    * {@inheritDoc}
    */
    @Override
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        super.writeExternal(oWriter);
        oWriter.writeInt(100, this.m_nLogLevel);
        }

    // ----- Members ---------------------------------------

    /**
    * <p>
    * The level at which this logger will log.
    * </p>
    */
    private int m_nLogLevel;
    }
