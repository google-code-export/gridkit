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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.run.xml.SimpleDocument;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;

public class CacheConfig {

	public enum LeaseGranularity {
		thread,
		member
	}
	
	public enum EvictionPolicy {
		LRU,
		LFU,
		HYBRID
	}

	public enum UnitCalculator {
		FIXED,
		BINARY
	}
	
	public enum Macro {
		CACHE_NAME {
			@Override
			public String toString() {
				return "{cache-name}";
			}
		},
		MANAGER_CONTEXT {
			@Override
			public String toString() {
				return "{manager-context}";
			}			
		}
	}

	
	public static XmlConfigFragment mapCache(String pattern, CacheScheme scheme) {
		XmlElement xml = scheme.getXml();
		String name = getSchemeName(xml);
		if (name == null) {
			name = "scheme-for-cache--" + pattern;
			xml.addElement("scheme-name").setString(name);
		}
		CacheNameInjector cni = new CacheNameInjector(pattern, name);
		CacheSchemeInjector csi = new CacheSchemeInjector(xml);
		return new MultiFragment(cni, csi);
	}

	public static XmlConfigFragment mapCache(String pattern, String schemeName) {
		return new CacheNameInjector(pattern, schemeName);
	}

	public static XmlConfigFragment addScheme(CacheScheme scheme) {
		XmlElement xml = scheme.getXml();
		String name = getSchemeName(xml);
		if (name == null) {
			throw new IllegalArgumentException("scheme does have a name");
		}
		return new CacheSchemeInjector(scheme.getXml());
	}

	public static XmlConfigFragment addScheme(ServiceScheme scheme) {
		return new CacheSchemeInjector(scheme.getXml());
	}
	
	public static DistributedScheme distributedSheme() {
		return new DistributedSchemeBuilder();
	}

	public static LocalScheme localScheme() {
		return new LocalSchemeBuilder();
	}

	public static ReadWriteBackingMap readWriteBackmingMap() {
		return new ReadWriteBackingMapBuilder();
	}
	
	public static ProxyScheme proxyScheme() {
		return new ProxySchemeBuilder();
	}

	public static RemoteCacheScheme remoteCacheScheme() {
		return new RemoteCacheSchemeBuilder();
	}

	public static ReplicatedScheme replicatedScheme() {
		return new ReplicatedSchemeBuilder();
	}
	
	public static Instantiation intantiate(Class<?> c, Object... args) {
		Class<?>[] paramTypes = toParamTypes(args);
		Constructor<?> winner = null;
		for(Constructor<?> cc: c.getConstructors()) {
			if (match(cc.getParameterTypes(), paramTypes)) {
				if (winner != null) {
					throw new RuntimeException("Ambigous parameter list, match both " + winner.toString() + " " + cc.toString()); 
				}
				winner = cc;
			}
		}
		if (winner == null) {
			throw new RuntimeException("No matching constructor found for " + c.getName());
		}
		XmlElement element = new SimpleDocument("class-scheme");
		element.addElement("class-name").setString(c.getName());
		if (args.length > 0) {
			addInitParams(element, winner.getParameterTypes(), args);
		}
		
		return new FixedInstantiation(element);
	}

	public static Instantiation factoryInstantiate(Class<?> factory, String method, Object... args) {
		Class<?>[] paramTypes = toParamTypes(args);
		Method winner = null;
		for(Method fm: factory.getMethods()) {
			if (!fm.getName().equals(method)) {
				continue;
			}
			if (!Modifier.isStatic(fm.getModifiers())) {
				continue;
			}
			if (match(fm.getParameterTypes(), paramTypes)) {
				if (winner != null) {
					throw new RuntimeException("Ambigous parameter list, match both " + winner.toString() + " " + fm.toString()); 
				}
				winner = fm;
			}
		}
		if (winner == null) {
			throw new RuntimeException("No matching static method found");
		}
		XmlElement element = new SimpleDocument("class-scheme");
		element.addElement("class-factory-name").setString(factory.getName());
		element.addElement("method-name").setString(method);
		if (args.length > 0) {
			addInitParams(element, winner.getParameterTypes(), args);
		}		
		
		return new FixedInstantiation(element);
	}
	
