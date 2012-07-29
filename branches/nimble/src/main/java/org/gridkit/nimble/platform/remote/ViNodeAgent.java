package org.gridkit.nimble.platform.remote;

import java.net.InetAddress;
import java.rmi.Remote;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import org.gridkit.nimble.platform.LocalAgent;
import org.gridkit.nimble.platform.RemoteAgent;
import org.gridkit.vicluster.ViNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ViNodeAgent implements RemoteAgent {

	private String uid;
	private ViNode node;
	private Set<String> tags;
	private RemoteHandle handle;
	
	public ViNodeAgent(ViNode node, Set<String> tags) {
		this.uid = UUID.randomUUID().toString();
		this.node = node;
		this.tags = tags;
		
		final String nodeId = uid;
		final Set<String> nodeTags = tags;
		
		handle = node.exec(new Callable<RemoteHandle>() {

			@Override
			public RemoteHandle call() throws Exception {
				return new RemoteAgent(nodeId, nodeTags);
			}
			
		});
	}

	@Override
	public String getId() {
		return uid;
	}

	@Override
	public Set<String> getLabels() {
		return new HashSet<String>(tags);
	}

	@Override
	public InetAddress getInetAddress() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getPid() {
		return 0;
	}

	@Override
	public void shutdown(boolean hard) {
		node.shutdown();
	}

	@Override
	public <T> Future<T> invoke(final Invocable<T> invocable) {
		final RemoteHandle handle = this.handle;
		return node.submit(new Callable<T>() {
			@Override
			public T call() throws Exception {				
				return handle.exec(invocable);
			}
		});
	}
	
	private static interface RemoteHandle extends Remote {		
		public <T> T exec(Invocable<T> task) throws Exception;		
	}
	
	private static class RemoteAgent implements LocalAgent, RemoteHandle {

		private String uid; 
		private Set<String> labels;
		private ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();
		
		public RemoteAgent(String uid, Set<String> labels) {
			this.uid = uid;
			this.labels = labels;
		}

		@Override
		public ConcurrentMap<String, Object> getAttributesMap() {
			return attributes;
		}

		@Override
		public String getId() {
			return uid;
		}

		@Override
		public Set<String> getLabels() {
			return labels;
		}

		@Override
		public InetAddress getInetAddress() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getPid() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void shutdown(boolean hard) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> Future<T> invoke(Invocable<T> invocable) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T exec(Invocable<T> task) throws Exception {
			return task.invoke(this);
		}

		@Override
		public long currentTimeMillis() {
			return System.currentTimeMillis();
		}

		@Override
		public long currentTimeNanos() {
			return System.nanoTime();
		}

		@Override
		public Logger getLogger(String name) {
			return LoggerFactory.getLogger(name);
		}
	}
}
