/*
 * File: SubmissionState.java
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
* This enum enumerates the states a Submission can be in.
* <ul>
* <li>INITIAL is the state for a newly created Submission - not yet
* submitted.</li>
* <li>SUBMITTED is the state for a Submission that has been submitted, but
* not yet executed.</li>
* <li>EXECUTING is the state where a Submission is executing.</li>
* <li>SUSPENDED is the state where a Submission is voluntarily suspended.</li>
* <li>FAILED is a <b>final</b> state where execution was not successful.</li>
* <li>DONE is the <b>final</b> state where execution was successful.</li>
* </ul>
* 
* @author Christer Fahlgren 2009.09.29
*/
public enum SubmissionState
    {
    /**
    * DONE is the <b>final</b> state where execution was successful.
    */
    DONE,

    /**
    * EXECUTING is the state where a Submission is executing.
    */
    EXECUTING,

    /**
    * FAILED is a <b>final</b> state where execution was not successful.
    */
    FAILED,

    /**
    * INITIAL is the state for a newly created Submission - not yet
    * submitted.
    */
    INITIAL,
    /**
    * SUBMITTED is the state for a Submission that has been submitted, but
    * not yet executed.
    */
    SUBMITTED,

    /**
    * SUSPENDED is the state when a voluntary suspension of the exection has
    * happened.
    */
    SUSPENDED
    }
