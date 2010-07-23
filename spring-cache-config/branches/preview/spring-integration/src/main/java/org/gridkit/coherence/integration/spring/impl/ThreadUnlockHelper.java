/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gridkit.coherence.integration.spring.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;


/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class ThreadUnlockHelper {

	private final ExecutorService blockingThreadExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {		
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("BlockingCacheOpsExecutor");
			return thread;
		}
	});
	
	private final ExecutorService springThreadExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {		
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("DeferredSpringOpsExecutor");
			return thread;
		}
	});
	
	
	private final static BlockingQueue<Event> queue = new LinkedBlockingQueue<Event>();

	private final Executor queueExecutor = new Executor() {
		
		@Override
		public void execute(Runnable command) {
			queue.add(new CallRequest(Executors.callable(command)));			
		}
	};
	
	public ThreadUnlockHelper() {
	}

	@SuppressWarnings("unchecked")
	public synchronized <V> V modalExecute(final Callable<V> callable) {
		blockingThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				FutureTask<V> future = new FutureTask<V>(callable);
				future.run();
				queue.add(new ValueEvent<V>(future));
			}
		});
		
		while(true) {
			try {
				Event evt = queue.take();
				if (evt instanceof ValueEvent) {
					queueCleanUp();
					return ((ValueEvent<V>)evt).result.get();
				}
				else {
					CallRequest req = (CallRequest) evt;
					req.run();
				}
			} catch (InterruptedException e) {
				throw new RuntimeException("Failed to invoke '" + callable + "', interrupted");
			} catch (ExecutionException e) {
				throw new RuntimeException("ExecutionException", e.getCause());
			}
		}		
	}

	private void queueCleanUp() {
		springThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				modalExecute(new Callable<Void>() {
					
					@Override
					public Void call() throws Exception {
						return null;
					}
				});
			}
		});
	}
	
	public <V> V safeExecute(Callable<V> callable) {
		SplitFuture<V> future = new SplitFuture<V>(callable, callable);
		queueExecutor.execute(future.getA());
		springThreadExecutor.execute(future.getB());

		try {
			return (V)future.get();
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException)e;
			}
			else {
				throw new RuntimeException("Derefered call failed", e);
			}
		}
	}
	
	private static interface Event {		
	}

	private static class CallRequest extends FutureTask<Object> implements Event {

		public CallRequest(Callable<Object> callable) {
			super(callable);
		}		
	}
	
	private static class ValueEvent<V> implements Event {
		final Future<V> result;
		
		public ValueEvent(Future<V> result) {
			this.result = result;
		}
	}
}
