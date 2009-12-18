/*
 * File: CreateSubmissionResultProcessor.java
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

import com.oracle.coherence.patterns.processing.SubmissionState;
import com.oracle.coherence.patterns.processing.internal.SubmissionKey;
import com.oracle.coherence.patterns.processing.internal.SubmissionResult;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.processor.AbstractProcessor;
import com.tangosol.util.UUID;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
* <p>
* This EntryProcessor creates a {@link SubmissionResult} in the grid with an
* initial state of SUBMITTED. If the entry was already present, its state is
* set to SUBMITTED.
* </p>
* 
* @author Christer Fahlgren 2009.09.30
* 
*/
@SuppressWarnings("serial")
public class CreateSubmissionResultProcessor
        extends AbstractProcessor
        implements PortableObject, ExternalizableLite
    {
    // ----- constructors ---------------------------------------------------

    /**
    * Default constructor, required by POF.
    */
    public CreateSubmissionResultProcessor()
        {
        }

    /**
    * Creates a processor for a particular {@link SubmissionKey} and 
    * {@link com.oracle.coherence.patterns.processing.ProcessingSession}.
    * 
    * @param oSubmissionKey the id of the submission request
    * @param identifier     the id of the session
    */
    public CreateSubmissionResultProcessor(final SubmissionKey oSubmissionKey,
                                           final Identifier identifier)
        {
        this.m_oSubmissionKey   = oSubmissionKey;
        this.m_oSessionid       = identifier;
        }

    /**
    * {@inheritDoc}
    */
    public Object process(final Entry oEntry)
        {
        if (oEntry.isPresent())
            {
            final SubmissionResult oResult = (SubmissionResult) oEntry.getValue();
            oResult.setSubmissionState(SubmissionState.SUBMITTED);
            oResult.setSubmissionTime(System.currentTimeMillis());
            oEntry.setValue(oResult);
            }
        else
            {
            final SubmissionResult oResult = new SubmissionResult(
                (UUID) oEntry.getKey(), m_oSubmissionKey, m_oSessionid, null,
                SubmissionState.SUBMITTED);
            oResult.setSubmissionTime(System.currentTimeMillis());
            oEntry.setValue(oResult);
            }
        return null;
        }

    // ----- ExternalizableLite Methods -------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final DataInput in) throws IOException
        {
        this.m_oSubmissionKey   = (SubmissionKey) ExternalizableHelper.readObject(in);
        this.m_oSessionid       = (Identifier) ExternalizableHelper.readObject(in);
        }

    /**
     * {@inheritDoc}
     */
     public void writeExternal(final DataOutput out) throws IOException
         {
         ExternalizableHelper.writeObject(out, m_oSubmissionKey);
         ExternalizableHelper.writeObject(out, m_oSessionid);

         }

    // ----- PortableObject Methods -----------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final PofReader oReader) throws IOException
        {
        this.m_oSubmissionKey   = (SubmissionKey) oReader.readObject(0);
        this.m_oSessionid       = (Identifier) oReader.readObject(1);
        }

    
    /**
    * {@inheritDoc}
    */
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        oWriter.writeObject(0, m_oSubmissionKey);
        oWriter.writeObject(1, m_oSessionid);
        }

    
    // ----- Members -----------------------------------------

    /**
    * SubmissionKey.
    */
    private SubmissionKey       m_oSubmissionKey;

    /**
    * Id of the session that submitted the submission.
    */
    private Identifier 			m_oSessionid;
    }
