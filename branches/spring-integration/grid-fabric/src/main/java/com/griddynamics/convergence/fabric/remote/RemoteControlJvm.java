/**
 * Copyright 2008-2009 Grid Dynamics Consulting Services, Inc.
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
package com.griddynamics.convergence.fabric.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.griddynamics.convergence.fabric.rmi.RemoteMessage;
import com.griddynamics.convergence.fabric.rmi.RmiChannel;

public class RemoteControlJvm {

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final RmiChannel channel;
	
	private boolean connected = false;
	private boolean terminated = false; 
	
	private Socket connection;
	private RmiObjectInputStream in;
	private RmiObjectOutputStream out;
	
	private ExecutorService service;
	private SlaveInterface remote;
	

	public RemoteControlJvm() {
		this(new Class[]{Remote.class});
	}

	RemoteControlJvm(String id) {
		this.channel = new RmiChannel(new MessageOut(), executor, new Class[]{Remote.class});
		this.service = new RemoteExecutionService();
	}

	@SuppressWarnings("unchecked")
	public RemoteControlJvm(Class[] remoteMarkers) {
		this.channel = new RmiChannel(new MessageOut(), executor, remoteMarkers);
		this.service = new RemoteExecutionService();
	}
	
	public String getJvmStartForClient(String host, int port) {
		return RemoteControlJvmAgent.class.getName() + " tcp-client " + host + " " + port ;
	}

	public String getJvmStartForServer(int port) {
		return RemoteControlJvmAgent.class.getName() + " tcp-server " + " " + port ;
	}

	public ExecutorService getExecutionService() {
		return service;
	}
	
	public synchronized boolean connect(Socket socket) throws InterruptedException {
		if (this.connection != null) {
			throw new IllegalStateException();
		}
		connection = socket;
		if (socket != null) {
			try {
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				out = new RmiObjectOutputStream(os);
				
				MasterInterface master = new MasterInterface() {
					public void ping() throws RemoteException {
						// do nothing
					}
				};
				channel.exportObject(MasterInterface.class, master);
				synchronized(out) {					
					out.writeUnshared(master);
					out.reset();
				}

				// important create out stream first!
				in = new RmiObjectInputStream(is);
				remote = (SlaveInterface) in.readObject();
				connected = true;
				
				Thread thread = new Thread() {
					@Override
					public void run() {
						
						try {
							while(true) {
								Object message = in.readObject();
								if (message != null) {
									if ("close".equals(message)) {
										shutdown();
									}
									else {
										channel.handleRemoteMessage((RemoteMessage) message);
									}
								}
							}
						}
						catch(Exception e) {
							if (e instanceof SocketException && connection.isClosed()) {
								System.err.println("RMI socket closed, remote [" + connection.getRemoteSocketAddress().toString() + "]");
							}
							else {
								System.err.println("RMI stream read exception [" + connection.getRemoteSocketAddress().toString() + "]");
								e.printStackTrace();
							}
							shutdown();
						}
					}
				};
				
				thread.setName("RMI-receiver-" + socket.getRemoteSocketAddress());
				thread.start();
				
			} catch (Exception e) {
				try {
					connection.close();
				} catch (IOException e1) {
					// ignore
				}
				e.printStackTrace();
			}
		}
		return socket != null;
	}
	
	public synchronized boolean isConnected() {
		return connected && !terminated;
	}
	
	public synchronized void shutdown() {
		if (terminated) {
			return;
		}
		terminated = true;
		try {
			out.writeUnshared("close");
		}
		catch(Exception e) {
			// ignore
		}
		try {
			connection.close();
		} catch (IOException e) {
			// ignore
		}
		try {
			service.shutdown();
		}
		catch(Exception e) {
			// ignore
		}
		try {
			channel.close();
		}
		catch(Exception e) {
			// ignore
		}
		try {
			executor.shutdown();		
		}
		catch(Exception e) {
			// ignore
		}
	}
	
	private class RmiObjectInputStream extends ObjectInputStream {
		
		public RmiObjectInputStream(InputStream in) throws IOException {
			super(in);
			enableResolveObject(true);
		}

		@Override
		protected Object resolveObject(Object obj) throws IOException {
			return channel.streamResolveObject(obj);
		}
	}

	private class RmiObjectOutputStream extends ObjectOutputStream {

		public RmiObjectOutputStream(OutputStream in) throws IOException {
			super(in);
			enableReplaceObject(true);
		}

		@Override
		protected Object replaceObject(Object obj) throws IOException {
			return channel.streamReplaceObject(obj);
		}
	}
	
	private class MessageOut implements RmiChannel.OutputChannel {
		public void send(RemoteMessage message) throws IOException {
			try {
				synchronized(out) {
					out.writeUnshared(message);
					out.reset();
				}
			} catch (IOException e) {
				shutdown();
			}
		}
	}

	private class RemoteExecutionService extends AbstractExecutorService {
		
		private final ExecutorService threadPool = Executors.newCachedThreadPool();
		
		@Override
		public <T> Future<T> submit(Runnable task, T result) {
			return submit(new CallableRunnableWrapper<T>(task, result));
		}

		@Override
		public Future<?> submit(Runnable task) {
			return submit(new CallableRunnableWrapper<Object>(task, null));
		}

		@Override
		public <T> Future<T> submit(Callable<T> task) {
			task = wrap(task);
			return threadPool.submit(task);
		}

		public void execute(Runnable command) {
			submit(new CallableRunnableWrapper<Object>(command, null));
		}

		private <T> Callable<T> wrap(final Callable<T> task) {
			return new Callable<T>() {

				public T call() throws Exception {
					return remote.remoteCall(task);
				}
			};
		}

		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		public boolean isShutdown() {
			throw new UnsupportedOperationException();
		}

		public boolean isTerminated() {
			throw new UnsupportedOperationException();
		}

		public void shutdown() {
			RemoteControlJvm.this.shutdown();
		}

		public List<Runnable> shutdownNow() {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class CallableRunnableWrapper<T> implements Callable<T>, Serializable {

		private static final long serialVersionUID = 1L;

		private Runnable runnable;
		private T result;
		
		public CallableRunnableWrapper() {};
		
		public CallableRunnableWrapper(Runnable runnable, T result) {
			this.runnable = runnable;
			this.result = result;
		}

		public T call() throws Exception {
			runnable.run();
			return result;
		}
	}
	
	public static interface MasterInterface extends Remote {
		public void ping() throws RemoteException;
	}

	public static interface SlaveInterface extends Remote {
		public <T> T remoteCall(Callable<T> callable) throws RemoteException;
		public void shutdown();
	}
}