	private static Class<?>[] toParamTypes(Object[] args) {
		Class<?>[] paramTypes = new Class<?>[args.length];
		for(int i = 0; i != args.length; ++i) {
			if (args[i] == null) {
				throw new IllegalArgumentException("null cannot be passed");
			}
			else if (args[i] instanceof Macro) {
				switch((Macro)args[i]) {
				case CACHE_NAME: paramTypes[i] = String.class; break;
				case MANAGER_CONTEXT: paramTypes[i] = BackingMapManagerContext.class;
				}
			}
			else if (args[i] instanceof String || args[i] instanceof Integer || args[i] instanceof Double) {
				paramTypes[i] = args[i].getClass();
			}
			else if (args[i] instanceof FixedInstantiation) {
				paramTypes[i] = ((FixedInstantiation)args[i]).getClass();
			}
			else {
				throw new IllegalArgumentException("Unsupported argument type: " + args[i].getClass().getName());
			}
		}
		
		return paramTypes;
	}
	
	private static void addInitParams(XmlElement root, Class<?>[] types, Object[] args) {
		XmlElement params = root.addElement("init-params");
		for(int i = 0; i != types.length; ++i) {
			XmlElement param = params.addElement("init-param");
			String type;
			Class<?> ct = types[i];
			if (ct == int.class) {
				type = "int";
			}
			else if (ct == double.class) {
				type = "double";
			}
			else {
				type = ct.getName();
			}
			param.addElement("param-type").setString(type);
			if (args[i] instanceof Instantiation) {
				XmlElement e = param.addElement("param-value");
				appendElement(e, ((Instantiation)args[i]).getXml());
			}
			else {
				param.addElement("param-value").setString(args[i].toString());
			}
		}
	}
	
	private static boolean match(Class<?>[] declared, Class<?>[] actual) {
		if (declared.length != actual.length) {
			return false;
		}
		for(int i = 0; i != declared.length; ++i) {
			if (actual[i] != null) {
				Class<?> c = declared[i];
				if (c.isPrimitive()) {
					if (c == int.class) {
						c = Integer.class;
					}
					else if (c == double.class) {
						c = Double.class;
					}
					if (!c.isAssignableFrom(actual[i])) {
						return false;
					}
				}
			}
			else if (declared[i].isPrimitive()) {
				return false;
			}
		}
		return true;
	}
	
	private static String getSchemeName(XmlElement e) {
		XmlElement sn = e.getElement("scheme-name");
		if (sn == null) {
			return null;
		}
		else {
			String name = sn.getString("");
			return name.length() == 0 ? null : name;
		}
	}
	
	public interface XmlFragment extends Copyable {
		
		XmlFragment copy();
		
	}
	
	public interface Instantiation extends Fragment {
		
	}

	public interface CacheScheme extends XmlFragment, Fragment, BackingMap {
		
		void schemeName(String name);
		
		void schemeRef(String name);
		
		void serviceName(String serviceName);
	}

	public interface ServiceScheme extends XmlFragment, Fragment {
		
		ServiceScheme copy();
		
		void schemeName(String name);
		
		void schemeRef(String name);
		
		void serviceName(String serviceName);
		
		void guardianTimeout(String timeout);

		void guardianTimeout(long timeoutMs);
		
		void autoStart(boolean enabled);	
		
	}
	
	public interface BackingMap extends XmlFragment, Fragment, BinaryBackingMap {
		
		BackingMap copy();
		
		void listener(Class<?> c, Object... params);

		void listener(Instantiation ref);
	}

	public interface BinaryBackingMap extends XmlFragment, Fragment {
		
		BinaryBackingMap copy();
		
	}

	public interface LocalScheme extends CacheScheme, BackingMap {
		
		LocalScheme copy();

		void evictionPolicy(EvictionPolicy policy);
		
		void evictionPolicy(Instantiation instantiation);
		
		void highUnits(int untis);
		
		void lowUnits(int units);
		
		void unitCalculator(UnitCalculator unitCalculator);

		void unitCalculator(Instantiation ref);
		
		void unitFactor(int factor);
		
		void expiryDelay(String delay);
		
		void listener(Class<?> c, Object... params);

		void listener(Instantiation ref);
		
	}
	
	
	public interface DistributedScheme extends CacheScheme, ServiceScheme, BackingMap {

