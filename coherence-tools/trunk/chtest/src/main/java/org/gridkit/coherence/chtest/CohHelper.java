/**
 * Copyright 2013 Alexey Ragozin
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
package org.gridkit.coherence.chtest;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;

import org.gridkit.util.concurrent.DebugHelper;
import org.gridkit.vicluster.ViConfigurable;
import org.gridkit.vicluster.ViExecutor;
import org.gridkit.vicluster.isolate.Isolate;
import org.gridkit.vicluster.telecontrol.jvm.JvmProps;

import com.tangosol.coherence.component.net.extend.RemoteService;
import com.tangosol.coherence.component.net.extend.connection.TcpConnection;
import com.tangosol.coherence.component.util.SafeCluster;
import com.tangosol.coherence.component.util.safeService.SafeProxyService;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.ProxyService;
import com.tangosol.net.Service;
import com.tangosol.net.management.MBeanServerFinder;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;

public class CohHelper {

	static {
		DebugHelper.enableJUnitTimeouts();
	}
	
	public static void pofConfig(ViConfigurable node, String path) {
		node.setProp("tangosol.pof.config", path);
	}

	public static void pofEnabled(ViConfigurable node, boolean enabled) {
		node.setProp("tangosol.pof.enabled", enabled ? "true" : "false");
	}

	public static void cacheConfig(ViConfigurable node, String path) {
		node.setProp("tangosol.coherence.cacheconfig", path);
	}

	public static void localstorage(ViConfigurable node, boolean enabled) {
		node.setProp("tangosol.coherence.distributed.localstorage", String.valueOf(enabled));
	}
	
	public static void applyCacheConfigFragment(ViConfigurable node, XmlConfigFragment fragment) {
		node.addStartupHook(fragment.toString(), new CacheConfigInjecter(fragment), false);
	}

	public static void applyOperationalConfigFragment(ViConfigurable node, XmlConfigFragment fragment) {
		node.addStartupHook(fragment.toString(), new OperationalConfigInjecter(fragment), false);
	}

	public static void enableFastLocalCluster(ViConfigurable node) {
		int port = new Random().nextInt(10000) + 50000;
		node.setProp("tangosol.coherence.ttl", "0");
		node.setProp("tangosol.coherence.wka", "127.0.0.1");
		node.setProp("tangosol.coherence.wka.port", String.valueOf(port));
		node.setProp("tangosol.coherence.localhost", "127.0.0.1");
		node.setProp("tangosol.coherence.localport", String.valueOf(port));
		node.setProp("tangosol.coherence.socketprovider", "tcp");
		node.setProp("tangosol.coherence.cluster", "jvm::" + ManagementFactory.getRuntimeMXBean().getName());
	}
	
	public static void setClusterLocalHost(ViConfigurable node, String host) {
		node.setProp("tangosol.coherence.localhost", host);
	}

	public static void setClusterLocalPort(ViConfigurable node, int port) {
		node.setProp("tangosol.coherence.localport", String.valueOf(port));
	}

	public static void enableTCMP(ViConfigurable node, boolean enable) {
		node.setProp("tangosol.coherence.tcmp.enabled", enable ? "true" : "false");
	}

	public static void addWkaAddress(ViConfigurable node, String host, int port) {
		applyOperationalConfigFragment(node, new AddWkaAddress(host, port));
	}
	
	public static void enableJmx(ViConfigurable node, boolean enable) {
		String hookName = "wipeout-jmx";
		if (enable) {
			node.setProp("tangosol.coherence.management", "local-only");
			node.setProp("tangosol.coherence.management.jvm.all", "false");
			node.setProp("tangosol.coherence.management.remote", "false");
			node.setProp("tangosol.coherence.management.serverfactory", IsolateMBeanFinder.class.getName());
			node.addShutdownHook(hookName, new JmxProxyCleanUp(), true);
		}
		else {
			node.setProp("tangosol.coherence.management", null);
			node.setProp("tangosol.coherence.management.jvm.all", null);
			node.setProp("tangosol.coherence.management.remote", null);
			node.setProp("tangosol.coherence.management.serverfactory", null);
			node.addShutdownHook(hookName, new Noop(), true);
		}
	}

	public static void enableViNodeName(ViConfigurable node, boolean enable) {
		String hookName = "coherence-process-name";
		if (enable) {
			node.addStartupHook(hookName, new NodeNameUpdater(), true);
		}
		else {
			node.addStartupHook(hookName, new Noop(), true);
		}
	}

	public static void enableCoherenceThreadKillers(ViConfigurable node, boolean enable) {
		String hookName1 = "coherence-socket-killer";
		String hookName2 = "coherence-daemon-killer";
		if (enable) {
			node.addShutdownHook(hookName1, new ShutdownSocketDestructor(), true);
			node.addShutdownHook(hookName2, new CoherenceDaemonKiller(), true);
		}
		else {
			node.addShutdownHook(hookName1, new Noop(), true);
			node.addShutdownHook(hookName2, new Noop(), true);
		}
	}
	
	/**
	 * Coherence does not have standard property for cluster join timeout.
	 * This method will patch op. configuration directly.
	 */
	public static void setJoinTimeout(ViConfigurable node, final long timeout) {
		node.addStartupHook("coherence-cluster-join-timeout", new Runnable() {
			@Override
			public void run() {
				Cluster cluster = CacheFactory.getCluster();
				if (cluster.isRunning()) {
					throw new IllegalStateException("Cluster is already started");
				}
				XmlElement config = CacheFactory.getClusterConfig();
				XmlHelper.ensureElement(config, "cluster-config/multicast-listener/join-timeout-milliseconds")
					.setLong(timeout);
				CacheFactory.setServiceConfig("Cluster", config);
			}
		}, true);
	}

	/**
	 * Coherence does not have standard property for TCMP packet timeout.
	 * This method will patch op. configuration directly.
	 */
	public static void setTCMPTimeout(ViConfigurable node, final long timeout) {
		node.addStartupHook("coherence-tcmp-timeout", new Runnable() {
			@Override
			public void run() {
				Cluster cluster = CacheFactory.getCluster();
				if (cluster.isRunning()) {
					throw new IllegalStateException("Cluster is already started");
				}
				XmlElement config = CacheFactory.getClusterConfig();
				XmlHelper.ensureElement(config, "packet-publisher/packet-delivery/timeout-milliseconds")
				.setLong(timeout);
				CacheFactory.setServiceConfig("Cluster", config);
			}
		}, true);
	}

	/**
	 * Coherence does not have standard property to disable TCP ring.
	 * TCP ring tends to hung in isolate environment, so disabling it makes testing more stable.
	 * This method will patch op. configuration directly.
	 */
	public static void enableTcpRing(ViConfigurable node, boolean enable) {
		String hookName = "coherence-disable-tcp-ring";
		if (enable) {
			node.addStartupHook(hookName, new Noop(), true);
		}
		else {
			node.addStartupHook(hookName, new Runnable() {
				@Override
				public void run() {
					Cluster cluster = CacheFactory.getCluster();
					if (cluster.isRunning()) {
						throw new IllegalStateException("Cluster is already started");
					}
					XmlElement config = CacheFactory.getClusterConfig();
					XmlHelper.ensureElement(config, "tcp-ring-listener/enabled")
					.setBoolean(false);
					CacheFactory.setServiceConfig("Cluster", config);
				}
			}, true);
		}
	}

	public static void configureCoherenceVersion(ViConfigurable node, String version) {		
		String path = JarManager.getJarPath(version);

		try {
			String curUrl = JarManager.getCoherenceJarPath();
			String curpath = new File(new URI(curUrl).getPath()).getCanonicalPath();
			if (path.equals(curpath)) {
				// if would try to add and remove at same time
				// it would break Isolate
				return;
			}
			node.setProp(JvmProps.CP_REMOVE + "coherence.jar",  curpath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}		

		node.setProp(JvmProps.CP_ADD + "coherence.jar", path);
	}
	
	public static int getNumberOfConnections(ProxyService service) {
		com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.ProxyService realService;
		if (service instanceof SafeProxyService) {
			realService = (com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.ProxyService) ((SafeProxyService)service).getService();
		}
		else {
			realService = (com.tangosol.coherence.component.util.daemon.queueProcessor.service.grid.ProxyService) service;
		}
		
		return realService.getServiceLoad().getConnectionCount();
	}
	
	/**
	 * Kills Coherence*Extends connection for given remote service.
	 * Could be used only on Extend client.
	 * 
	 * @param serviceName - remote service name
	 */
	public static void killTcpInitiator(String serviceName) {
		for(Service s: getLocalServices()) {
			if (serviceName.equals(s.getInfo().getServiceName())) {
				killTcpInitiator(s);
			}
		}
	}
	
	/**
	 * Kills Coherence*Extends connections.
	 * Could be used only on Extend client.
	 */
	public static void killTcpAllInitiators() {
		for(Service s: getLocalServices()) {
			if (s instanceof RemoteService) {
				killTcpInitiator(s);
			}
		}
	}
	
	private static void killTcpInitiator(Service s) {
		RemoteService rs = (RemoteService) s;
		try {
			TcpConnection tcp = (TcpConnection) rs.getInitiator().ensureConnection();
			Socket sock = tcp.getSocket();
			System.err.println("Dropping Extend socket " + rs.getInfo().getServiceName() + " | " + sock);
			sock.close();
		} catch (IOException e) {
			// ignore
		}
	}

	@SuppressWarnings("unchecked")
	private static Set<Service> getLocalServices() {
		try {
			Cluster cluster = CacheFactory.getCluster();
			Method m = SafeCluster.class.getDeclaredMethod("getLocalServices");
			m.setAccessible(true);
			return (Set<Service>) m.invoke(cluster);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unused")
	private static Object jmxAttribute(ViExecutor node, final ObjectName name, final String attribute) {
		if (node == null) {
			return jmxAttribute(name, attribute);
		}
		else return node.exec(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return jmxAttribute(name, attribute);
			}
		});
	}
	
	private static Object jmxAttribute(ObjectName name, String attribute) {
		try {
			MBeanServerConnection mserver = getContextMBeanServer();
			Object bi = mserver.getAttribute(name, attribute);
			if (bi == null) {
				return 0;
			}
			else {
				return bi;
			}
		}
		catch (InstanceNotFoundException e) {
			return null;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}

	private static <V> boolean waitFor(Callable<V> condition, V excepted, long timeoutMs) {
		long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
		do {
			V val;
			try {
				val = condition.call();
			} catch (Exception e) {
				continue;
			}
			if (val == null) {
				if (excepted == null) {
					return true;
				}
			}
			else {
				if (val.equals(excepted)) {
					return true;
				}
			}
			try {
				Thread.sleep(100);
			}
			catch(InterruptedException e) {				
			}
		}
		while(deadline > System.nanoTime());
			
		return false;
	}
	
	private static ObjectName mbeanCluster() {
		try {
			return new ObjectName("Coherence:type=Cluster");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static ObjectName mbeanServiceName(String name,int nodeId) {
		try {
			return new ObjectName("Coherence:type=Service,name=" + name + ",nodeId=" + nodeId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static int jmxMemberId() {
		Object x = jmxAttribute(mbeanCluster(), "LocalMemberId");
		return x == null ? 0 : ((Integer)x).intValue();
	}

	public static int jmxMemberId(ViExecutor node) {
		return node == null ? jmxMemberId() : node.exec(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return jmxMemberId();
			}
		});
	}

	/**
	 * Use JMX to drop Coherence*Extend connections on proxy side.
	 * @param node ViNode to operate
	 */
	public static void jmxCloseAllProxyConnections(ViExecutor node) {
		if (node == null) {
			jmxCloseProxyConnections("*");
		}
		else {
			node.exec(new Runnable() {
				
				@Override
				public void run() {
					jmxCloseProxyConnections("*");
				}
			});
		}
	}

	/**
	 * Use JMX to drop Coherence*Extend connections on proxy side.
	 * @param node ViNode to operate
	 */
	public static void jmxCloseProxyConnections(ViExecutor node, final String service) {
		if (node == null) {
			jmxCloseProxyConnections(service);
		}
		else {
			node.exec(new Runnable() {
				
				@Override
				public void run() {
					jmxCloseProxyConnections(service);
				}
			});
		}
	}

	public static void jmxCloseProxyConnections(String proxyServiceName) {		
		try {
			final MBeanServerConnection server = getContextMBeanServer();
			int id = jmxMemberId();
			for(final ObjectName name : server.queryNames(null, null)) {
				if (isConnectionBean(name) && String.valueOf(id).equals(name.getKeyProperty("nodeId"))) {
					if (!"*".equals(proxyServiceName)) {
						if (!proxyServiceName.equals(name.getKeyProperty("name"))) {
							continue;
						}
					}
					// Separate thread is required if we are executing on that connection
					Thread thread = new Thread() {
						@Override
						public void run() {
							try {
								server.invoke(name, "closeConnection", new Object[0], new String[0]);
								System.err.println("Extend conntection closed: " + name);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
					thread.start();
					try {
						thread.join();
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * List Coherence*Extend connections MBeans.
	 */
	public static Collection<ObjectName> jmxListProxyConnections() {
		return jmxListProxyConnections("*");
	}

	/**
	 * List Coherence*Extend connections MBeans.
	 */
	public static Collection<ObjectName> jmxListProxyConnections(String proxyServiceName) {		
		try {
			final MBeanServerConnection server = getContextMBeanServer();
			int id = jmxMemberId();
			List<ObjectName> result = new ArrayList<ObjectName>();
			for(final ObjectName name : server.queryNames(null, null)) {
				if (isConnectionBean(name) && String.valueOf(id).equals(name.getKeyProperty("nodeId"))) {
					if (!"*".equals(proxyServiceName)) {
						if (!proxyServiceName.equals(name.getKeyProperty("name"))) {
							continue;
						}
					}
					result.add(name);
				}
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean isConnectionBean(ObjectName name) {
		return "Coherence".equals(name.getDomain())
				&& "Connection".equals(name.getKeyProperty("type"));
	}

	public static String jmxServiceStatusHA(ViExecutor node, final String serviceName) {
		return node.exec(new Callable<String>() {
			@Override
			public String call() throws Exception {
				return jmxServiceStatusHA(serviceName);
			}
		});
	}
	
	/**
	 * Check service HA status via JMX.
	 */
	public static String jmxServiceStatusHA(String serviceName) {
		String s = (String) jmxAttribute(mbeanServiceName(serviceName, jmxMemberId()), "StatusHA");
		return s;
	}

	/**
	 * Check that service is running using JMX.  
	 */
	public static boolean jmxServiceRunning(ViExecutor node, final String serviceName) {
		return node.exec(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return jmxServiceRunning(serviceName);
			}
		});
	}

	/**
	 * Check that service is running using JMX.  
	 */
	public static boolean jmxServiceRunning(String serviceName) {
		Boolean b = (Boolean) jmxAttribute(mbeanServiceName(serviceName, jmxMemberId()), "Running");
		return b == null ?  false : b.booleanValue();
	}

	public static void jmxWaitForService(ViExecutor node, String serviceName) {
		jmxWaitForService(node, serviceName, 30000); // Coherence is slow, 30000
	}

	public static void jmxWaitForService(String serviceName) {
		jmxWaitForService(serviceName, 30000); // Coherence is slow, 30000
	}

	public static void jmxWaitForService(ViExecutor node, final String serviceName, final long timeoutMs) {
		node.exec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				jmxWaitForService(serviceName, timeoutMs);
				return null;
			}
		});
	}
	
	public static void jmxWaitForService(final String serviceName, long timeoutMs) {
		if (!waitFor(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return jmxServiceRunning(serviceName);
			}
			
		}, Boolean.TRUE, timeoutMs)) {
			throw new AssertionError("Wait for service have failed");
		}
	}

	public static void jmxWaitForStatusHA(String serviceName, String status) {
		jmxWaitForStatusHA(serviceName, status, 30000); // Coherence is slow, 30000		
	}

	public static void jmxWaitForStatusHA(ViExecutor node, String serviceName, String status) {
		jmxWaitForStatusHA(node, serviceName, status, 30000); // Coherence is slow, 30000		
	}

	public static void jmxWaitForStatusHA(ViExecutor node, final String serviceName, final String status, final long timeoutMs) {
		node.exec(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				jmxWaitForStatusHA(serviceName, status, timeoutMs);
				return null;
			}
		});
	}

	public static void jmxWaitForStatusHA(final String serviceName, final String status, long timeoutMs) {
		if (!waitFor(new Callable<String>() {
			
			@Override
			public String call() throws Exception {
				return jmxServiceStatusHA(serviceName);
			}
			
		}, status, timeoutMs)) {
			throw new AssertionError("Wait for service have failed");
		}
	}

	public static void jmxDumpMBeans() {
		try {
			MBeanServerConnection server = getContextMBeanServer();
			for(ObjectName name : server.queryNames(null, null)) {
				System.out.println(name);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static MBeanServerConnection getContextMBeanServer() {
		// cluster connection my take sometime
		long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(3500);
		MBeanServer server = null;
		while(deadline > System.nanoTime()) {			
			server = IsolateMBeanFinder.MSERVER;
			if (server == null) {
				LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
			}
			else {
				break;
			}			
		}
		if (server == null) {
			throw new IllegalStateException("Local JMX is not enabled for node " + Isolate.currentIsolate().getName());
		}
		return server;
	}
	
	private static class IsolatedMBeanServerProxy implements MBeanServer {
		
		private MBeanServer shared;
		private MBeanServer internal;
		private String isolateName;
		
		public IsolatedMBeanServerProxy(String name, MBeanServer shared, MBeanServer internal) {
			this.isolateName = name;
			this.shared = shared;
			this.internal = internal;
		}

		private ObjectName decorate(ObjectName name) {			
			try {
				String mname = name.getDomain();
				mname += "@" + isolateName + ":";
				mname += name.toString().substring(name.toString().indexOf(':') + 1);
				return new ObjectName(mname);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public void dispose() {
			for(ObjectName name: internal.queryNames(null, null)) {
				try {
					shared.unregisterMBean(decorate(name));
				} catch (Exception e) {
					// ignore
				}
			}
		}
		
		public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
			if (internal.isRegistered(name)) {
				try {
					internal.unregisterMBean(name);
				} catch (InstanceNotFoundException e) {
					// ignore
				}
			}
			return internal.createMBean(className, name);
		}

		public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
			return internal.createMBean(className, name, loaderName);
		}

		public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException,  NotCompliantMBeanException {
			return internal.createMBean(className, name, params, signature);
		}

		public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
			return internal.createMBean(className, name, loaderName, params, signature);
		}

		public ObjectInstance registerMBean(Object object, ObjectName name) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
			if (shared.isRegistered(decorate(name))) {
				try {
					shared.unregisterMBean(decorate(name));
				} catch (InstanceNotFoundException e) {
					// ignore
				}
			}
			shared.registerMBean(object, decorate(name));
			return internal.registerMBean(object, name);
		}

		public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException {
			shared.unregisterMBean(decorate(name));
			internal.unregisterMBean(name);
		}

		public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException {
			return internal.getObjectInstance(name);
		}

		public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
			return internal.queryMBeans(name, query);
		}

		public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
			return internal.queryNames(name, query);
		}

		public boolean isRegistered(ObjectName name) {
			return internal.isRegistered(name);
		}

		public Integer getMBeanCount() {
			return internal.getMBeanCount();
		}

		public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
			return internal.getAttribute(name, attribute);
		}

		public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException {
			return internal.getAttributes(name, attributes);
		}

		public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
			internal.setAttribute(name, attribute);
		}

		public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException {
			return internal.setAttributes(name, attributes);
		}

		public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException {
			return internal.invoke(name, operationName, params, signature);
		}

		public String getDefaultDomain() {
			return internal.getDefaultDomain();
		}

		public String[] getDomains() {
			return internal.getDomains();
		}

		public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException {
			internal.addNotificationListener(name, listener, filter, handback);
		}

		public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException {
			internal.addNotificationListener(name, listener, filter, handback);
		}

		public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException {
			internal.removeNotificationListener(name, listener);
		}

		public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
			internal.removeNotificationListener(name, listener, filter,	handback);
		}

		public void removeNotificationListener(ObjectName name, NotificationListener listener) 	throws InstanceNotFoundException, ListenerNotFoundException {
			internal.removeNotificationListener(name, listener);
		}

		public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException {
			internal.removeNotificationListener(name, listener, filter,	handback);
		}

		public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
			return internal.getMBeanInfo(name);
		}

		public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException {
			return internal.isInstanceOf(name, className);
		}

		public Object instantiate(String className) throws ReflectionException, MBeanException {
			return internal.instantiate(className);
		}

		public Object instantiate(String className, ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException {
			return internal.instantiate(className, loaderName);
		}

		public Object instantiate(String className, Object[] params, String[] signature) throws ReflectionException, MBeanException {
			return internal.instantiate(className, params, signature);
		}

		public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException {
			return internal.instantiate(className, loaderName, params, signature);
		}

		@SuppressWarnings("deprecation")
		public ObjectInputStream deserialize(ObjectName name, byte[] data) throws InstanceNotFoundException, OperationsException {
			return internal.deserialize(name, data);
		}

		@SuppressWarnings("deprecation")
		public ObjectInputStream deserialize(String className, byte[] data) throws OperationsException, ReflectionException {
			return internal.deserialize(className, data);
		}

		@SuppressWarnings("deprecation")
		public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data) throws InstanceNotFoundException, OperationsException, ReflectionException {
			return internal.deserialize(className, loaderName, data);
		}

		public ClassLoader getClassLoaderFor(ObjectName mbeanName) throws InstanceNotFoundException {
			return internal.getClassLoaderFor(mbeanName);
		}

		public ClassLoader getClassLoader(ObjectName loaderName) throws InstanceNotFoundException {
			return internal.getClassLoader(loaderName);
		}

		public ClassLoaderRepository getClassLoaderRepository() {
			return internal.getClassLoaderRepository();
		}
	}
	
	public static class IsolateMBeanFinder implements MBeanServerFinder {

		static IsolatedMBeanServerProxy MSERVER = null;
		
		@Override
		public synchronized MBeanServer findMBeanServer(String sDefaultDomain) {
			if (Isolate.currentIsolate() == null) {
				return ManagementFactory.getPlatformMBeanServer();
			}
			else {
				if (MSERVER == null) {
					String iname = System.getProperty("isolate.name");
					MBeanServer internal = MBeanServerFactory.newMBeanServer(iname); 
					MSERVER = new IsolatedMBeanServerProxy(iname, ManagementFactory.getPlatformMBeanServer(), internal);
				}
				return MSERVER;
			}
		}
	}
	
	@SuppressWarnings("serial")
	public static class JmxProxyCleanUp implements Runnable, Serializable {

		@Override
		public void run() {
			IsolatedMBeanServerProxy proxy = IsolateMBeanFinder.MSERVER;
			if (proxy != null) {
				proxy.dispose();
			}
		}
	}
	
	@SuppressWarnings("serial")
	private static class Noop implements Runnable, Serializable {
		@Override
		public void run() {
		}
	}

	@SuppressWarnings("serial")
	private static class NodeNameUpdater implements Runnable, Serializable {
		@Override
		public void run() {
			String name = System.getProperty("vinode.name");
			if (name != null) {
				System.setProperty("tangosol.coherence.process", name);
			}
		}
	}
	
	@SuppressWarnings("serial")
	private static class AddWkaAddress implements XmlConfigFragment, Serializable {
		
		private String hostname;
		private int port;

		public AddWkaAddress(String hostname, int port) {
			this.hostname = hostname;
			this.port = port;
		}

		@Override
		public void inject(XmlElement opConfig) {
			XmlElement wka = opConfig.getSafeElement("unicast-listener/well-known-addresses");
			XmlElement sa = wka.addElement("socket-address");
			sa.addElement("address").setString(hostname);
			sa.addElement("port").setInt(port);			
		}
		
		public String toString() {
			return getClass().getSimpleName() + "[" + hostname + ":" + port + "]";
		}
	}
}
