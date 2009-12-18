/*
 * File: Submission.java
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

import com.oracle.coherence.common.backingmaplisteners.Cause;
import com.oracle.coherence.common.backingmaplisteners.LifecycleAwareCacheEntry;
import com.oracle.coherence.common.identifiers.Identifier;

import com.oracle.coherence.patterns.processing.SubmissionConfiguration;
import com.oracle.coherence.patterns.processing.dispatchers.DispatchController;
import com.oracle.coherence.patterns.processing.friends.ProcessingFrameworkFactory;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.MapEvent;
import com.tangosol.util.UUID;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.HashSet;
import java.util.Set;

/**
* <p>
* A {@link Submission} captures a request to process some specific payload
* with associated meta data.
* </p>
* 
* @author Noah Arliss 2009.04.30
* @author Brian Oliver 2009.04.30
* @author Christer Fahlgren 2009.09.30
*/
@SuppressWarnings("serial")
public class Submission
        implements LifecycleAwareCacheEntry, PortableObject, 
        ExternalizableLite
    {
    // ----- constructors ---------------------------------------------------

    /**
    * <p>
    * Required for {@link ExternalizableLite} and {@link PortableObject}.
    * </p>
    */
    public Submission()
        {
        }

    /**
    * <p>
    * Standard Constructor.
    * </p>
    * 
    * @param oSubmissionUUID       the {@link UUID} of the submission.
    * @param oPayload              the object to process in the grid.
    * @param oConfigurationData    the {@link DefaultSubmissionConfiguration}
    *                              associated with the payload
    * @param oResultUUID           the UUID of the result associated with this
    *                              {@link Submission}
    * @param oSessionId            the UUID of the
    *                              {@link com.oracle.coherence.patterns.processing.ProcessingSession}
    *                              of this {@link Submission}
    */
    @SuppressWarnings("unchecked")
    public Submission(final UUID oSubmissionUUID, final Object oPayload,
                      final SubmissionConfiguration oConfigurationData,
                      final UUID oResultUUID,
                      final Identifier oSessionId)
        {
        this.m_oSubmissionUUID          = oSubmissionUUID;
        this.m_oPayload                 = oPayload;
        this.m_oSubmissionConfiguration = oConfigurationData;
        this.m_oResultUUID              = oResultUUID;
        this.m_oSessionIdentifier           = oSessionId;
        this.m_oOwnedBy                 = new HashSet();
        }

    // ----- Submission methods ---------------------------------------------------

    /**
    * <p>
    * Adds the specified owner that is responsible for processing this
    * {@link Submission}.
    * </p>
    * 
    * @param oOwner  an owner responsible for processing this
    *               {@link Submission}
    */
    @SuppressWarnings("unchecked")
    public void addOwner(final Object oOwner)
        {
        m_oOwnedBy.add(oOwner);
        }

    /**
    * <p>
    * Generate a {@link SubmissionKey} for this Submission.
    * </p>
    * 
    * @return a {@link SubmissionKey} for this Submission.
    */
    public SubmissionKey generateKey()
        {
        return new SubmissionKey(m_oSubmissionConfiguration
                .getGroupAffinity(), getUUID());
        }

    /**
    * <p>
    * Return the actual payload associated with this {@link Submission}.
    * </p>
    * 
    * @return the actual payload associated with this {@link Submission}
    */
    public Object getPayload()
        {
        return m_oPayload;
        }

    /**
    * <p>
    * Return the {@link DefaultSubmissionConfiguration} associated with this
    * {@link Submission}.
    * </p>
    * 
    * @return the {@link DefaultSubmissionConfiguration} associated with this
    *         {@link Submission}
    */
    public SubmissionConfiguration getSubmissionConfiguration()
        {
        return m_oSubmissionConfiguration;
        }

    /**
    * <p>
    * Returns the {@link UUID} that may be used to locate the the result (if
    * one is produced).
    * </p>
    * 
    * @return the {@link UUID} that may be used to locate the the result (if
    *         one is produced).
    */
    public UUID getResultUUID()
        {
        return m_oResultUUID;
        }

    /**
    * Return the {@link Identifier} for the
    * {@link com.oracle.coherence.patterns.processing.ProcessingSession} that
    * submitted this {@link Submission}.
    * 
    * @return the {@link Identifier} for the
    *         {@link com.oracle.coherence.patterns.processing.ProcessingSession}
    *         that submitted this {@link Submission}
    */
    public Identifier getSessionIdentifier()
        {
        return m_oSessionIdentifier;
        }

    /**
    * <p>
    * Returns the {@link UUID} of the {@link Submission} as allocated by the
    * {@link com.oracle.coherence.patterns.processing.ProcessingSession}.
    * </p>
    * 
    * @return the {@link UUID} of the {@link Submission} as allocated by the
    *         {@link com.oracle.coherence.patterns.processing.ProcessingSession}
    *         .
    */
    public UUID getUUID()
        {
        return m_oSubmissionUUID;
        }

    /**
    * Checks if this Submission has an owner.
    * 
    * @return true if it has an owner
    * 
    */
    public boolean hasOwner()
        {
        return (!m_oOwnedBy.isEmpty());
        }

    /**
    * <p>
    * Checks if the specified owner is an owner of this {@link Submission}.
    * </p>
    * 
    * @param owner an owner that may be responsible for processing this
    *            {@link Submission}
    * @return true if owned by owner
    */
    public boolean isOwnedBy(final Object owner)
        {
        return m_oOwnedBy.contains(owner);
        }

    /**
    * If the Submission doesn't have an owner, set it to the provided owner.
    * If it already has one, return null.
    * 
    * @param oOwnerId   the new owner
    * 
    * @return null if failed, the owner if successful
    */
    public Object tryToOwn(Object oOwnerId)
        {
        if (hasOwner())
            {
            if (isOwnedBy(oOwnerId))
                {
                return oOwnerId;
                }
            else
                {
                return null;
                }
            }
        else
            {
            addOwner(oOwnerId);
            return oOwnerId;
            }
        }
    
     /**
     * {@inheritDoc}
     */
     @Override
     public String toString()
         {
         return String.format(
                     "%s{uuid=%s, payload=%s, requestdata=%s, resultUUID=%s, ownedBy=%s}",
                     this.getClass().getName(), m_oSubmissionUUID, m_oPayload,
                     m_oSubmissionConfiguration, m_oResultUUID, m_oOwnedBy);
         }


    // ----- LifecycleAwareCacheEntry methods -------------------------------
 
    /**
    * {@inheritDoc}
    */
    public void onCacheEntryLifecycleEvent(final MapEvent mapEvent, 
            final Cause cause)
        {
        // handle when submission been arrived in the cluster
        final DispatchController controller = ProcessingFrameworkFactory
            .getInstance().getDispatchController();
        if (mapEvent.getId() == MapEvent.ENTRY_INSERTED)
            {
            Submission sub = (Submission) mapEvent.getNewValue();
            controller.accept(new DefaultPendingSubmission(
                    (SubmissionKey) mapEvent.getKey(),
                    sub.getResultUUID(),
                    sub.getSubmissionConfiguration(),
                    sub.getSubmissionConfiguration().getSubmissionDelay()));
            }
        else
            if (mapEvent.getId() == MapEvent.ENTRY_DELETED
                && cause == Cause.PartitionManagement)
                {
                controller.discard(new DefaultPendingSubmission(
                    (SubmissionKey) mapEvent.getKey(), ((Submission) mapEvent
                        .getNewValue()).getResultUUID(),
                    ((Submission) mapEvent.getOldValue())
                        .getSubmissionConfiguration(), 0));
                }
            else
                if (mapEvent.getId() == MapEvent.ENTRY_DELETED
                    && cause == Cause.Eviction)
                    {
                    throw new RuntimeException(
                        "The processing pattern doesn't handle eviction.");
                    }
        }

    // ----- ExternalizableLite  methods ------------------------------------
    
    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public void readExternal(final DataInput in) throws IOException
        {
        this.m_oSubmissionUUID          = (UUID) ExternalizableHelper.readObject(in);
        this.m_oPayload                 = ExternalizableHelper.readObject(in);
        this.m_oSubmissionConfiguration = (SubmissionConfiguration) ExternalizableHelper.readObject(in);
        this.m_oResultUUID              = (UUID) ExternalizableHelper.readExternalizableLite(in);
        this.m_oOwnedBy                 = new HashSet();
        ExternalizableHelper.readCollection(in, this.m_oOwnedBy, Thread
            .currentThread().getContextClassLoader());
        this.m_oSessionIdentifier       = (Identifier) ExternalizableHelper.readObject(in);
        }

    /**
    * {@inheritDoc}
    */
     public void writeExternal(final DataOutput out) throws IOException
         {
         ExternalizableHelper.writeObject(out, this.m_oSubmissionUUID);
         ExternalizableHelper.writeObject(out, this.m_oPayload);
         ExternalizableHelper.writeObject(out, this.m_oSubmissionConfiguration);
         ExternalizableHelper.writeExternalizableLite(out, this.m_oResultUUID);
         ExternalizableHelper.writeCollection(out, this.m_oOwnedBy);
         ExternalizableHelper.writeObject(out, this.m_oSessionIdentifier);
         }

    // ----- PortableObject methods -----------------------------------------
    
    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public void readExternal(final PofReader oReader) throws IOException
        {
        this.m_oSubmissionUUID          = (UUID) oReader.readObject(0);
        this.m_oPayload                 = oReader.readObject(1);
        this.m_oSubmissionConfiguration = (SubmissionConfiguration) oReader.readObject(2);
        this.m_oResultUUID              = (UUID) oReader.readObject(3);
        this.m_oOwnedBy                 = new HashSet();
        oReader.readCollection(4, this.m_oOwnedBy);
        this.m_oSessionIdentifier       = (Identifier) oReader.readObject(5);
        }

    /**
     * {@inheritDoc}
     */
     public void writeExternal(final PofWriter oWriter) throws IOException
         {
         oWriter.writeObject(0, this.m_oSubmissionUUID);
         oWriter.writeObject(1, this.m_oPayload);
         oWriter.writeObject(2, this.m_oSubmissionConfiguration);
         oWriter.writeObject(3, this.m_oResultUUID);
         oWriter.writeCollection(4, this.m_oOwnedBy);
         oWriter.writeObject(5, this.m_oSessionIdentifier);
         }

     // ----- Members -------------------------------------------------------  
    
    /**
    * <p>
    * The {@link Set} of executors that have own or have responsibility for
    * the {@link Submission}.
    * </p>
    */
    @SuppressWarnings("unchecked")
    private Set                     m_oOwnedBy;

    /**
    * <p>
    * The payload to process.
    * </p>
    */
    private Object                  m_oPayload;

    /**
    * <p>
    * the {@link DefaultSubmissionConfiguration} associated with the payload.
    * </p>
    */
    private SubmissionConfiguration m_oSubmissionConfiguration;

    /**
    * <p>
    * The {@link UUID} of the result in the results cache (if any is required
    * / produced).
    * </p>
    */
    private UUID                    m_oResultUUID;

    /**
    * <p>
    * The {@link UUID} of the submission (generated by a
    * {@link com.oracle.coherence.patterns.processing.ProcessingSession}).
    * </p>
    */
    private UUID                    m_oSubmissionUUID;

    /**
    * <p>
    * The {@link Identifier} of the
    * {@link com.oracle.coherence.patterns.processing.ProcessingSession} that
    * submitted this {@link Submission}.
    */
    private Identifier     			m_oSessionIdentifier;

    /**
    * <p>
    * The name of the Coherence Cache that will store {@link Submission}s.
    * </p>
    */
    public static final String      CACHENAME = "coherence.patterns.processing.submissions";
    }