		DistributedScheme copy();
		
		void serializer(String name);
		
		void serializer(Class<?> type, Object... arguments);

		void serializer(Instantiation instance);
		
		void threadCount(int threadCount);
		
		void leaseGranularity(LeaseGranularity granularity);
		
		void localStorage(boolean enabled);
		
		void partitionCount(int partitionCount);
		
		void backupCount(int backupCount);
		
		void backupCountAfterWriteBehind(int backupCount);
		
		void backingMapScheme(BinaryBackingMap backingMap);
		
		void listener(Class<?> c, Object... params);

		void listener(Instantiation ref);
	}

	public interface ReplicatedScheme extends CacheScheme, BackingMap {

		ReplicatedScheme copy();
		
		void serviceName(String serviceName);
		
		void serializer(String name);
		
		void serializer(Class<?> type, Object... arguments);

		void serializer(Instantiation instance);

		void backingMapScheme(CacheScheme backingMap);
				
		void autoStart(boolean enabled);
		
		void listener(Class<?> c, Object... params);

		void listener(Instantiation ref);
	}	
	
	public interface ProxyScheme extends ServiceScheme {
		
		ProxyScheme copy();
		
		void schemeName(String name);
		
		void schemeRef(String name);
		
		void serviceName(String serviceName);
		
		void threadCount(int threadCount);
		
		void cacheProxyEnabled(boolean enabled);

		void cacheProxyReadOnly(boolean readOnly);
		
		void cacheProxyLockEnabled(boolean enabled);
		
		void invocationProxyEnabled(boolean enabled);
		
		void autoStart(boolean autoStart);
		
		void serializer(String serializer);
		
		void serializer(Instantiation instantiation);

		void serializer(Class<?> c, Object... arguments);
		
		void tcpAcceptorLocalAddress(String host, int port);
		
		void tcpAcceptorReuseAddress(boolean reuse);

		void tcpAcceptorKeepAlive(boolean enabled);

		void tcpAcceptorTcpDelay(boolean enabled);

		void tcpAcceptorSendBufferSize(String size);

		void tcpAcceptorSendBufferSize(int size);
		
		void connectionLimit(int limit);
		
	}
	
	public interface RemoteCacheScheme extends CacheScheme {
		
		RemoteCacheScheme copy();
		
		void serviceName(String serviceName);

		void deferKeyAssociationCheck(boolean defer);
		
		void serializer(String serializer);
		
		void serializer(Instantiation instantiation);
		
		void serializer(Class<?> c, Object... arguments);
		
		void tpcInitiatorRemoteAddress(String host, int port);

	}
	
	public interface ReadWriteBackingMap extends BackingMap {
		
		ReadWriteBackingMap copy();
		
		void schemeName(String name);
		
		void schemeRef(String name);

		void internalCacheScheme(BackingMap backingMap);

		void writeMaxBatchSize(int maxSize);

		void missCacheScheme(BackingMap backingMap);
		
		void cacheStoreScheme(Class<?> c, Object... params);

		void cacheStoreScheme(Instantiation ref);
		
		void readOnly(boolean readOnly);
		
		void writeDelay(String delay);
		
		void writeBatchFactor(double factor);
		
		void writeRequeueThreshold(int threshold);
	}
	
	private interface Fragment {
		
		public XmlElement getXml();
		
	}
	
	private interface Copyable {
		
		public Object copy();
		
	}
	
	private static class BaseXmlBuilder implements Fragment {
		
		private final XmlElement xml;
		
		public BaseXmlBuilder(String name) {
			xml = new SimpleDocument(name);
		}

		protected BaseXmlBuilder(XmlElement xml) {
			this.xml = xml;
		}

		@Override
		public XmlElement getXml() {
			return (XmlElement) xml.clone();
		}
		
		protected void addElement(String element, String value) {
			addElement(null, element, value);
		}
		
		protected void addElement(String element, int value) {
			addElement(null, element, value);
		}

		protected void addElement(String element, boolean value) {
			addElement(null, element, value);
		}
		
		protected void addElement(String element, Fragment frag) {
			addElement(null, element, frag);
		}

		@SuppressWarnings("unused")
		protected void addElement(String element, XmlElement frag) {
			addElement(null, element, frag);
		}
		
