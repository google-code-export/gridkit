/*
 * File: DispatchOutcome.java
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

package com.oracle.coherence.patterns.processing.dispatchers;

import java.util.concurrent.TimeUnit;

/**
* <p>
* A {@link DispatchOutcome} captures the result of an attempt to dispatch a
* {@link PendingSubmission} with a {@link Dispatcher} for processing.
* </p>
* 
* <p>
* Internally a {@link DispatchOutcome} is used to signal to a
* {@link DispatchController} what a {@link Dispatcher} did with a
* {@link PendingSubmission} during a call to
* {@link Dispatcher#dispatch(PendingSubmission)} and/or advice to the
* {@link DispatchController} on what to do next with the said
* {@link PendingSubmission}.
* </p>
* 
* @author Brian Oliver 2009.04.30
* @author Christer Fahlgren 2009.09.30
*/
public interface DispatchOutcome
    {

    /**
    * <p>
    * An {@link Accepted} {@link DispatchOutcome} signals that a
    * {@link Dispatcher} has successfully dispatched a
    * {@link PendingSubmission} for processing and that no further attempts
    * to dispatch the said {@link PendingSubmission} should be made by the
    * said {@link Dispatcher}.
    * </p>
    */
    public static final class Accepted
            implements DispatchOutcome
        {
        }

    /**
    * <p>
    * A {@link Rejected} {@link DispatchOutcome} signals that a
    * {@link Dispatcher} could not and may never be able to process a
    * {@link PendingSubmission}. The {@link DispatchController} may (and
    * should) attempt to use another {@link Dispatcher} to have the said
    * {@link PendingSubmission} processed.
    * </p>
    * 
    * <p>
    * Should all known {@link Dispatcher}s reject a {@link PendingSubmission}
    * (ie: not be able to dispatch it) for processing, the said
    * {@link PendingSubmission} will be suspended until a new {@link Dispatcher} is
    * registered / updated with the underlying {@link DispatchController}.
    * </p>
    */
    public static final class Rejected
            implements DispatchOutcome
        {
        }

    /**
    * <p>
    * A {@link RetryLater} {@link DispatchOutcome} signals that a
    * {@link Dispatcher} is capable of processing a {@link PendingSubmission}
    * but is currently unavailable to do so at a point in time.
    * </p>
    * 
    * <p>
    * The {@link DispatchController} may (and should) attempt to use another
    * {@link Dispatcher} to have the said {@link PendingSubmission}
    * processed, but if all those fail, the {@link DispatchController} should
    * only wait for the "shortest" possible time (indicated by the
    * "retryDelay" attribute) before attempting to "redispatch" the said
    * {@link PendingSubmission}.
    * </p>
    */
    public static final class RetryLater
            implements DispatchOutcome
        {
        
        /**
        * The delay to apply before resubmitting.
        */
        private long     m_lDelay;
        
        /**
        * The time unit for the delay.
        */
        private TimeUnit m_oTimeUnit;

        /**
        * Default constructor.
        */
        public RetryLater()
            {
            this.m_lDelay = 0;
            this.m_oTimeUnit = TimeUnit.SECONDS;
            }

        /**
        * Constructor taking the delay and time unit as input.
        *  
        * @param lDelay     the delay
        * @param oTimeUnit  the unit of time
        */
        public RetryLater(long lDelay, TimeUnit oTimeUnit)
            {
            this.m_lDelay = lDelay;
            this.m_oTimeUnit = oTimeUnit;
            }

        /**
        * Returns the delay.
        *  
        * @return the delay
        */
        public long getDelay()
            {
            return m_lDelay;
            }

        /**
        * Returns the {@link TimeUnit}.
        * 
        * @return the TimeUnit of the delay
        */
        public TimeUnit getTimeUnit()
            {
            return m_oTimeUnit;
            }
        }

    /**
    * <p>
    * An {@link Abort} {@link DispatchOutcome} signals that a
    * {@link Dispatcher} believes a {@link PendingSubmission} should no
    * longer be attempted to be dispatched or processed (typically due to
    * some error condition).
    * </p>
    * 
    * <p>
    * The {@link DispatchController} should assume that the
    * {@link PendingSubmission} has "finished" processing and set the result
    * of the processing for the said {@link PendingSubmission} be set to the
    * value provided by the {@link DispatchOutcome}.
    * </p>
    */
    public static final class Abort
            implements DispatchOutcome
        {
        
        /**
        * The rationale for aborting the submission. 
        */
        private String m_sRationale;
        
        /**
        * The result of the abortion of the submission.
        */
        private Object m_oResult;

        /**
        * Constructor taking rationale and result as input parameters.
        * 
        * @param sRationale the rationale for the abortion
        * @param oResult the potential result at the point of abortion
        */
        public Abort(String sRationale, Object oResult)
            {
            this.m_sRationale = sRationale;
            this.m_oResult = oResult;
            }

        /**
        * Returns the rationale.
        *  
        * @return the rationale
        */
        public String getRationale()
            {
            return m_sRationale;
            }

        /**
        * Returns the result.
        *  
        * @return the result object
        */
        public Object getResult()
            {
            return m_oResult;
            }
        }

    /**
    * Preconstructed {@link DispatchOutcome.Accepted} object.
    */
    public static final DispatchOutcome ACCEPTED = new DispatchOutcome.Accepted();
    
    /**
    * Preconstructed {@link DispatchOutcome.Rejected} object. 
    */
    public static final DispatchOutcome REJECTED = new DispatchOutcome.Rejected();
    
    /**
    * Preconstructed {@link DispatchOutcome.RetryLater} object. 
    */
    public static final DispatchOutcome CONTINUE = new DispatchOutcome.RetryLater();
    }
