/*
 * File: DefaultSubmissionConfiguration.java
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

import com.oracle.coherence.patterns.processing.SubmissionConfiguration;

import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.ExternalizableHelper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

/**
* <p>
* {@link DefaultSubmissionConfiguration} Provides additional data about a submission.
* </p>
* 
* @author Noah Arliss 2009.04.30
* @author Christer Fahlgren 2009.09.30
*/
@SuppressWarnings("serial")
public class DefaultSubmissionConfiguration
        implements ExternalizableLite, PortableObject, SubmissionConfiguration
    {
    // ----- constructors --------------------------------------------------------

    /**
    * <p>
    * Default constructor.
    * </p>
    */
    @SuppressWarnings("unchecked")
    public DefaultSubmissionConfiguration()
        {
        m_oConfigurationDataMap = new HashMap();
        }

    /**
    * <p>
    * Constructor which takes a delay parameter.
    * </p>
    * 
    * @param lDelay the delay before the request gets processed
    * 
    */
    @SuppressWarnings("unchecked")
    public DefaultSubmissionConfiguration(final long lDelay)
        {
        m_oConfigurationDataMap = new HashMap();
        m_lSubmissionDelay         = lDelay;
        }

    // ----- SubmissionConfiguration methods --------------------------------
    /**
    * {@inheritDoc} 
    */
    public Object getGroupAffinity()
        {
        return m_lGroupAffinity;
        }

    /**
    * {@inheritDoc} 
    */
    @SuppressWarnings("unchecked")
    public Map getConfigurationDataMap()
        {
        return m_oConfigurationDataMap;
        }

    /**
    * {@inheritDoc} 
    */
    public long getSubmissionDelay()
        {
        return m_lSubmissionDelay;
        }
    // ----- DefaultSubmissionConfiguration methods -------------------------
    
    /**
     * <p>
     * Set the groupAffinity object used to co-locate
     * {@link com.oracle.coherence.patterns.processing.internal.Submission}s
     * that should be processed on the same node.
     * </p>
     * 
     * @param oGroupAffinity 	the object used to co-locate
     *            				{@link com.oracle.coherence.patterns.processing.internal.Submission}	
     *            				s that should be processed on the same node.
     */
    public void setGroupAffinity(final Object oGroupAffinity)
    	{
    	this.m_lGroupAffinity = oGroupAffinity;
    	}

     /**
     * <p>
     * Set the {@link Map} with configuration data.
     * </p>
     * 
     * @param data the configuration data map
     */
     @SuppressWarnings("unchecked")
     public void setConfigurationDataMap(final Map data)
        {
        this.m_oConfigurationDataMap = data;
        }

     /**
     * <p>
     * Set amount of time a
     * {@link com.oracle.coherence.patterns.processing.internal.Submission}
     * should wait before being executed in milliseconds.
     * </p>
     * 
     * @param lSubmissionDelay 	the amount of time a
     *            				{@link com.oracle.coherence.patterns.processing.internal.Submission}
     *            				should wait before being executed in milliseconds
     */
     public void setSubmissionDelay(final long lSubmissionDelay)
     {
     this.m_lSubmissionDelay = lSubmissionDelay;
     }

    /**
    * <p>
    * Return a String representation of the {@link DefaultSubmissionConfiguration}.
    * </p>
    * 
    * @return a String representation of the {@link DefaultSubmissionConfiguration}
    */
    @Override
    public String toString()
        {
        return String
            .format(
                    "%s{requestDelay=%d, data=[%s] affinity=%s}",
                    this.getClass().getName(), m_lSubmissionDelay,
                    m_oConfigurationDataMap, m_lGroupAffinity);
        }

    // ----- ExternalizableLite methods -------------------------------------
    
    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public void readExternal(final DataInput in) throws IOException
        {
        this.m_lSubmissionDelay            = ExternalizableHelper.readLong(in);
        this.m_oConfigurationDataMap    = new HashMap();
        ExternalizableHelper.readMap(in, this.m_oConfigurationDataMap, Thread
            .currentThread().getContextClassLoader());
        this.m_lGroupAffinity		 	= ExternalizableHelper.readObject(in);
        }

    /**
     * {@inheritDoc} 
     */
     public void writeExternal(final DataOutput out) throws IOException
         {
         ExternalizableHelper.writeLong(out, this.m_lSubmissionDelay);
         ExternalizableHelper.writeMap(out, this.m_oConfigurationDataMap);
         ExternalizableHelper.writeObject(out, this.m_lGroupAffinity);
         }

    // ----- PortableObject methods -----------------------------------------

    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public void readExternal(final PofReader oReader) throws IOException
        {
        this.m_lSubmissionDelay            = oReader.readLong(0);
        this.m_oConfigurationDataMap    = oReader.readMap(1, new HashMap());
        this.m_lGroupAffinity           = oReader.readObject(2);
        }


    /**
     * {@inheritDoc}
     */
    public void writeExternal(final PofWriter oWriter) throws IOException
        {
        oWriter.writeLong(0, this.m_lSubmissionDelay);
        oWriter.writeMap(1, this.m_oConfigurationDataMap);
        oWriter.writeObject(2, this.m_lGroupAffinity);
        }

    // ----- Members --------------------------------------------------------

    
    /**
    * <p>
    * The groupAffinity object is used to co-locate processes that should be
    * executed on the same node.
    * </p>
    */
    private Object          m_lGroupAffinity;

    /**
    * <p>
    * The amount of time in milliseconds a request should wait before being
    * executed. The time of execution will be based on the time of the system
    * where this request is dispatched from.
    * </p>
    */
    private long            m_lSubmissionDelay;

    /**
    * <p>
    * Map of generic data associated with the request.
    * </p>
    */
    @SuppressWarnings("unchecked")
    private Map             m_oConfigurationDataMap;
     }
