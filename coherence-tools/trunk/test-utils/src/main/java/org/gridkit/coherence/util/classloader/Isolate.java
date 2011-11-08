/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
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
package org.gridkit.coherence.util.classloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;

import org.junit.Ignore;

/**
 *	@author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@Ignore
public class Isolate {
	
	private static final InheritableThreadLocal<Isolate> ISOLATE = new InheritableThreadLocal<Isolate>();
	
	private static PrintStream rootOut;
	private static PrintStream rootErr;
	private static Properties rootProperties;
	
	static {
		
		System.err.println("Installing java.lang.System multiplexor");
		
		rootOut = System.out;
		rootErr = System.err;
		rootProperties = System.getProperties();
		
		PrintStream mOut = new PrintStreamMultiplexor() {
			@Override
			protected PrintStream resolve() {
				Isolate i = ISOLATE.get();
				if (i == null) {
					return rootOut;
				}
				else {
					return i.stdOut;
				}
			}
		};

		PrintStream mErr = new PrintStreamMultiplexor() {
			@Override
			protected PrintStream resolve() {
				Isolate i = ISOLATE.get();
				if (i == null) {
					return rootErr;
				}
				else {
					return i.stdErr;
				}
			}
		};
		
		@SuppressWarnings("serial")
		Properties mProps = new PropertiesMultiplexor() {
			@Override
			protected Properties resolve() {
				Isolate i = ISOLATE.get();
				if (i == null) {
					return rootProperties;
				}
				else {
					return i.sysProps;
				}
			}
		};
		
		System.setOut(mOut);
		System.setErr(mErr);
		System.setProperties(mProps);
	}
	
	private String name;
	private Thread isolatedThread;
	private IsolatedClassloader cl;
	
	private PrintStream stdOut;
	private PrintStream stdErr;
	private Properties sysProps;
	
	private BlockingQueue<WorkUnit> queue = new SynchronousQueue<WorkUnit>();
	
	public Isolate(String name, String... packages) {		
		this.name = name;
		this.cl = new IsolatedClassloader(getClass().getClassLoader(), packages);
		
		sysProps = new Properties();
		sysProps.putAll(System.getProperties());
		
		stdOut = new PrintStream(new WrapperPrintStream("[" + name + "] ", rootOut));
		stdErr = new PrintStream(new WrapperPrintStream("[" + name + "] ", rootErr));
	}
	
	public synchronized void start() {
		isolatedThread = new Thread(new Runner());
		isolatedThread.setName("Isolate-" + name);
		isolatedThread.setDaemon(true);
		isolatedThread.start();		
	}
	
	public void exclude(Class<?>... excludes) {
		cl.exclude(excludes);
	}

	public void removeFromClasspath(URL basePath) {
		cl.removeFromClasspath(basePath);
	}

	public void addToClasspath(URL path) {
		cl.addToClasspath(path);
	}

	public void submit(Class<?> clazz, Object... constructorArgs) {
		try {
			queue.put(new ClassWorkUnit(clazz.getName(), constructorArgs));
			queue.put(NOP);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void exec(Runnable task) {
		try {
			CallableWorkUnit wu = new CallableWorkUnit((Runnable) convertIn(task));
			queue.put(wu);
			queue.put(NOP);
			wu.future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			AnyThrow.throwUncheked(e.getCause());
		}
	}

	@SuppressWarnings("unchecked")
	public <V> V exec(Callable<V> task) {
		CallableWorkUnit<V> future = new CallableWorkUnit<V>((Callable<V>) convertIn(task));
		try {			
			queue.put(future);
			queue.put(NOP);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}		
		Object res; 
		try {
			res = future.future.get();
			return (V) convertOut(res);
		}
		catch(Throwable e) {
			AnyThrow.throwUncheked((Throwable)convertOut(e));
			return null;
		}
	}
	
	protected <T> Object convertIn(T obj) {
		return fromBytes(toBytes(obj), cl);
	}

	protected Object convertOut(Object obj) {
		return fromBytes(toBytes(obj), cl.getParent());
	}
	
	public void stop() {
		try {
			queue.put(STOP);
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

	private interface WorkUnit {		
		public void exec() throws Exception;
	}
	
	private static StopMarker STOP = new StopMarker();

	private static class StopMarker implements WorkUnit {
		@Override
		public void exec() {
			throw new UnsupportedOperationException();
		}
	}
	
	private static Nop NOP = new Nop();
	
	private static class Nop implements WorkUnit {
		@Override
		public void exec() {
			// nop
		}
	}
	
	private static class ClassWorkUnit implements WorkUnit {
		final String className;
		final Object[] constructorArgs;
		
		public ClassWorkUnit(String className, Object... constructorArgs) {
			this.className = className;
			this.constructorArgs = constructorArgs;
		}

		@Override
		public void exec() throws Exception {
			Class<?> task = Thread.currentThread().getContextClassLoader().loadClass(className);						
			Constructor<?> c = task.getConstructors()[0];
			c.setAccessible(true);
			Runnable r = (Runnable) c.newInstance(constructorArgs);
			r.run();
		}
	}

	private static byte[] toBytes(Object x) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(x);
			oos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}					
	}
	
	@SuppressWarnings("unused")
	private static Object fromBytes(byte[] serialized) {
		return fromBytes(serialized, Thread.currentThread().getContextClassLoader());
	}

	private static Object fromBytes(byte[] serialized, final ClassLoader cl) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialized)) {
				@Override
				protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
					String name = desc.getName();
					try {
					    return Class.forName(name, false, cl);
					} catch (ClassNotFoundException ex) {
						return super.resolveClass(desc);
					}
				}				
			};
			return ois.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	private static class CallableWorkUnit<V> implements WorkUnit {

		final FutureTask<V> future;
		
		public CallableWorkUnit(Callable<V> x) {			
			future = new FutureTask<V>(x);
		}

		public CallableWorkUnit(Runnable x) {			
			future = new FutureTask<V>(x,(V) null);
		}

		@Override
		public void exec() throws Exception {
			future.run();
		}
	}
	
	private class Runner implements Runnable {

		@Override
		public void run() {
			Thread.currentThread().setContextClassLoader(cl);
			ISOLATE.set(Isolate.this);
			
			while(true) {
				try {
					WorkUnit unit = queue.take();
					if (unit instanceof StopMarker) {
						break;
					}
					else {
						unit.exec();
					}
				} catch (Exception e) {
					e.printStackTrace();
				};
			}
		};
		
	}
	
	private class IsolatedClassloader extends ClassLoader {
		
		private ClassLoader baseClassloader;
		private String[] packages;
		private Set<String> excludes;
		
		private Collection<String> forbidenPaths = new ArrayList<String>();
		private Collection<URL> externalPaths = new ArrayList<URL>();
		private URLClassLoader cpExtention;
		
		IsolatedClassloader(ClassLoader base, String[] packages) {
			super(null);			
			this.baseClassloader = base;
			this.packages = packages;
			this.excludes = new HashSet<String>();
		}
		
		public void exclude(Class<?>... excludedClasses) {
			for (Class<?> clazz : excludedClasses) {
				excludes.add(clazz.getCanonicalName());
			}
		}
		
		public void removeFromClasspath(URL basePath) {
			forbidenPaths.add(basePath.toString());			
		}
		
		public synchronized void addToClasspath(URL path) {
			externalPaths.add(path);
			cpExtention = null;
		}
		
		public void clearAssertionStatus() {
			baseClassloader.clearAssertionStatus();
		}

		public synchronized URL getResource(String name) {
			if (cpExtention == null) {
				cpExtention = new URLClassLoader(externalPaths.toArray(new URL[0]));
			}
			URL r = cpExtention.findResource(name);
			if (r != null) {
				return r;
			}
			r = baseClassloader.getResource(name);
			if (isForbiden(r)) {
				return null;
			}
			else {
				return r;
			}
		}

		public synchronized Enumeration<URL> getResources(String name) throws IOException {
			if (cpExtention == null) {
				cpExtention = new URLClassLoader(externalPaths.toArray(new URL[0]));
			}
			Vector<URL> result = new Vector<URL>();
			// TODO my have several names
			URL r = cpExtention.findResource(name);
			if (r != null) {
				result.add(r);
			}
			
			Enumeration<URL> en = baseClassloader.getResources(name);
			
			while(en.hasMoreElements()) {
				r = en.nextElement();
				if (!isForbiden(r)) {
					result.add(r);
				}
			}
			
			return result.elements();
		}

		private boolean isForbiden(URL r) {
			if (!forbidenPaths.isEmpty()) {
				String s = r.toString();
				for(String path: forbidenPaths) {
					if (s.startsWith(path)) {
						return true;
					}
				}
			}
			return false;
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
			if (!excludes.contains(name)) {
				for(String prefix: packages) {
					if (name.startsWith(prefix)) {
						return super.loadClass(name, false);
					}
				}
			}
			Class<?> cc = baseClassloader.loadClass(name);
			return cc;
		}

		@SuppressWarnings("unused")
		private byte[] asBytes(InputStream is) {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] buf = new byte[4 << 10];
				while(true) {
					int n = is.read(buf);
					if (n < 0) {
						return bos.toByteArray();
					}
					else if (n != 0) {
						bos.write(buf, 0, n);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
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
	
	public static class AnyThrow {

	    public static void throwUncheked(Throwable e) {
	        AnyThrow.<RuntimeException>throwAny(e);
	    }
	   
	    @SuppressWarnings("unchecked")
	    private static <E extends Throwable> void throwAny(Throwable e) throws E {
	        throw (E)e;
	    }
	}
	
	private class WrapperPrintStream extends FilterOutputStream {

		private String prefix;
		private boolean startOfLine;
		private PrintStream printStream;
		
		public WrapperPrintStream(String prefix, PrintStream printStream) {
			super(printStream);
			this.prefix = prefix;
			this.startOfLine = true;
			this.printStream = printStream;
		}
		
		@Override
		public synchronized void write(int c) throws IOException {
			synchronized(printStream) {
				checkNewLine();
				if (c == '\n') {
					startOfLine = true;
				}
				super.write(c);
			}
		}

		private void checkNewLine() {
			if (startOfLine) {
				printStream.append(prefix);
				startOfLine = false;
			}
		}
	
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			synchronized(printStream) {
				checkNewLine();
				for (int i = 0; i != len; ++i) {
					if (b[off + i] == '\n') {
						writeByChars(b, off, len);
						return;
					}
				}
				super.write(b, off, len);
			}
		}

		private void writeByChars(byte[] cbuf, int off, int len) throws IOException {
			for (int i = 0; i != len; ++i) {
				write(cbuf[off + i]);
			}
		}

		@Override
		public void close() throws IOException {
			super.flush();
		}
	}
	
	static abstract class PrintStreamMultiplexor extends PrintStream {
		
		protected abstract PrintStream resolve();
		
		public PrintStreamMultiplexor() {
			super(new ByteArrayOutputStream(8));
		}
		
		public int hashCode() {
			return resolve().hashCode();
		}
		public void write(byte[] b) throws IOException {
			resolve().write(b);
		}
		public boolean equals(Object obj) {
			return resolve().equals(obj);
		}
		public String toString() {
			return resolve().toString();
		}
		public void flush() {
			resolve().flush();
		}
		public void close() {
			resolve().close();
		}
		public boolean checkError() {
			return resolve().checkError();
		}
		public void write(int b) {
			resolve().write(b);
		}
		public void write(byte[] buf, int off, int len) {
			resolve().write(buf, off, len);
		}
		public void print(boolean b) {
			resolve().print(b);
		}
		public void print(char c) {
			resolve().print(c);
		}
		public void print(int i) {
			resolve().print(i);
		}
		public void print(long l) {
			resolve().print(l);
		}
		public void print(float f) {
			resolve().print(f);
		}
		public void print(double d) {
			resolve().print(d);
		}
		public void print(char[] s) {
			resolve().print(s);
		}
		public void print(String s) {
			resolve().print(s);
		}
		public void print(Object obj) {
			resolve().print(obj);
		}
		public void println() {
			resolve().println();
		}
		public void println(boolean x) {
			resolve().println(x);
		}
		public void println(char x) {
			resolve().println(x);
		}
		public void println(int x) {
			resolve().println(x);
		}
		public void println(long x) {
			resolve().println(x);
		}
		public void println(float x) {
			resolve().println(x);
		}
		public void println(double x) {
			resolve().println(x);
		}
		public void println(char[] x) {
			resolve().println(x);
		}
		public void println(String x) {
			resolve().println(x);
		}
		public void println(Object x) {
			resolve().println(x);
		}
		public PrintStream printf(String format, Object... args) {
			return resolve().printf(format, args);
		}
		public PrintStream printf(Locale l, String format, Object... args) {
			return resolve().printf(l, format, args);
		}
		public PrintStream format(String format, Object... args) {
			return resolve().format(format, args);
		}
		public PrintStream format(Locale l, String format, Object... args) {
			return resolve().format(l, format, args);
		}
		public PrintStream append(CharSequence csq) {
			return resolve().append(csq);
		}
		public PrintStream append(CharSequence csq, int start, int end) {
			return resolve().append(csq, start, end);
		}
		public PrintStream append(char c) {
			return resolve().append(c);
		}
	}
	
	@SuppressWarnings("serial")
	static abstract class PropertiesMultiplexor extends Properties {
		
		protected abstract Properties resolve();

		public Object setProperty(String key, String value) {
			return resolve().setProperty(key, value);
		}

		public void load(Reader reader) throws IOException {
			resolve().load(reader);
		}

		public int size() {
			return resolve().size();
		}

		public boolean isEmpty() {
			return resolve().isEmpty();
		}

		public Enumeration<Object> keys() {
			return resolve().keys();
		}

		public Enumeration<Object> elements() {
			return resolve().elements();
		}

		public boolean contains(Object value) {
			return resolve().contains(value);
		}

		public boolean containsValue(Object value) {
			return resolve().containsValue(value);
		}

		public boolean containsKey(Object key) {
			return resolve().containsKey(key);
		}

		public Object get(Object key) {
			return resolve().get(key);
		}

		public void load(InputStream inStream) throws IOException {
			resolve().load(inStream);
		}

		public Object put(Object key, Object value) {
			return resolve().put(key, value);
		}

		public Object remove(Object key) {
			return resolve().remove(key);
		}

		public void putAll(Map<? extends Object, ? extends Object> t) {
			resolve().putAll(t);
		}

		public void clear() {
			resolve().clear();
		}

		public Object clone() {
			return resolve().clone();
		}

		public String toString() {
			return resolve().toString();
		}

		public Set<Object> keySet() {
			return resolve().keySet();
		}

		public Set<Entry<Object, Object>> entrySet() {
			return resolve().entrySet();
		}

		public Collection<Object> values() {
			return resolve().values();
		}

		public boolean equals(Object o) {
			return resolve().equals(o);
		}

		@SuppressWarnings("deprecation")
		public void save(OutputStream out, String comments) {
			resolve().save(out, comments);
		}

		public int hashCode() {
			return resolve().hashCode();
		}

		public void store(Writer writer, String comments) throws IOException {
			resolve().store(writer, comments);
		}

		public void store(OutputStream out, String comments) throws IOException {
			resolve().store(out, comments);
		}

		public void loadFromXML(InputStream in) throws IOException,
				InvalidPropertiesFormatException {
			resolve().loadFromXML(in);
		}

		public void storeToXML(OutputStream os, String comment)
				throws IOException {
			resolve().storeToXML(os, comment);
		}

		public void storeToXML(OutputStream os, String comment, String encoding)
				throws IOException {
			resolve().storeToXML(os, comment, encoding);
		}

		public String getProperty(String key) {
			return resolve().getProperty(key);
		}

		public String getProperty(String key, String defaultValue) {
			return resolve().getProperty(key, defaultValue);
		}

		public Enumeration<?> propertyNames() {
			return resolve().propertyNames();
		}

		public Set<String> stringPropertyNames() {
			return resolve().stringPropertyNames();
		}

		public void list(PrintStream out) {
			resolve().list(out);
		}

		public void list(PrintWriter out) {
			resolve().list(out);
		}
	}
}