		protected void addElementContent(String element, XmlElement frag) {
			addElementContent(null, element, frag);
		}
		
		protected void addElement(String path, String element, String value) {
			XmlElement base = path == null ? xml : xml.ensureElement(path);
			base.addElement(element).setString(value);
		}

		protected void addElement(String path, String element, int value) {
			XmlElement base = path == null ? xml : xml.ensureElement(path);
			base.addElement(element).setInt(value);
		}

		protected void addElement(String path, String element, boolean value) {
			XmlElement base = path == null ? xml : xml.ensureElement(path);
			base.addElement(element).setBoolean(value);
		}

		protected void addElement(String path, String element, Fragment frag) {
			XmlElement base = path == null ? xml : xml.ensureElement(path);
			XmlElement e = base.addElement(element);
			appendElement(e, frag.getXml());
		}

		protected void addElement(String path, String element, XmlElement frag) {
			XmlElement base = path == null ? xml : xml.ensureElement(path);
			XmlElement e = base.addElement(element);
			appendElement(e, frag);
		}

		protected void addElementContent(String path, String element, XmlElement frag) {
			XmlElement base = path == null ? xml : xml.ensureElement(path);
			XmlElement copy = (XmlElement) frag.clone();
			copy.setName(element);
			appendElement(base, copy);
		}

		protected void addChild(String path, XmlElement frag) {
			XmlElement base = path == null ? xml : xml.ensureElement(path);
			appendElement(base, frag);
		}
		
		public String toString() {
			return xml.toString();
		}
	}
	
	private static class FixedInstantiation extends BaseXmlBuilder implements Instantiation {

		public FixedInstantiation(XmlElement xml) {
			super(xml);
		}
	}
	
	private static class LocalSchemeBuilder extends BaseXmlBuilder implements LocalScheme {

		public LocalSchemeBuilder() {
			super("local-scheme");
		}

		protected LocalSchemeBuilder(XmlElement xml) {
			super(xml);
		}

		@Override
		public LocalScheme copy() {
			return new LocalSchemeBuilder(getXml());
		}

		@Override
		public void schemeName(String name) {
			addElement("scheme-name", name);
		}

		@Override
		public void schemeRef(String name) {
			addElement("scheme-ref", name);
		}

		@Override
		public void serviceName(String serviceName) {
			addElement("service-name", serviceName);
		}

		@Override
		public void evictionPolicy(EvictionPolicy policy) {
			if (policy == null) {
				throw new NullPointerException("policy should not be null");
			}
			addElement("eviction-policy", policy.toString());
		}

		@Override
		public void evictionPolicy(Instantiation instantiation) {
			addElement("eviction-policy", instantiation);
		}

		@Override
		public void highUnits(int units) {
			addElement("high-units", units);
		}

		@Override
		public void lowUnits(int units) {
			addElement("low-units", units);
		}

		@Override
		public void unitCalculator(UnitCalculator unitCalculator) {
			if (unitCalculator == null) {
				throw new NullPointerException("unitCalculator should not be null");
			}
			addElement("unit-calculator", unitCalculator.toString());
		}

		@Override
		public void unitCalculator(Instantiation ref) {
			addElement("unit-calculator", ref);
		}

		@Override
		public void unitFactor(int factor) {
			addElement("unit-factor", factor);
		}

		@Override
		public void expiryDelay(String delay) {
			XmlHelper.parseTime(delay);
			addElement("expiry-delay", delay);
		}
		
		@Override
		public void listener(Class<?> c, Object... params) {
			addElement("listener", intantiate(c, params));
		}

		@Override
		public void listener(Instantiation ref) {
			addElement("listener", ref);
		}
	}
	
	private static class DistributedSchemeBuilder extends BaseXmlBuilder implements DistributedScheme {

		public DistributedSchemeBuilder() {
			super("distributed-scheme");
		}

		private DistributedSchemeBuilder(XmlElement xml) {
			super(xml);
		}
		
		@Override
		public DistributedScheme copy() {
			return new DistributedSchemeBuilder(getXml());
		}

		@Override
		public void schemeName(String name) {
			addElement("scheme-name", name);
		}

