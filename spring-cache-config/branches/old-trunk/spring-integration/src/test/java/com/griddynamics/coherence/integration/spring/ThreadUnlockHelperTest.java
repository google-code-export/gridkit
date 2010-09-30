package com.griddynamics.coherence.integration.spring;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

public class ThreadUnlockHelperTest {
	
	
	@Test
	public void testSingleCall() {
		
		final Object lock = new Object();
		final ExecutorService edt = Executors.newFixedThreadPool(1);
		final ThreadUnlockHelper helper = new ThreadUnlockHelper();

		final Callable<String> synchroCall = new Callable<String>() {
			@Override
			public String call() throws Exception {
				synchronized(lock) {
					return "Hallo world!";
				}
			}
		};
		
		final Callable<String> handlerCall = new Callable<String>() {
			@Override
			public String call() throws Exception {
				return helper.invoke(synchroCall);
			}
		};
		
		Callable<String> blockingCall = new Callable<String>() {
			@Override
			public String call() throws Exception {
				String txt = edt.submit(handlerCall).get();
				return txt;
			}			
		};
		
		String txt = helper.derefInvoke(blockingCall);
		
		Assert.assertEquals(txt, "Hallo world!");
	}

}
