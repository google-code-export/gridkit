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
package org.gridkit.gatling.remoting;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.googlecode.gridkit.fabric.exec.ssh.SocketHelper;
import com.googlecode.gridkit.fabric.remote.RemoteControledJvm.MasterInterface;
import com.googlecode.gridkit.fabric.remote.RemoteControledJvm.SlaveInterface;
import com.googlecode.gridkit.fabric.remoting.RemoteMessage;
import com.googlecode.gridkit.fabric.remoting.RmiChannel;
import com.googlecode.gridkit.fabric.remoting.RmiChannel.OutputChannel;


public class RemoteControledJvmAgent implements SlaveInterface, Runnable, OutputChannel {

	private Executor executor = Executors.newCachedThreadPool();
	private RmiChannel channel; 
	private RmiObjectInputStream in;
	private RmiObjectOutputStream out;
	private MasterInterface master;
	
	public RemoteControledJvmAgent(final Socket socket) throws IOException, ClassNotFoundException {
		
		InputStream pin = socket.getInputStream();
		OutputStream pout = socket.getOutputStream();
		
		out = new RmiObjectOutputStream(pout);
		
		this.channel = new RmiChannel(this, executor, new Class[]{Remote.class});
		this.channel.exportObject(SlaveInterface.class, this);
		
		synchronized(out) {
			out.writeUnshared(this);
			out.reset();
		};

		// important create out stream first!
		in = new RmiObjectInputStream(pin);
		master = (MasterInterface) in.readObject();
		executor.execute(new Runnable() {
			public void run() {
				try {
					if (socket.isClosed()) {
						System.err.println("Socket closed");
						System.exit(1);	
					}
					Thread.sleep(300);
				}
				catch(Exception e) {
					System.exit(1);
				}
			}
		});
	}

	public static void main(String[] args) {
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				System.out.println("Slave JVM is shutdown");
			}
		});
		
		try {
			Socket socket;
			if (args[0].toLowerCase().equals("tcp-client")) {
				String serverHost = args[1];
				int serverPort = Integer.parseInt(args[2]);
				socket = SocketHelper.connect(serverHost, serverPort, 5000);
				System.out.println("Controlled by " + socket.getRemoteSocketAddress());
			}
			else if (args[0].toLowerCase().equals("tcp-server")) {
				int serverPort = Integer.parseInt(args[1]);
				socket = SocketHelper.accept(serverPort, 5000);
				System.out.println("Controlled by " + socket.getRemoteSocketAddress());
			}
			else {
				System.out.println("Unknown transport " + args[0]);
				System.exit(1);
				throw new Error();
			}
			
			try {
				new RemoteControledJvmAgent(socket).run();
			} catch (SocketException e) {
				System.err.println(e);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		System.exit(1);
	}

	public <T> T remoteCall(Callable<T> callable) throws RemoteException {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new RemoteException("Invocation failed", e);
		}
	}

	public void shutdown() {
		executor.execute(new Runnable() {
			public void run() {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
				System.err.println("Shutdown request");
				System.exit(0);
			}
		});
	}

	public void run() {
		while(true) {
			try {
				Object message = in.readObject();
				if (message != null) {
					if ("close".equals(message)) {
						System.out.println("");
						try {
							channel.close();
						} catch (Exception e) {
						}
						try {
							in.close();
						} catch (Exception e) {
						}
						try {
							out.close();
						} catch (Exception e) {
						}
						break;
					}
					channel.handleRemoteMessage((RemoteMessage) message);
				}
			}
			catch (SocketException e) {
				System.err.println(e);
				break;
			}
			catch (Exception e) {
				System.err.print("Channel error");
				e.printStackTrace();
				// TODO
				throw new RuntimeException(e);
			}
		}
	}

	public void send(RemoteMessage message) throws IOException {
		synchronized(out) {
			out.writeUnshared(message);
			out.reset();
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
}
