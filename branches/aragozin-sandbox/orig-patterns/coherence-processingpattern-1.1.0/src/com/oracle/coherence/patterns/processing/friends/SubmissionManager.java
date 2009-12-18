/*
 * File: SubmissionManager.java
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

package com.oracle.coherence.patterns.processing.friends;

import com.oracle.coherence.patterns.processing.internal.Submission;
import com.oracle.coherence.patterns.processing.internal.SubmissionKey;

/**
* The {@link SubmissionManager} is an interface to manage immutable 
* {@link Submission} objects in the grid. 
* 
* @author Christer Fahlgren 2009.09.30
*/
public interface SubmissionManager
		extends Subsystem
    {

    /**
    * Returns the {@link Submission} pointed out by the key.
    * 
    * @param oKey The {@link SubmissionKey} to the {@link Submission}
    * 
    * @return the {@link Submission}
    */
    public Submission getSubmission(SubmissionKey oKey);

    /**
    * Adds an owner to the {@link Submission}. The owner is the entity processing
    * the {@link Submission}.
    * 
    * @param oKey   the {@link SubmissionKey} to the submission
    * @param oOwner the owner (the one who processes the submission)
    * 
    * @return returns true if successful, false if an owner already exists
    */
    public boolean ownSubmission(SubmissionKey oKey, Object oOwner);

    /**
    * Removes a {@link Submission} from the grid.
    * 
    * @param oSubmissionKey is the {@link SubmissionKey} to the {@link Submission}
    */
    public void removeSubmission(SubmissionKey oSubmissionKey);
    }
