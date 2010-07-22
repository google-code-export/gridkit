package org.gridkit.coherence.integration.spring.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class SplitFuture<V> implements Future<V> {

	private FutureTask<V> task;
	private Runnable a;
	private Runnable b;

	private V result;
	private Exception exception;
	
	public SplitFuture(Callable<V> a, Callable<V> b) {
		this.task = new FutureTask<V>(new Callable<V>() {
			@Override
			public V call() throws Exception {
				return innerCall();
			}
		});
		this.a = wrap(a);
		this.b = wrap(b);
	}
	
	public Runnable getA() {
		return a;
	}
	
	public Runnable getB() {
		return b;
	}

	private Runnable wrap(final Callable<V> callable) {
		return new Runnable() {
			@Override
			public void run() {
				if (task.isCancelled() || task.isDone()) {
					// ignore					
				}
				else {						
					V result = null;
					Exception exception = null;
					
					try {
						result = callable.call();
					}
					catch(Exception e) {
						exception = e;
					}
					synchronized(SplitFuture.this) {
						SplitFuture.this.result = result;
						SplitFuture.this.exception = exception;
						task.run();
					}
				}
			}
		};
	}
	
	private V innerCall() throws Exception {
		if (exception != null) {
			throw exception;			
		}
		else {
			return result;
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return task.cancel(false);
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		return task.get();
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return task.get(timeout, unit);
	}

	@Override
	public boolean isCancelled() {
		return task.isCancelled();
	}

	@Override
	public boolean isDone() {
		return task.isDone();
	}	
}