		@Override
		public void schemeRef(String name) {
			addElement("scheme-ref", name);			
		}

		@Override
		public void serviceName(String serviceName) {
			addElement("service-name", serviceName);
		}

		@Override
		public void serializer(String name) {
			addElement("serializer", name);
		}

		@Override
		public void serializer(Class<?> type, Object... arguments) {
			serializer(intantiate(type, arguments));
		}

		@Override
		public void serializer(Instantiation instance) {
			addElementContent("serializer", instance.getXml());
		}

		@Override
		public void threadCount(int threadCount) {
			addElement("thread-count", threadCount);
		}

		@Override
		public void leaseGranularity(LeaseGranularity granularity) {
			if (granularity == null) {
				throw new NullPointerException();
			}
			addElement("lease-granularity", granularity.toString());
		}

		@Override
		public void localStorage(boolean enabled) {
			addElement("local-storage", enabled);
		}

		@Override
		public void partitionCount(int partitionCount) {
			addElement("partition-count", partitionCount);
		}

		@Override
		public void backupCount(int backupCount) {
			addElement("backup-count", backupCount);
		}

		@Override
		public void backupCountAfterWriteBehind(int backupCount) {
			addElement("backup-count-after-writebehind", backupCount);
		}

		@Override
		public void backingMapScheme(BinaryBackingMap backingMap) {
			addElement("backing-map-scheme", backingMap);
		}

		@Override
		public void guardianTimeout(String timeout) {
			addElement("guardian-timeout", timeout);
		}

		@Override
		public void guardianTimeout(long timeoutMs) {
			addElement("guardian-timeout", String.valueOf(timeoutMs));
		}

		@Override
		public void listener(Class<?> c, Object... params) {
			addElement("listener", intantiate(c, params));
		}

		@Override
		public void listener(Instantiation ref) {
			addElement("listener", ref);
		}

		@Override
		public void autoStart(boolean enabled) {
			addElement("autostart", enabled);
		}
	}

	private static class ReplicatedSchemeBuilder extends BaseXmlBuilder implements ReplicatedScheme {
		
		public ReplicatedSchemeBuilder() {
			super("replicated-scheme");
		}
		
		private ReplicatedSchemeBuilder(XmlElement xml) {
			super(xml);
		}
		
		@Override
		public ReplicatedScheme copy() {
			return new ReplicatedSchemeBuilder(getXml());
		}
		
		@Override
		public void schemeRef(String name) {
			addElement("scheme-ref", name);			
		}
		
		@Override
		public void schemeName(String name) {
			addElement("scheme-name", name);
		}
		
		@Override
		public void serviceName(String serviceName) {
			addElement("service-name", serviceName);
		}
		
		@Override
		public void serializer(String name) {
			addElement("serializer", name);
		}
		
		@Override
		public void serializer(Class<?> type, Object... arguments) {
			serializer(intantiate(type, arguments));
		}
		
		@Override
		public void serializer(Instantiation instance) {
			addElementContent("serializer", instance.getXml());
		}

		@Override
		public void backingMapScheme(CacheScheme backingMap) {
			addElement("backing-map-scheme", backingMap);
		}		
		
		@Override
		public void autoStart(boolean enabled) {
			addElement("autostart", enabled);
		}
		
		@Override
		public void listener(Class<?> c, Object... params) {
			addElement("listener", intantiate(c, params));
		}

		@Override
		public void listener(Instantiation ref) {
			addElement("listener", ref);
		}
	}
	
	private static class ReadWriteBackingMapBuilder extends BaseXmlBuilder implements ReadWriteBackingMap {
		
		public ReadWriteBackingMapBuilder() {
			super("read-write-backing-map-scheme");
		}
		
		private ReadWriteBackingMapBuilder(XmlElement xml) {
			super(xml);
		}
		
		@Override
		public ReadWriteBackingMap copy() {
			return new ReadWriteBackingMapBuilder(getXml());
		}

		@Override
		public void schemeName(String name) {
			addElement("scheme-name", name);
		}

		@Override
		public void schemeRef(String name) {
			addElement("scheme-ref", name);			
		}

		@Override
		public void internalCacheScheme(BackingMap backingMap) {
			addElement("internal-cache-scheme", backingMap);
		}

