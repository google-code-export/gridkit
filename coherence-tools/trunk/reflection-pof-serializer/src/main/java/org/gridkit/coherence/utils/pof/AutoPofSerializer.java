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

package org.gridkit.coherence.utils.pof;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import com.tangosol.io.ReadBuffer.BufferInput;
import com.tangosol.io.Serializer;
import com.tangosol.io.WriteBuffer.BufferOutput;
import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.PofBufferReader;
import com.tangosol.io.pof.PofBufferWriter;
import com.tangosol.io.pof.PofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.io.pof.PortableObjectSerializer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheService;
import com.tangosol.net.Cluster;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import com.tangosol.util.UUID;
import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.processor.ConditionalPut;

/**
 * Automatic serializer utilising POF without using static configuration xml.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class AutoPofSerializer implements Serializer, PofContext {

	private static final String AUTO_POF_SERVICE = "AUTO-POF-SERVICE";
	private static final String AUTO_POF_MAPPING = "AUTO_POF_MAPPING";
	
	private static final int MAX_CONFIG_ID = Integer.getInteger("gridkit.auto-pof.max-config-id", 1000);
	private static final int MIN_AUTO_ID = Integer.getInteger("gridkit.auto-pof.min-auto-id", 10000);
	private static final Boolean USE_PUBLIC_CACHE_CONFIG = Boolean.getBoolean("gridkit.auto-pof.use-public-cache-config");
	
	private static final String XML_FRAGMENT =
	"<cache-config>"
+ 		"<caching-scheme-mapping>"
+			"<cache-mapping><cache-name>AUTO_POF_MAPPING</cache-name><scheme-name>AUTO_POF_SCHEME</scheme-name></cache-mapping>"
+		"</caching-scheme-mapping>"
+ 		"<caching-schemes>"
+			"<replicated-scheme>"
+				"<scheme-name>AUTO_POF_SCHEME</scheme-name>"
+				"<service-name>AUTO_POF_SERVICE</service-name>"
+               "<backing-map-scheme><local-scheme/></backing-map-scheme>"
+			"</replicated-scheme>"
+		"</caching-schemes>"
+	"</cache-config>";
	
	// base context to delegate system class serialization
	private ConfigurablePofContext context;
	private NamedCache typeMap;
	
	private volatile Map<Class<?>, SerializationContext> classMap;  
	private volatile SerializationContext[] lowTypeIdMap = new SerializationContext[1024];
	private volatile SerializationContext[] highTypeIdMap = new SerializationContext[256];
	
	private ReflectionPofSerializer autoSerializer = new ReflectionPofSerializer();
	private int minAutoId = MIN_AUTO_ID;
	
	public AutoPofSerializer() {
		context = new ConfigurablePofContext();
//		initTypeMap();
		initContextMap();
		initCustomPredefines();
	}
	
	public AutoPofSerializer(String pofConfig) {
		context = new ConfigurablePofContext(pofConfig);
//		initTypeMap();
		initContextMap();
		initCustomPredefines();
	}

	public AutoPofSerializer(String pofConfig, NamedCache typeMap) {
		this.context = new ConfigurablePofContext(pofConfig);
		this.typeMap = typeMap;
		initContextMap();
		initCustomPredefines();
	}
	
		
	private void initTypeMap() {
		if (USE_PUBLIC_CACHE_CONFIG) {
			typeMap = CacheFactory.getCache(AUTO_POF_MAPPING);
		}
		else {
			Cluster cluster = CacheFactory.getCluster();
			if (cluster.getServiceInfo(AUTO_POF_SERVICE) != null) {
				CacheService service = (CacheService) cluster.getService(AUTO_POF_SERVICE);
				typeMap = service.ensureCache(AUTO_POF_MAPPING, null);
			}
			else {
				XmlElement xml = XmlHelper.loadXml(XML_FRAGMENT);
				typeMap = new DefaultConfigurableCacheFactory(xml).ensureCache(AUTO_POF_MAPPING, null);
			}
		}
	}

	private synchronized void initContextMap() {
 		Map<Class<?>, SerializationContext> map = new HashMap<Class<?>, SerializationContext>();
 		for(int i = 0; i != MAX_CONFIG_ID; ++i) {
 			try {
 				PofSerializer serializer = context.getPofSerializer(i);
 				Class<?> cls = context.getClass(i);
 				SerializationContext ctx = new SerializationContext();
 				ctx.pofId = i;
 				ctx.type = cls;
 				ctx.serializer = serializer;
 				map.put(cls, ctx);
 				putContext(ctx.pofId, ctx);
 			}
 			catch(IllegalArgumentException e) {
 				continue;
 			}
 		}
 		classMap = map; 		
	}
	
	private synchronized void initCustomPredefines() {
//		registerSerializationContext(minAutoId++, Throwable.class, new JavaSerializationSerializer());
		registerArraySerializer(minAutoId++ , UUID[].class);
	}

	@Override
	public PofSerializer getPofSerializer(int userType) {
		SerializationContext ctx = contextById(userType);
		return ctx.serializer;
	}

	@Override
	public int getUserTypeIdentifier(Object obj) {
		SerializationContext ctx = contextByClass(obj.getClass());
		return ctx.pofId;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public int getUserTypeIdentifier(Class cls) {
		SerializationContext ctx = contextByClass(cls);
		return ctx.pofId;
	}

	@Override
	public int getUserTypeIdentifier(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getClassName(int userType) {
		SerializationContext ctx = contextById(userType);
		return ctx.type.getName();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class getClass(int userType) {
		SerializationContext ctx = contextById(userType);
		return ctx.type;
	}

	@Override
	public boolean isUserType(Object obj) {
		contextByClass(obj.getClass());
		return true;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean isUserType(Class cls) {
		contextByClass(cls);
		return true;
	}

	@Override
	public boolean isUserType(String name) {
		throw new UnsupportedOperationException();
	}

	private SerializationContext contextByClass(Class<?> cls) {
		SerializationContext ctx = classMap.get(cls);
		if (ctx == null) {
			return ensureContextByClass(cls);
		}
		return ctx;
	}
	
	private SerializationContext contextById(int id) {
		SerializationContext context = typeIdMap(id);
		if (context == null) {
			return ensureContextById(id);
		}
		else {
			return context;
		}
	}
	
	private SerializationContext typeIdMap(int id) {
		if (id < MIN_AUTO_ID) {
			return id >= lowTypeIdMap.length ? null : lowTypeIdMap[id];
		}
		else {
			int iid = id - MIN_AUTO_ID;
			return iid >= highTypeIdMap.length ? null : highTypeIdMap[iid];
		}		
	}
	
	private synchronized void putContext(int id, SerializationContext ctx) {
		if (id < MIN_AUTO_ID) {
			if (id >= lowTypeIdMap.length) {
				int newSize = (id + 1023) & (~1023);
				SerializationContext[] lowArray = new SerializationContext[newSize];
				System.arraycopy(lowTypeIdMap, 0, lowArray, 0, lowTypeIdMap.length);
				lowTypeIdMap = lowArray;
			}
			lowTypeIdMap[id] = ctx;
		}
		else {
			int iid = id - MIN_AUTO_ID;
			if (iid >= highTypeIdMap.length) {
				int newSize = (iid + 255) & (~255);
				SerializationContext[] highArray = new SerializationContext[newSize];
				System.arraycopy(highTypeIdMap, 0, highArray, 0, highTypeIdMap.length);
				highTypeIdMap = highArray;
			}
			highTypeIdMap[iid] = ctx;
		}
	}
	
	private synchronized SerializationContext ensureContextByClass(Class<?> cls) {
		
		if (Throwable.class.isAssignableFrom(cls)) {
			SerializationContext ctx = classMap.get(Throwable.class);
			registerSerializationContext(ctx.pofId, cls, ctx.serializer);			
		}

		SerializationContext ctx = classMap.get(cls);
		if (ctx != null) {
			return ctx;
		}
		else {
			
			// try to import configure serializer on demand
			if (context.isUserType(cls)) {
				int id = context.getUserTypeIdentifier(cls);
				if (typeIdMap(id) != null && id < MIN_AUTO_ID) {
					ctx = importPofSerializer(id);
					if (classMap.get(cls) == ctx) {
						return ctx;
					}
				}
			}
			
			if (cls.isArray()) {
				registerArraySerializer(-1, cls);
			}
			else {
				registerTypeSerialser(-1, cls);
			}
			return classMap.get(cls);
		}
	}	

	private void registerArraySerializer(int userType, Class<?> cls) {
		if (userType < 0) {
			userType = registerUserType(cls.getName());
		}
		
//		System.out.println("AUTOPOF: " + userType + "->" + cls.getName());
		
		PofSerializer serializer;
		Class<?> base = cls.getComponentType();
		if (base.isPrimitive()) {
			if (base == boolean.class) {
				serializer = new BooleanArraySerializer();
			}
			else if (base == byte.class) {
				serializer = new ByteArraySerializer();
			}
			else if (base == char.class) {
				serializer = new CharArraySerializer();
			}
			else if (base == short.class) {
				serializer = new ShortArraySerializer();
			}
			else if (base == int.class) {
				serializer = new IntArraySerializer();
			}
			else if (base == long.class) {
				serializer = new LongArraySerializer();
			}
			else if (base == float.class) {
				serializer = new FloatArraySerializer();
			}
			else if (base == double.class) {
				serializer = new DoubleArraySerializer();
			}
			else {
				throw new IllegalArgumentException("Unknown primitive type - " + base.getName());
			}
		}
		else {
			Object[] proto = (Object[]) Array.newInstance(base, 0);
			serializer = new ObjectArraySerializer(proto);
		}		
		
		registerSerializationContext(userType, cls, serializer);
	}

	private void registerTypeSerialser(int userType, Class<?> cls) {
		if (userType < 0) {
			userType = registerUserType(cls.getName());
		}

//		System.out.println("AUTOPOF: " + userType + "->" + cls.getName());
		
		PofSerializer serializer;
		if (PortableObject.class.isAssignableFrom(cls)) {
			serializer = new PortableObjectSerializer(userType);
		}
		else {
			try {
				serializer = autoSerializer.getClassCodec(cls);
			} catch (IOException e) {
				throw new IllegalArgumentException(e.getMessage(), e.getCause());
			}
		}

		registerSerializationContext(userType, cls, serializer);
	}

	private void registerSerializationContext(int userType, Class<?> cls, PofSerializer serializer) {
		SerializationContext ctx = new SerializationContext();
		ctx.pofId = userType;
		ctx.type = cls;
		ctx.serializer = serializer;
		
		Map<Class<?>, SerializationContext> cm = new HashMap<Class<?>, AutoPofSerializer.SerializationContext>(classMap);
		cm.put(cls, ctx);
		classMap = cm;
		
		putContext(userType, ctx);
	}
	
	private synchronized SerializationContext ensureContextById(int id) {
		SerializationContext ctx = typeIdMap(id);
		if (ctx != null) {
			return ctx;
		}
		else {
			
			if (id < MIN_AUTO_ID) {
				return importPofSerializer(id);
			}
			else {
				
				if (typeMap == null) {
					initTypeMap();
				}
				
				String cname = (String) typeMap.get(id); // new java.util.HashMap(typeMap)
				if (cname == null) {
					throw new IllegalArgumentException("Unknown POF user type " + id);
				}
				try {
					Class<?> cls = Class.forName(cname, true, Thread.currentThread().getContextClassLoader());
					ensureContextByClass(cls);
					return typeIdMap(id);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Class is not found " + cname);
				}
			}
		}
	}

	private SerializationContext importPofSerializer(int id) {
		SerializationContext ctx;
		PofSerializer serializer = context.getPofSerializer(id);
		Class<?> cls = context.getClass(id);
		ctx = new SerializationContext();
		ctx.pofId = id;
		ctx.type = cls;
		ctx.serializer = serializer;
		Map<Class<?>, SerializationContext> map = new HashMap<Class<?>, AutoPofSerializer.SerializationContext>(classMap);
		map.put(cls, ctx);
		classMap = map;
		putContext(ctx.pofId, ctx);
		
		return ctx;
	}

	private synchronized int registerUserType(String name) {
		if (typeMap == null) {
			initTypeMap();
		}
		while(true) {
			if (typeMap.lock(name, 100)) {
				try {
					Integer n = (Integer) typeMap.get(name);
					if (n != null) {
						return n.intValue();
					}
					int nn;
					while(true) {
						n = (Integer) typeMap.get("");
						if (n == null) {
							nn = minAutoId;
						}
						else {
							nn = n + 1;
						}
						Object x = typeMap.invoke("", new ConditionalPut(new EqualsFilter(IdentityExtractor.INSTANCE, n), nn, true));
						if (x == null) {
							break;
						}
					}
					
					typeMap.put(nn, name);
					typeMap.put(name, nn);
					return nn;
				}
				finally {
					typeMap.unlock(name);
				}
			}
		}
	}
	
	public void serialize(BufferOutput out, Object o) throws IOException {
		PofBufferWriter writer = new PofBufferWriter(out, this);
		try {
			writer.writeObject(-1, o);
		} catch (RuntimeException e) {
			IOException ioex = new IOException(e.getMessage());

			ioex.initCause(e);
			throw ioex;
		}
	}

	public Object deserialize(BufferInput in) throws IOException {
		PofBufferReader reader = new PofBufferReader(in, this);
		try {
			return reader.readObject(-1);
		} catch (RuntimeException e) {
			IOException ioex = new IOException(e.getMessage());
			ioex.initCause(e);
			throw ioex;
		}
	}
	
	private static class SerializationContext {
		
		private Class<?> type;
		private int pofId;
		private PofSerializer serializer;
		
	}
	
	private static class BooleanArraySerializer implements PofSerializer {
		
		@Override
		public void serialize(PofWriter writer, Object array) throws IOException {
			writer.writeBooleanArray(0, (boolean[]) array);
			writer.writeRemainder(null);
		}
		
		@Override
		public Object deserialize(PofReader reader) throws IOException {
			boolean[] array = reader.readBooleanArray(0);
			reader.readRemainder();
			return array;
		}		
	}
	
	private static class ByteArraySerializer implements PofSerializer {

		@Override
		public void serialize(PofWriter writer, Object array) throws IOException {
			writer.writeByteArray(0, (byte[]) array);
			writer.writeRemainder(null);
		}

		@Override
		public Object deserialize(PofReader reader) throws IOException {
			byte[] array = reader.readByteArray(0);
			reader.readRemainder();
			return array;
		}		
	}

	private static class CharArraySerializer implements PofSerializer {

		@Override
		public void serialize(PofWriter writer, Object array) throws IOException {
			writer.writeCharArray(0, (char[]) array);
			writer.writeRemainder(null);
		}

		@Override
		public Object deserialize(PofReader reader) throws IOException {
			char[] array = reader.readCharArray(0);
			reader.readRemainder();
			return array;
		}		
	}

	private static class ShortArraySerializer implements PofSerializer {
		
		@Override
		public void serialize(PofWriter writer, Object array) throws IOException {
			writer.writeShortArray(0, (short[]) array);
			writer.writeRemainder(null);
		}
		
		@Override
		public Object deserialize(PofReader reader) throws IOException {
			short[] array = reader.readShortArray(0);
			reader.readRemainder();
			return array;
		}		
	}

	private static class IntArraySerializer implements PofSerializer {
		
		@Override
		public void serialize(PofWriter writer, Object array) throws IOException {
			writer.writeIntArray(0, (int[]) array);
			writer.writeRemainder(null);
		}
		
		@Override
		public Object deserialize(PofReader reader) throws IOException {
			int[] array = reader.readIntArray(0);
			reader.readRemainder();
			return array;
		}		
	}

	private static class LongArraySerializer implements PofSerializer {
		
		@Override
		public void serialize(PofWriter writer, Object array) throws IOException {
			writer.writeLongArray(0, (long[]) array);
			writer.writeRemainder(null);
		}
		
		@Override
		public Object deserialize(PofReader reader) throws IOException {
			long[] array = reader.readLongArray(0);
			reader.readRemainder();
			return array;
		}		
	}

	private static class FloatArraySerializer implements PofSerializer {
		
		@Override
		public void serialize(PofWriter writer, Object array) throws IOException {
			writer.writeFloatArray(0, (float[]) array);
			writer.writeRemainder(null);
		}
		
		@Override
		public Object deserialize(PofReader reader) throws IOException {
			float[] array = reader.readFloatArray(0);
			reader.readRemainder();
			return array;
		}		
	}

	private static class DoubleArraySerializer implements PofSerializer {
		
		@Override
		public void serialize(PofWriter writer, Object array) throws IOException {
			writer.writeDoubleArray(0, (double[]) array);
			writer.writeRemainder(null);
		}
		
		@Override
		public Object deserialize(PofReader reader) throws IOException {
			double[] array = reader.readDoubleArray(0);
			reader.readRemainder();
			return array;
		}		
	}
	
	private static class ObjectArraySerializer implements PofSerializer {
		
		private final Object[] prototype;
		
		public ObjectArraySerializer(Object[] prototype) {
			this.prototype = prototype;
		}

		@Override
		public void serialize(PofWriter writer, Object obj) throws IOException {
			Object[] array = (Object[]) obj;
			writer.writeObjectArray(0, array);
			writer.writeRemainder(null);
		}

		@Override
		public Object deserialize(PofReader reader) throws IOException {
			Object result = reader.readObjectArray(0, prototype);
			reader.readRemainder();
			if (((Object[])result).length == 0) {
				return prototype;
			}
			else {
				return result;
			}
		}		
	}
	
	public static class JavaSerializationSerializer implements Serializer, PofSerializer {

		public static Object fromBytes(byte[] buf) throws IOException {
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(buf);
				ObjectInputStream ois = new ObjectInputStream(bis);
				Object result = ois.readObject();
				return result;
			} catch (ClassNotFoundException e) {
				throw new IOException("Class not found '" + e.getMessage() + "'");
			}
		}

		public static byte[] toBytes(Object object) throws IOException {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(object);
			oos.close();
			byte[] byteArray = bos.toByteArray();
			return byteArray;
		}

		@Override
		public Object deserialize(BufferInput in) throws IOException {
			byte[] buf = new byte[in.available()];
			in.read(buf);
			return fromBytes(buf);
		}

		@Override
		public Object deserialize(PofReader in) throws IOException {
			byte[] data = in.readByteArray(0);
			in.readRemainder();
			return fromBytes(data);
		}
		
		@Override
		public void serialize(BufferOutput out, Object object) throws IOException {
			byte[] byteArray = toBytes(object);
			out.write(byteArray);
		}

		@Override
		public void serialize(PofWriter out, Object object)	throws IOException {
			byte[] data = toBytes(object);
			out.writeByteArray(0, data);
			out.writeRemainder(null);
		}
	}
}
