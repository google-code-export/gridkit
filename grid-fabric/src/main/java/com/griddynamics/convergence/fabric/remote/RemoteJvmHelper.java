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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.griddynamics.convergence.demo.utils.cluster.ssh.SocketHelper;
import com.griddynamics.convergence.demo.utils.exec.ExecCommand;
import com.griddynamics.convergence.demo.utils.exec.ProcessExecutor;
import com.griddynamics.convergence.demo.utils.io.LineDumper;

public class RemoteJvmHelper {
	
	private static ExecutorService EXECUTORS = Executors.newCachedThreadPool();
	
	public static ExecutorService createRemoteExecutor(ProcessExecutor pexec, String outputPrefix, ExecCommand javaCmd, InetSocketAddress rserver) throws IOException, InterruptedException {
		return createRemoteExecutor(pexec, outputPrefix, javaCmd, rserver, 0);
	}

	public static ExecutorService createRemoteExecutor(ProcessExecutor pexec, String outputPrefix, ExecCommand javaCmd, InetSocketAddress rserver, long timeout) throws IOException, InterruptedException {
		return createRemoteExecutor(pexec, outputPrefix, javaCmd, rserver, timeout, false);
	}

	public static ExecutorService createRemoteExecutor(ProcessExecutor pexec, final String outputPrefix, ExecCommand javaCmd, InetSocketAddress rserver, long timeout, boolean shutdownWhenExit) throws IOException, InterruptedException {
		
		RemoteControlJvm remoteJvm = new RemoteControlJvm();
		javaCmd = javaCmd.clone();
		javaCmd.addMultipleArg(remoteJvm.getJvmStartForServer(rserver.getPort()));
		
		final Process jvm = pexec.execute(javaCmd);
		new LineDumper(outputPrefix, jvm.getInputStream(), System.out);
		new LineDumper(outputPrefix, jvm.getErrorStream(), System.out);
		if (shutdownWhenExit) {
			EXECUTORS.execute(new ShutdownHook(jvm, outputPrefix));
		}
		
		Socket socket;
		if (timeout == 0) {
			socket = SocketHelper.connect(rserver.getAddress().getHostName(), rserver.getPort(), timeout);
		}
		else {
			socket = SocketHelper.connect(rserver.getAddress().getHostName(), rserver.getPort());
		};
		
		remoteJvm.connect(socket);
		
		return new ProcessExecutionService(jvm, remoteJvm.getExecutionService());
	}

	public static ExecutorService createRemoteExecutor(ProcessExecutor pexec, final String outputPrefix, ExecCommand javaCmd) throws IOException, InterruptedException {
		return createRemoteExecutor(pexec, outputPrefix, javaCmd, 0);
	}

	public static ExecutorService createRemoteExecutor(ProcessExecutor pexec, final String outputPrefix, ExecCommand javaCmd, long timeout) throws IOException, InterruptedException {
		return createRemoteExecutor(pexec, outputPrefix, javaCmd, timeout, false);
	}

	public static ExecutorService createRemoteExecutor(ProcessExecutor pexec, final String outputPrefix, ExecCommand javaCmd, long timeout, boolean shutdownWhenExit) throws IOException, InterruptedException {
		
		ServerSocket socket = new ServerSocket();
		socket.bind(new InetSocketAddress(0));
		int port = socket.getLocalPort();
		
		RemoteControlJvm remoteJvm = new RemoteControlJvm();
		javaCmd = javaCmd.clone();
		javaCmd.addMultipleArg(remoteJvm.getJvmStartForClient(InetAddress.getLocalHost().getHostAddress(), port));
		
		final Process jvm = pexec.execute(javaCmd);
		new LineDumper(outputPrefix, jvm.getInputStream(), System.out);
		new LineDumper(outputPrefix, jvm.getErrorStream(), System.out);
		if (shutdownWhenExit) {
			EXECUTORS.execute(new ShutdownHook(jvm, outputPrefix));
		}
		
		socket.setSoTimeout((int) timeout);
		Socket connection = socket.accept();
		if (connection == null) {
			try {
				jvm.destroy();
			}
			catch(Exception e) {
				// ignore
			}
			throw new SocketTimeoutException();
		}
		
		remoteJvm.connect(connection);
		socket.close();
		
		return new ProcessExecutionService(jvm, remoteJvm.getExecutionService());
	}

	private static final class ShutdownHook implements Runnable {
		private final Process jvm;
		private final String outputPrefix;

		private ShutdownHook(Process jvm, String outputPrefix) {
			this.jvm = jvm;
			this.outputPrefix = outputPrefix;
		}

		public void run() {
			while(true) {
				try {
					int status = jvm.waitFor();
					System.err.println(outputPrefix + " JVM has been terminated (" + status + ")");
					System.err.println("Demo shutdown");
					Thread.sleep(1000); // some time to dump logs
					System.exit(1);
					break;
				} catch (InterruptedException e) {
					continue;
				}
			}
		}
	}

	private static class ProcessExecutionService implements ExecutorService {
		
		private final Process process;
		private final ExecutorService executor;

		public ProcessExecutionService(Process process, ExecutorService executor) {
			this.process = process;
			this.executor = executor;
		}

		public boolean awaitTermination(long timeout, TimeUnit unit)
				throws InterruptedException {
			return executor.awaitTermination(timeout, unit);
		}

		public void execute(Runnable command) {
			executor.execute(command);
		}

		public <T> List<Future<T>> invokeAll(
				Collection<? extends Callable<T>> tasks, long timeout,
				TimeUnit unit) throws InterruptedException {
			return executor.invokeAll(tasks, timeout, unit);
		}

		public <T> List<Future<T>> invokeAll(
				Collection<? extends Callable<T>> tasks)
				throws InterruptedException {
			return executor.invokeAll(tasks);
		}

		public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
				long timeout, TimeUnit unit) throws InterruptedException,
				ExecutionException, TimeoutException {
			return executor.invokeAny(tasks, timeout, unit);
		}

		public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
				throws InterruptedException, ExecutionException {
			return executor.invokeAny(tasks);
		}

		public boolean isShutdown() {
			return executor.isShutdown();
		}

		public boolean isTerminated() {
			return executor.isTerminated();
		}

		public void shutdown() {
			executor.shutdown();
			try {
				int result = process.waitFor();
			} catch (InterruptedException e) {
				// ignore
			}
		}

		public List<Runnable> shutdownNow() {
			return executor.shutdownNow();
		}

		public <T> Future<T> submit(Callable<T> task) {
			return executor.submit(task);
		}

		public <T> Future<T> submit(Runnable task, T result) {
			return executor.submit(task, result);
		}

		public Future<?> submit(Runnable task) {
			return executor.submit(task);
		}
	}
}
