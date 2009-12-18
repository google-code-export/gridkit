/*
 * File: ProcessingSession.java
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


/**
* <p>
* A {@link ProcessingSession} provides the mechanism by which we may submit
* and manage a Submission (and any associated configuration) for processing.
* </p>
* 
* @author Noah Arliss, Christer Fahlgren 2009.09.29
*/
public interface ProcessingSession
    {
    /**
    * <p>
    * Submit a payload and associated {@link SubmissionConfiguration} for
    * asynchronous processing.
    * </p>
    * 
    * @param oPayload       the payload to be processed
    * @param oConfiguration the {@link SubmissionConfiguration}
    *                       associated with the payload
    * 
    * @return A {@link SubmissionOutcome} to track the results of the
    *         asynchronous processing
    */
    public SubmissionOutcome submit(Object oPayload,
            SubmissionConfiguration oConfiguration);

    /**
    * <p>
    * Submit a payload and associated {@link SubmissionConfiguration} for
    * asynchronous processing as well as a {@link SubmissionOutcomeListener}.
    * The SubmissionOutcomeListener can be used to get notifications when
    * the state of the Submission processing changes.
    * </p>
    * 
    * @param oPayload           the payload to be processed
    * @param oConfigurationData the {@link SubmissionConfiguration}
    *                           associated with the payload
    * @param oListener          the {@link SubmissionOutcomeListener}
    * 
    * @return A {@link SubmissionOutcome} to track the results of the
    *         asynchronous processing
    */
    public SubmissionOutcome submit(Object oPayload,
            SubmissionConfiguration oConfigurationData,
            SubmissionOutcomeListener oListener);

    }
