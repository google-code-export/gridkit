package org.gridkit.util.vicontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ViGroup implements ViHost {

	private ViHostConfig config = new ViHostConfig();
	private List<ViHost> hosts = new ArrayList<ViHost>();
	private boolean shutdown = false;
	
	private void checkActive() {
		if (shutdown) {
			throw new IllegalStateException("Group is shutdown");
		}
	}

	private void checkExecutable() {
		checkActive();
		if (hosts.isEmpty()) {
			throw new IllegalStateException("No hosts in this group");
		}
	}
	
	public synchronized void addHost(ViHost host) {
		checkActive();
		hosts.add(host);
		config.apply(host);
	}
	
	@Override
	public synchronized void setProp(String propName, String value) {
		checkActive();
		config.setProp(propName, value);
		for(ViHost vh: hosts) {
			vh.setProp(propName, value);
		}
	}
	
	@Override
	public synchronized void setProps(Map<String, String> props) {
		checkActive();
		config.setProps(props);
		for(ViHost vh: hosts) {
			vh.setProps(props);
		}
	}
	
	@Override
	public synchronized void addStartupHook(String name, Runnable hook, boolean override) {
		checkActive();
		config.addStartupHook(name, hook, override);
		for(ViHost vh: hosts) {
			vh.addStartupHook(name, hook, override);
		}
	}
	
	@Override
	public synchronized void addShutdownHook(String name, Runnable hook, boolean override) {
		checkActive();
		config.addShutdownHook(name, hook, override);
		for(ViHost vh: hosts) {
			vh.addShutdownHook(name, hook, override);
		}
	}
	
	@Override
	public synchronized void suspend() {
		checkActive();
		
	}

	@Override
	public synchronized void resume() {
		checkActive();
		
	}

	@Override
	public synchronized void shutdown() {
		if (!shutdown) {
			for(ViHost host: hosts) {
				host.shutdown();
			}			
			shutdown = true;
		}		
	}

	@Override
	public synchronized void kill() {
		if (!shutdown) {
			for(ViHost host: hosts) {
				host.kill();
			}			
			shutdown = true;
		}		
	}

	@Override
	public synchronized void exec(Runnable task) {
		MassExec.waitAll(massSubmit(task));		
	}
	
	@Override
	public synchronized void exec(VoidCallable task) {
		MassExec.waitAll(massSubmit(task));		
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized <T> T exec(Callable<T> task) {
		return (T) MassExec.waitAll((List)massSubmit(task)).get(0);		
	}
	
	@Override
	public synchronized Future<Void> submit(Runnable task) {
		return new GroupFuture<Void>(massSubmit(task));
	}
	
	@Override
	public synchronized Future<Void> submit(VoidCallable task) {
		return new GroupFuture<Void>(massSubmit(task));
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized <T> Future<? super T> submit(Callable<T> task) {
		return new GroupFuture(massSubmit(task));
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized <T> List<? super T> massExec(Callable<T> task) {
		return MassExec.waitAll((List)massSubmit(task));
	}
	
	@Override
	public synchronized List<Future<Void>> massSubmit(Runnable task) {
		checkExecutable();
		List<Future<Void>> results = new ArrayList<Future<Void>>();
		for(ViHost host: hosts) {
			results.addAll(host.massSubmit(task));
		}
		return results;
	}
	
	@Override
	public synchronized List<Future<Void>> massSubmit(VoidCallable task) {
		checkExecutable();
		List<Future<Void>> results = new ArrayList<Future<Void>>();
		for(ViHost host: hosts) {
			results.addAll(host.massSubmit(task));
		}
		return results;
	}
	
	@Override
	public synchronized <T> List<Future<? super T>> massSubmit(Callable<T> task) {
		checkExecutable();
		List<Future<? super T>> results = new ArrayList<Future<? super T>>();
		for(ViHost host: hosts) {
			results.addAll(host.massSubmit(task));
		}
		return results;
	}
	
	private static class GroupFuture<T> implements Future<T> {
		
		private List<Future<T>> futures;
		
		public GroupFuture(List<Future<T>> futures) {
			this.futures = futures;
		}

		@Override
		public boolean cancel(boolean mayInterrupt) {
			for(Future<T> future : futures) {
				try {
					future.cancel(mayInterrupt);
				}
				catch(RuntimeException e) {
					// ignore;
				}
			}
			return true;
		}

		@Override
		public boolean isCancelled() {
			// TODO implement
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isDone() {
			// TODO implement
			throw new UnsupportedOperationException();
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public T get() throws InterruptedException, ExecutionException {
			return (T) MassExec.waitAll((List)futures).get(0);
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			// TODO implement
			throw new UnsupportedOperationException();
		}
	}
}
