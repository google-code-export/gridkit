package org.gridkit.utils.vicluster;

import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import com.tangosol.net.management.MBeanServerFinder;

public class CohHelper {

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
	
	public static class IsolateMBeanFinder implements MBeanServerFinder {

		static MBeanServer MSERVER = null;
		
		@Override
		public synchronized MBeanServer findMBeanServer(String sDefaultDomain) {
			if (MSERVER == null) {
				MSERVER = MBeanServerFactory.newMBeanServer(System.getProperty("isolate.name"));
			}
			return MSERVER;
		}
	}
}
