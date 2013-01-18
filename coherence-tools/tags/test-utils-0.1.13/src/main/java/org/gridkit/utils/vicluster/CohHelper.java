package org.gridkit.utils.vicluster;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.Socket;
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

import com.tangosol.coherence.component.net.extend.RemoteService;
import com.tangosol.coherence.component.net.extend.connection.TcpConnection;
import com.tangosol.coherence.component.util.SafeCluster;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Service;
import com.tangosol.net.management.MBeanServerFinder;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;

public class CohHelper {

	public static void pofConfig(ViProps node, String path) {
		node.setProp("tangosol.pof.config", path);
	}

	public static void cacheConfig(ViProps node, String path) {
		node.setProp("tangosol.coherence.cacheconfig", path);
	}

	public static void localstorage(ViProps node, boolean enabled) {
		node.setProp("tangosol.coherence.distributed.localstorage", String.valueOf(enabled));
	}

	public static void enableFastLocalCluster(ViProps node) {
		int port = new Random().nextInt(10000) + 50000;
		node.setProp("tangosol.coherence.ttl", "0");
		node.setProp("tangosol.coherence.wka", "127.0.0.1");
		node.setProp("tangosol.coherence.wka.port", String.valueOf(port));
		node.setProp("tangosol.coherence.localhost", "127.0.0.1");
		node.setProp("tangosol.coherence.localport", String.valueOf(port));
		node.setProp("tangosol.coherence.socketprovider", "tcp");
		node.setProp("tangosol.coherence.cluster", "jvm::" + ManagementFactory.getRuntimeMXBean().getName());
	}

	public static void shareCluster(ViProps node, ViCluster cluster) {
		node.setProp("tangosol.coherence.ttl", 			cluster.getClusterProp("tangosol.coherence.ttl"));
		node.setProp("tangosol.coherence.wka", 			cluster.getClusterProp("tangosol.coherence.wka"));
		node.setProp("tangosol.coherence.wka.port", 	cluster.getClusterProp("tangosol.coherence.wka.port"));
		node.setProp("tangosol.coherence.localhost", 	cluster.getClusterProp("tangosol.coherence.localhost"));
		node.setProp("tangosol.coherence.localport", 	cluster.getClusterProp("tangosol.coherence.localport"));
		node.setProp("tangosol.coherence.cluster", 		cluster.getClusterProp("tangosol.coherence.cluster"));
	}
	
	public static void disableTCMP(ViProps node) {
		node.setProp("tangosol.coherence.tcmp.enabled", "false");
	}

	public static void enableJmx(ViProps node) {
		node.setProp("tangosol.coherence.management", "local-only");
		node.setProp("tangosol.coherence.management.jvm.all", "false");
		node.setProp("tangosol.coherence.management.remote", "false");
		node.setProp("tangosol.coherence.management.serverfactory", IsolateMBeanFinder.class.getName());
	}
	
	/**
	 * Coherence does not have standard property for cluster join timeout.
	 * This method will patch op. configuration directly.
	 */
	public static void setJoinTimeout(ViNode node, final long timeout) {
		node.exec(new Runnable() {
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
		});
	}

	/**
	 * Coherence does not have standard property for TCMP packet timeout.
	 * This method will patch op. configuration directly.
	 */
	public static void setTCMPTimeout(ViNode node, final long timeout) {
		node.exec(new Runnable() {
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
		});
	}

	/**
	 * Coherence does not have standard property to disable TCP ring.
	 * TCP ring tends to hung in isolate environment, so disabling it makes testing more stable.
	 * This method will patch op. configuration directly.
	 */
	public static void disableTcpRing(ViNode node) {
		node.exec(new Runnable() {
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
		});
	}

	public static void killTcpInitiator(String serviceName) {
		for(Service s: getLocalServices()) {
			if (serviceName.equals(s.getInfo().getServiceName())) {
				killTcpInitiator(s);
			}
		}
	}
	
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
	
