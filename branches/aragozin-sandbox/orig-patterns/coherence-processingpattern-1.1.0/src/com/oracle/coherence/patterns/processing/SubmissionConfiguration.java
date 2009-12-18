/*
 * File: SubmissionConfiguration.java
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

package com.oracle.coherence.patterns.processing;

import java.util.Map;

/**
* A {@link SubmissionConfiguration} provides additional configuration to a
* particular
* {@link com.oracle.coherence.patterns.processing.internal.Submission}.
* 
* Configuration data includes a group affinity object, which can be
* used to co-locate submissions on a particular node, a SubmissionDelay
* which is used to schedule the execution at a later time and a Map which
* can be used to provide arbitrary data.
* 
* @author Christer Fahlgren 2009.09.29
*/
public interface SubmissionConfiguration
    {
    /**
    * <p>
    * Return the groupAffinity object used to co-locate
    * {@link com.oracle.coherence.patterns.processing.internal.Submission}s
    * that should be processed on the same node.
    * </p>
    * 
    * @return the groupAffinity object used to co-locate
    *         {@link com.oracle.coherence.patterns.processing.internal.Submission}
    *         s that should be processed on the same node.
    */
    public Object getGroupAffinity();

    /**
    * <p>
    * Return the configuration data map.
    * </p>
    * 
    * @return the configuration data map
    */
    @SuppressWarnings("unchecked")
    public Map getConfigurationDataMap();

    /**
    * <p>
    * The amount of time a submission should wait before being executed in
    * milliseconds.
    * </p>
    * 
    * @return the amount of time a submission should wait before being
    *         executed in milliseconds
    */
    public long getSubmissionDelay();
    }
