package org.gridkit.nimble.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ListenableFuture;

public class DumbListenableFuture<V> extends Thread implements ListenableFuture<V> {
	
	private Future<V> future;
	private List<Runnable> callbacks = new ArrayList<Runnable>();
	
	public static <X> ListenableFuture<X> wrap(String name, Future<X> f) {
		return new DumbListenableFuture<X>(name, f);
	}
	
	public DumbListenableFuture(String name, Future<V> future) {
		this.setName(name);
		this.future = future;
		start();
	}

	@Override
	public void run() {
		try {
			future.get();
			synchronized(this) {
				for(Runnable callback: callbacks) {
					callback.run();
				}
				callbacks = null;
			}
		} catch (Exception e) {
			synchronized(this) {
				for(Runnable callback: callbacks) {
					callback.run();
				}
				callbacks = null;
			}
			// ignore
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		return future.get();
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException,	ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}

	@Override
	public synchronized void addListener(final Runnable listener, final Executor executor) {
		if (future.isDone()) {
			executor.execute(listener);
		}
		else {
			callbacks.add(new Runnable() {
				@Override
				public void run() {
					executor.execute(listener);
				}
			});
		}
	}
}
