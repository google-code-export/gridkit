package com.griddynamics.coherence.integration.spring;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
class ThreadUnlockHelper implements CallableExecutor {

	private final static ExecutorService DEREF_THREAD_EXECUTOR = Executors.newFixedThreadPool(1, new ThreadFactory() {		
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("DereferedEnsureCacheExecutor");
			return thread;
		}
	});
	
	private final BlockingQueue<Event> queue = new LinkedBlockingQueue<Event>();
	
	public ThreadUnlockHelper() {
	}

	@SuppressWarnings("unchecked")
	public synchronized <V> V derefInvoke(final Callable<V> callable) {
		DEREF_THREAD_EXECUTOR.execute(new Runnable() {
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
	
	@SuppressWarnings("unchecked")
	public <V> V invoke(Callable<V> callable) {
		CallRequest request = new CallRequest((Callable)callable);
		queue.add(request);
		try {
			return (V)request.get();
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
