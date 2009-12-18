/*
 * File: SubmissionKey.java
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

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.net.cache.KeyAssociation;

import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.UUID;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
* A key for the {@link Submission} object which supports KeyAssociation.
* 
* @author Noah Arliss 2009.04.30
*/
@SuppressWarnings("serial")
public class SubmissionKey
        implements KeyAssociation, ExternalizableLite, PortableObject
    {
    // ----- constructors ---------------------------------------------------

    /**
    * <p>
    * Used by the {@link ExternalizableLite} and {@link PortableObject}
    * interfaces.
    * </p>
     */
    public SubmissionKey()
        {
        m_oAssociatedKey = null;
        }

    /**
    * <p>
    * Construct a new {@link SubmissionKey}.
    * </p>
    * 
    * @param oAssociatedKey  the associatedKey to use
    * @param oSubmissionId  the {@link UUID} of the {@link Submission}
    */
    public SubmissionKey(final Object oAssociatedKey, final UUID oSubmissionId)
        {
        this.m_oAssociatedKey   = oAssociatedKey;
        this.m_oSubmissionId    = oSubmissionId;
        }

    // ----- SubmissionKey Methods ------------------------------------------
    
    /**
    * Checks for equality for this
    * {@link com.oracle.coherence.patterns.processing.dispatchers.PendingSubmission}
    * .
    * 
    * @param oOther the object to compare with
    * 
    * @return whether two keys are equal
    */
    @Override
    public boolean equals(final Object oOther)
        {
        if (this == oOther)
            {
            return true;
            }
        if (oOther == null)
            {
            return false;
            }
        if (getClass() != oOther.getClass())
            {
            return false;
            }
        final SubmissionKey otherKey = (SubmissionKey) oOther;
        if (m_oAssociatedKey == null)
            {
            if (otherKey.m_oAssociatedKey != null)
                {
                return false;
                }
            }
        else
            if (!m_oAssociatedKey.equals(otherKey.m_oAssociatedKey))
                {
                return false;
                }
        if (m_oSubmissionId == null)
            {
            if (otherKey.m_oSubmissionId != null)
                {
                return false;
                }
            }
        else
            if (!m_oSubmissionId.equals(otherKey.m_oSubmissionId))
                {
                return false;
                }
        return true;
        }

    /**
    * {@inheritDoc}
    */
    public Object getAssociatedKey()
        {
        return m_oAssociatedKey == null ? m_oSubmissionId : m_oAssociatedKey;
        }

    /**
    * Returns a hash code value for this
    * {@link com.oracle.coherence.patterns.processing.dispatchers.PendingSubmission}
    * .
    * 
    * @return the hash code value.
    */
    @Override
    public int hashCode()
        {
        final int nPrime = 31;
        int nResult = 1;
        nResult = nPrime
                 * nResult
                 + ((m_oAssociatedKey == null) ? 0 : m_oAssociatedKey
                     .hashCode());
        nResult = nPrime
                 * nResult
                 + ((m_oSubmissionId == null) ? 0 : m_oSubmissionId
                     .hashCode());
        return nResult;
        }

    /**
    * 
    * @return string representing the key.
    */
    @Override
    public String toString()
        {
        return m_oSubmissionId.toString();
        }
    // ----- ExternalizableLite Methods -------------------------------------
    
    /**
    * {@inheritDoc}
    */
    public void readExternal(final DataInput in) throws IOException
        {
        this.m_oAssociatedKey   = ExternalizableHelper.readObject(in);
        this.m_oSubmissionId    = (UUID) ExternalizableHelper.readObject(in);
        }

    /**
    * {@inheritDoc}
    */
    public void writeExternal(final DataOutput out) throws IOException
        {
        ExternalizableHelper.writeObject(out, this.m_oAssociatedKey);
        ExternalizableHelper.writeObject(out, this.m_oSubmissionId);
        }

    // ----- PortableObject Methods -----------------------------------------

    /**
    * {@inheritDoc}
    */
    public void readExternal(final PofReader oReader) throws IOException
        {
        this.m_oAssociatedKey   = oReader.readObject(0);
        this.m_oSubmissionId    = (UUID) oReader.readObject(1);
        }

   
    /**
    * {@inheritDoc}
    */
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        oWriter.writeObject(0, this.m_oAssociatedKey);
        oWriter.writeObject(1, this.m_oSubmissionId);
        }

    // ----- Members --------------------------------------------------------

    
    /**
    * <p>
    * The associated key.
    * </p>
    */
    private Object m_oAssociatedKey;

    /**
    * <p>
    * The {@link UUID} of the {@link Submission}.
    * </p>
    */
    private UUID   m_oSubmissionId;
    }