		@Override
		public void writeMaxBatchSize(int maxSize) {
			addElement("write-max-batch-size", maxSize);
		}

		@Override
		public void missCacheScheme(BackingMap backingMap) {
			addElement("miss-cache-scheme", backingMap);
		}

		@Override
		public void cacheStoreScheme(Instantiation ref) {
			addElement("cachestore-scheme", ref);
		}

		@Override
		public void cacheStoreScheme(Class<?> c, Object... params) {
			cacheStoreScheme(intantiate(c, params));
		}

		@Override
		public void listener(Class<?> c, Object... params) {
			addElement("listener", intantiate(c, params));
		}

		@Override
		public void listener(Instantiation ref) {
			addElement("listener", ref);
		}

		@Override
		public void readOnly(boolean readOnly) {
			addElement("read-only", readOnly);
		}

		@Override
		public void writeDelay(String delay) {
			XmlHelper.parseTime(delay, XmlHelper.UNIT_S);
			addElement("write-delay", delay);
		}

		@Override
		public void writeBatchFactor(double factor) {
			addElement("write-batch-factor", String.valueOf(factor));
		}

		@Override
		public void writeRequeueThreshold(int threshold) {
			addElement("write-requeue-threshold", threshold);
		}
	}
	
	private static class ProxySchemeBuilder extends BaseXmlBuilder implements ProxyScheme {

		public ProxySchemeBuilder() {
			super("proxy-scheme");
		}

		private ProxySchemeBuilder(XmlElement xml) {
			super(xml);
		}

		@Override
		public ProxyScheme copy() {
			return new ProxySchemeBuilder(getXml());
		}

		@Override
		public void schemeName(String name) {
			addElement("scheme-name", name);
		}

		@Override
		public void schemeRef(String name) {
			addElement("scheme-ref", name);			
		}

		@Override
		public void serviceName(String serviceName) {
			addElement("service-name", serviceName);
		}

		@Override
		public void threadCount(int threadCount) {
			addElement("thread-count", threadCount);
		}

		@Override
		public void cacheProxyEnabled(boolean enabled) {
			addElement("proxy-config/cache-service-proxy", "enabled", enabled);
		}

		@Override
		public void cacheProxyReadOnly(boolean readOnly) {
			addElement("proxy-config/cache-service-proxy", "read-only", readOnly);
		}

		@Override
		public void cacheProxyLockEnabled(boolean enabled) {
			addElement("proxy-config/cache-service-proxy", "lock-enabled", enabled);
		}

		@Override
		public void invocationProxyEnabled(boolean enabled) {
			addElement("proxy-config/invocation-service-proxy", "enabled", enabled);
		}

		@Override
		public void autoStart(boolean autoStart) {
			addElement("autostart", autoStart);
		}

		@Override
		public void serializer(String serializer) {
			addElement("acceptor-config", "serializer", serializer);
		}

		@Override
		public void serializer(Instantiation ref) {
			addElementContent("acceptor-config", "serializer", ref.getXml());
		}

		@Override
		public void guardianTimeout(String timeout) {
			addElement("guardian-timeout", timeout);
		}

		@Override
		public void guardianTimeout(long timeoutMs) {
			addElement("guardian-timeout", String.valueOf(timeoutMs));
		}

		@Override
		public void serializer(Class<?> c, Object... arguments) {
			serializer(intantiate(c, arguments));			
		}

		@Override
		public void tcpAcceptorLocalAddress(String host, int port) {
			addElement("acceptor-config/tcp-acceptor/local-address", "address", host);
			addElement("acceptor-config/tcp-acceptor/local-address", "port", port);
		}

		@Override
		public void tcpAcceptorReuseAddress(boolean reuse) {
			addElement("acceptor-config/tcp-acceptor", "reuse-address", reuse);
		}

		@Override
		public void tcpAcceptorKeepAlive(boolean enabled) {
			addElement("acceptor-config/tcp-acceptor", "keep-alive-enabled", enabled);
		}

		@Override
		public void tcpAcceptorTcpDelay(boolean enabled) {
			addElement("acceptor-config/tcp-acceptor", "tcp-delay-enabled", enabled);
		}

