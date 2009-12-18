/*
 * File: Yield.java
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

package com.oracle.coherence.patterns.processing.task;

/**
* 
* The {@link Yield} object is used to suspend execution and store away an
* intermediate state. When execution resumes, the stored state is passed in
* to the Task. A Delay can be passed in to indicate that resumption should
* wait at least Delay milliseconds.
* 
* @author Christer Fahlgren 2009.09.30
*/
public class Yield
    {
    // ----- constructors ---------------------------------------------------

    /**
    * The constructor taking an intermediate state as input.
    * 
    * @param oIntermediatestate the state to store away
    * @param lDelay             the delay in milliseconds before rescheduling
    */
    public Yield(final Object oIntermediatestate, final long lDelay)
        {
        m_intermediateState = oIntermediatestate;
        m_lDelay            = lDelay;
        }

    // ----- Yield Methods -----------------------------------------

    /**
    * Return the delay.
    * 
    * @return the Delay
    */
    public long getDelay()
        {
        return m_lDelay;
        }

    /**
    * Returns the intermediate state.
    * 
    * @return the intermediate state
    */
    public Object getIntermediateState()
        {
        return m_intermediateState;
        }

    // ----- Members -----------------------------------------

    /**
    * The intermediate state.
    */
    private final Object m_intermediateState;

    /**
    * The number of milliseconds to yield.
    */
    private final long   m_lDelay;
    }
