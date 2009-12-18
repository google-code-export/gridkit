/*
 * File: FunctorPatternExample.java
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

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.command.ContextsManager;
import com.oracle.coherence.patterns.command.DefaultContextsManager;
import com.oracle.coherence.patterns.functor.DefaultFunctorSubmitter;
import com.oracle.coherence.patterns.functor.FunctorSubmitter;
import com.tangosol.net.CacheFactory;

public class FunctorPatternExample {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws ExecutionException 
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws InterruptedException, ExecutionException {

		
		ContextsManager contextsManager = DefaultContextsManager.getInstance();
		Identifier contextIdentifier = contextsManager.registerContext("myCounter", new Counter(0));
		
		FunctorSubmitter functorSubmitter = DefaultFunctorSubmitter.getInstance();
		
		functorSubmitter.submitCommand(contextIdentifier, new LoggingCommand("Commenced", 0));

		for (int i = 0; i < 50; i++) {
			Future<Long> future = functorSubmitter.submitFunctor(contextIdentifier, new NextValueFunctor());
			System.out.println(future.get());
		}
		
		functorSubmitter.submitCommand(contextIdentifier, new LoggingCommand("Completed", 0));
		
		Thread.sleep(2000);
		
		Counter counter = (Counter)contextsManager.getContext(contextIdentifier);
		System.out.println(counter);
		
		CacheFactory.shutdown();
	}

}