		@Override
		public void tcpAcceptorSendBufferSize(String size) {
			XmlHelper.parseMemorySize(size);
			addElement("acceptor-config/tcp-acceptor", "send-buffer-size", size);
		}

		@Override
		public void tcpAcceptorSendBufferSize(int size) {
			addElement("acceptor-config/tcp-acceptor", "send-buffer-size", size);
		}

		@Override
		public void connectionLimit(int limit) {
			addElement("acceptor-config", "connection-limit", limit);
		}
	}
	
	private static class RemoteCacheSchemeBuilder extends BaseXmlBuilder implements RemoteCacheScheme {

		public RemoteCacheSchemeBuilder() {
			super("remote-cache-scheme");
		}

		private RemoteCacheSchemeBuilder(XmlElement xml) {
			super(xml);
		}

		@Override
		public RemoteCacheScheme copy() {
			return new RemoteCacheSchemeBuilder(getXml());
		}

		@Override
		public void schemeName(String name) {
			addElement("scheme-name", name);
		}

		@Override
		public void schemeRef(String name) {
			addElement("scheme-ref", name);			
		}

		@Override
		public void serviceName(String serviceName) {
			addElement("service-name", serviceName);
		}

		@Override
		public void deferKeyAssociationCheck(boolean defer) {
			addElement("defer-key-association-check", defer);
		}

		@Override
		public void serializer(String serializer) {
			addElement("initiator-config", "serializer", serializer);
		}

		@Override
		public void serializer(Instantiation ref) {
			addElementContent("initiator-config", "serializer", ref.getXml());
		}

		@Override
		public void serializer(Class<?> c, Object... arguments) {
			serializer(intantiate(c, arguments));
		}

		@Override
		public void tpcInitiatorRemoteAddress(String host, int port) {
			SimpleDocument doc = new SimpleDocument("socket-address");
			doc.addElement("address").setString(host);
			doc.addElement("port").setInt(port);
			
			addChild("initiator-config/tcp-initiator/remote-addresses", doc);
		}
		
		@Override
		public void listener(Class<?> c, Object... params) {
			addElement("listener", intantiate(c, params));
		}

		@Override
		public void listener(Instantiation ref) {
			addElement("listener", ref);
		}		
	}
	
	@SuppressWarnings("serial")
	private static class CacheNameInjector implements XmlConfigFragment, Serializable {

		private final String cacheName;
		private final String schemeName;
		
		public CacheNameInjector(String cacheName, String schemeName) {
			this.cacheName = cacheName;
			this.schemeName = schemeName;
		}

		@Override
		public void inject(XmlElement cacheConfig) {
			XmlElement mapping = cacheConfig.getElement("caching-scheme-mapping").addElement("cache-mapping");
			mapping.addElement("cache-name").setString(cacheName);
			mapping.addElement("scheme-name").setString(schemeName);
		}
	}

	@SuppressWarnings("serial")
	private static class CacheSchemeInjector implements XmlConfigFragment, Serializable {

		private final XmlElement scheme;
		
		public CacheSchemeInjector(XmlElement scheme) {
			this.scheme = scheme;
		}

		@Override
		public void inject(XmlElement cacheConfig) {
			XmlElement schemes = cacheConfig.getElement("caching-schemes");
		
			appendElement(schemes, scheme);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void appendElement(XmlElement target, XmlElement fragment) {
		XmlElement copy = target.addElement(fragment.getName());
		for(Map.Entry<String, String> attr : ((Map<String, String>)fragment.getAttributeMap()).entrySet()) {
			copy.addAttribute(attr.getKey()).setString(attr.getValue());
		}
		for(XmlElement sub: (List<XmlElement>)fragment.getElementList()) {
			appendElement(copy, sub);
		}
		String text = fragment.getString();
		if (text != null && text.length() > 0) {
			copy.setString(text);
		}
	}
	
	@SuppressWarnings("serial")
	private static class MultiFragment implements XmlConfigFragment, Serializable {
		
		private final XmlConfigFragment[] fragments;
		
		public MultiFragment(XmlConfigFragment... fragments) {
			this.fragments = fragments;
		}

		@Override
		public void inject(XmlElement cacheConfig) {
			for(XmlConfigFragment fragment: fragments) {
				fragment.inject(cacheConfig);
			}
		}
	}	
}