	private static Object jmxAttribute(ViNode node, ObjectName name, String attribute) {
		try {
			MBeanServer mserver = getMBeanServer(node);
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
	
	public static int jmxMemberId(ViNode node) {
		Object x = jmxAttribute(node, mbeanCluster(), "LocalMemberId");
		return x == null ? 0 : ((Integer)x).intValue();
	}

	public static void jmxCloseProxyConnections(ViNode node) {
		jmxCloseProxyConnections(node, "*");
	}

	public static void jmxCloseProxyConnections(ViNode node, String proxyServiceName) {		
		final MBeanServer server = getMBeanServer(node);
		int id = jmxMemberId(node);
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
	}

	public static Collection<ObjectName> jmxListProxyConnections(ViNode node) {
		return jmxListProxyConnections(node, "*");
	}

	public static Collection<ObjectName> jmxListProxyConnections(ViNode node, String proxyServiceName) {		
		final MBeanServer server = getMBeanServer(node);
		int id = jmxMemberId(node);
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
	}
	
	private static boolean isConnectionBean(ObjectName name) {
		return "Coherence".equals(name.getDomain())
				&& "Connection".equals(name.getKeyProperty("type"));
	}

	public static String jmxServiceStatusHA(ViNode node, String serviceName) {
		String s = (String) jmxAttribute(node, mbeanServiceName(serviceName, jmxMemberId(node)), "StatusHA");
		return s;
	}

	public static boolean jmxServiceRunning(ViNode node, String serviceName) {
		Boolean b = (Boolean) jmxAttribute(node, mbeanServiceName(serviceName, jmxMemberId(node)), "Running");
		return b == null ?  false : b.booleanValue();
	}

	public static void jmxWaitForService(ViNode node, String serviceName) {
		jmxWaitForService(node, serviceName, 30000); // Coherence is slow, 30000
	}

	public static void jmxWaitForService(final ViNode node, final String serviceName, long timeoutMs) {
		if (!waitFor(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				return jmxServiceRunning(node, serviceName);
			}
			
		}, Boolean.TRUE, timeoutMs)) {
			throw new AssertionError("Wait for service have failed");
		}
	}

	public static void jmxWaitForStatusHA(ViNode node, String serviceName, String status) {
		jmxWaitForStatusHA(node, serviceName, status, 30000); // Coherence is slow, 30000		
	}

	public static void jmxWaitForStatusHA(final ViNode node, final String serviceName, final String status, long timeoutMs) {
		if (!waitFor(new Callable<String>() {
			
			@Override
			public String call() throws Exception {
				return jmxServiceStatusHA(node, serviceName);
			}
			
		}, status, timeoutMs)) {
			throw new AssertionError("Wait for service have failed");
		}
	}

	public static void jmxDumpMBeans(ViNode node) {
		MBeanServer server = getMBeanServer(node);
		for(ObjectName name : server.queryNames(null, null)) {
			System.out.println(name);
		}
	}
	
	private static MBeanServer getMBeanServer(ViNode node) {
		if (node != null) {
			// cluster connection my take sometime
			long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(3500);
			MBeanServer server = null;
			while(deadline > System.nanoTime()) {			
				server = node.getIsolate().exportNoProxy(new Callable<MBeanServer>() {
					@Override
					public MBeanServer call() throws Exception {
						return IsolateMBeanFinder.MSERVER;
					}
				});
				if (server == null) {
					LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
				}
				else {
					break;
				}			
			}
			if (server == null) {
				throw new IllegalStateException("Local JMX is not enabled for node " + node.getName());
			}
			return server;
		}
		else {
			MBeanServer server = IsolateMBeanFinder.MSERVER;
			if (server == null) {
				throw new IllegalStateException("Local JMX is not enabled for this node");
			}
			return server;
		}
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
				mname += "[" + isolateName + "]:";
				mname += name.toString().substring(name.toString().indexOf(':') + 1);
				return new ObjectName(mname);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
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

		static MBeanServer MSERVER = null;
		
		@Override
		public synchronized MBeanServer findMBeanServer(String sDefaultDomain) {
			if (MSERVER == null) {
				String iname = System.getProperty("isolate.name");
				MBeanServer internal = MBeanServerFactory.newMBeanServer(iname); 
				MSERVER = new IsolatedMBeanServerProxy(iname, ManagementFactory.getPlatformMBeanServer(), internal);
			}
			return MSERVER;
		}
	}
}
