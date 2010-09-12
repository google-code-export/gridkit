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
package org.gridkit.coherence.utils.classloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.junit.Ignore;

/**
 *	@author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@Ignore
public class Isolate {
	
	private String name;
	private Thread isolatedThread;
	private ClassLoader cl;
	
	private BlockingQueue<String> queue = new SynchronousQueue<String>();
	
	public Isolate(String name, String... packages) {		
		this.name = name;
		this.cl = new IsolatedClassloader(getClass().getClassLoader(), packages);
	}
	
	public synchronized void start() {
		isolatedThread = new Thread(new Runner());
		isolatedThread.setName("Isolate-" + name);
		isolatedThread.setDaemon(true);
		isolatedThread.start();		
	}
	
	public void submit(String classname) {
		try {
			queue.put(classname);
			queue.put(Nop.class.getName());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		try {
			queue.put("");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cl = null;
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return cl.loadClass(name);
	}
	
	public ClassLoader getClassLoader() {
		return cl;
	}
	
	private class Runner implements Runnable {

		@Override
		public void run() {
			Thread.currentThread().setContextClassLoader(cl);
			while(true) {
				try {
					String msg = queue.take();
					if (msg.length() == 0) {
						break;
					}
					else {
						Class<?> task = cl.loadClass(msg);						
						Constructor<?> c = task.getConstructors()[0];
						c.setAccessible(true);
						Runnable r = (Runnable) c.newInstance();
						r.run();
					}
				} catch (Exception e) {
					e.printStackTrace();
				};
			}
		};
		
	}
	
	public static class Nop implements Runnable {
		
		public Nop() {
		}
		
		@Override
		public void run() {
		}
	}
	
	private class IsolatedClassloader extends ClassLoader {
		
		private ClassLoader baseClassloader;
		private String[] packages;
		
		public IsolatedClassloader(ClassLoader base, String[] packages) {
			super(null);
			this.baseClassloader = base;
			this.packages = packages;
		}

		public void clearAssertionStatus() {
			baseClassloader.clearAssertionStatus();
		}

		public URL getResource(String name) {
			return baseClassloader.getResource(name);
		}

		public InputStream getResourceAsStream(String name) {
			return baseClassloader.getResourceAsStream(name);
		}

		public Enumeration<URL> getResources(String name) throws IOException {
			return baseClassloader.getResources(name);
		}

		public void setClassAssertionStatus(String className, boolean enabled) {
			baseClassloader.setClassAssertionStatus(className, enabled);
		}

		public void setDefaultAssertionStatus(boolean enabled) {
			baseClassloader.setDefaultAssertionStatus(enabled);
		}

		public void setPackageAssertionStatus(String packageName, boolean enabled) {
			baseClassloader.setPackageAssertionStatus(packageName, enabled);
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			for(String prefix: packages) {
				if (name.startsWith(prefix)) {
					return this.loadClass(name, false);
				}
			}
			Class<?> cc = baseClassloader.loadClass(name);
			return cc;
		}

		@Override
		protected Class<?> findClass(String classname) throws ClassNotFoundException {
			try {
				String path = classname.replace('.', '/').concat(".class");
				InputStream res = baseClassloader.getResourceAsStream(path);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] buf = new byte[4096];
				while(true) {
					int x = res.read(buf);
					if (x <= 0) {
						break;
					}
					else {
						bos.write(buf, 0, x);
					}
				}
				byte[] cd = bos.toByteArray();
				Class<?> baseC = baseClassloader.loadClass(classname);				
				Class<?> c = defineClass(classname, cd, 0, cd.length, baseC.getProtectionDomain());
				//System.out.println("IS-" + name + " > " + classname);
				return c;
			}
			catch(Exception e) {
				throw new ClassNotFoundException(classname);
			}
		}
	}
}
