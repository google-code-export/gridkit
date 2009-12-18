/*
 * File: FunctorTests.java
 * 
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.testng.annotations.Test;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.ContextsManager;
import com.oracle.coherence.patterns.command.DefaultContextsManager;
import com.oracle.coherence.patterns.functor.DefaultFunctorSubmitter;
import com.oracle.coherence.patterns.functor.FunctorSubmitter;

public class FunctorTests {
	
	@Test
	public void testCancelFunctor() throws InterruptedException, ExecutionException {
		ContextsManager contextsManager = DefaultContextsManager.getInstance();
		Identifier contextIdentifier = contextsManager.registerContext("testCancelFunctor", new TestContext());
		
		FunctorSubmitter functorSubmitter = DefaultFunctorSubmitter.getInstance();
		
		Future<Boolean> delayedResult  = functorSubmitter.submitFunctor(contextIdentifier, new DelayedFunctor());
		Future<Boolean> canceledResult = functorSubmitter.submitFunctor(contextIdentifier, new CanceledFunctor());
		
		boolean canceled = canceledResult.cancel(false);
		assert (canceled == true);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		assert(delayedResult.get() == true);
	}
	
	@Test
	public void testFailToCancelFunctor() throws InterruptedException, ExecutionException {
		ContextsManager contextsManager = DefaultContextsManager.getInstance();
		Identifier contextIdentifier = contextsManager.registerContext("testFailToCancelFunctor", new TestContext());
		
		FunctorSubmitter functorSubmitter = DefaultFunctorSubmitter.getInstance();
		
		Future<Boolean> delayedResult  = functorSubmitter.submitFunctor(contextIdentifier, new DelayedFunctor());
		
		Thread.sleep(100);
		
		boolean canceled = delayedResult.cancel(false);
		assert (canceled == false);
	}
	
	@Test
	public void testTimedGetSuccess() throws InterruptedException, ExecutionException {
		ContextsManager contextsManager = DefaultContextsManager.getInstance();
		Identifier contextIdentifier = contextsManager.registerContext("testTimedGetSuccess", new TestContext());
		
		FunctorSubmitter functorSubmitter = DefaultFunctorSubmitter.getInstance();
		
		Future<Boolean> delayedResult  = functorSubmitter.submitFunctor(contextIdentifier, new DelayedFunctor());
		
		try
			{
			boolean result = false;
			result = delayedResult.get(3, TimeUnit.SECONDS);
			
			assert(result == true);
			} catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	@Test
	public void testTimedGetFail() throws InterruptedException, ExecutionException {
		ContextsManager contextsManager = DefaultContextsManager.getInstance();
		Identifier contextIdentifier = contextsManager.registerContext("testTimedGetFail", new TestContext());
		
		FunctorSubmitter functorSubmitter = DefaultFunctorSubmitter.getInstance();
		
		Future<Boolean> delayedResult  = functorSubmitter.submitFunctor(contextIdentifier, new DelayedFunctor());
		
		try
			{
			delayedResult.get(500, TimeUnit.MILLISECONDS);
			
			assert(false);
			} catch(TimeoutException e) {
				assert(true);
			} catch(Exception e) {
				e.printStackTrace();
				assert(false);
			}
	}
}
