package com.medx.framework.proxy.serialization.xom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Document;
import nu.xom.Serializer;

import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.proxy.MapProxy;
import com.medx.framework.proxy.MapProxyFactory;
import com.medx.framework.proxy.serialization.MapProxySerializer;
import com.medx.framework.proxy.serialization.xom.internal.ArrayXomSerializer;
import com.medx.framework.proxy.serialization.xom.internal.EnumXomSerializer;
import com.medx.framework.proxy.serialization.xom.internal.ListXomSerializer;
import com.medx.framework.proxy.serialization.xom.internal.MapProxyXomSerializer;
import com.medx.framework.proxy.serialization.xom.internal.MapXomSerializer;
import com.medx.framework.proxy.serialization.xom.internal.PrimitiveXomSerializer;
import com.medx.framework.proxy.serialization.xom.internal.SetXomSerializer;

public class XomSerializer implements MapProxySerializer<String>, XomSerializationContext {
	private final ThreadLocal<Integer> objectCounter = new ThreadLocal<Integer>();
	private final ThreadLocal<Map<Object, Integer>> identityMap = new ThreadLocal<Map<Object,Integer>>();
	private final ThreadLocal<Map<Integer, Object>> objectMap = new ThreadLocal<Map<Integer, Object>>();
	
	private final ArrayXomSerializer arraySerializer = new ArrayXomSerializer();
	private final EnumXomSerializer enumSerializer = new EnumXomSerializer();
	private final ListXomSerializer<?> listSerializer = new ListXomSerializer<Object>();
	private final MapXomSerializer<?, ?> mapSerializer = new MapXomSerializer<Object, Object>();
	private final SetXomSerializer<?> setSerializer = new SetXomSerializer<Object>();
	private final PrimitiveXomSerializer primitiveSerializer = new PrimitiveXomSerializer();
	
	private final MapProxyXomSerializer mapProxySerializer;
	
	private final MapProxyFactory proxyFactory;
	
	public XomSerializer(MapProxyFactory proxyFactory, ModelMetadata modelMetadata) {
		this.proxyFactory = proxyFactory;
		this.mapProxySerializer = new MapProxyXomSerializer(modelMetadata);
	}
	
	@Override
	public String serialize(MapProxy mapProxy) {
		objectCounter.set(0);
		identityMap.set(new IdentityHashMap<Object, Integer>());
		
		String result = null;
		
		try {
			result = serializeInternal(mapProxy);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		objectCounter.set(null);
		identityMap.set(null);
		
		return result;
	}
	
	private String serializeInternal(MapProxy mapProxy) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Serializer serializer = new Serializer(outputStream, "UTF-8");
		serializer.setIndent(4);
		serializer.write(new Document(mapProxySerializer.serialize(mapProxy, this)));
		
		outputStream.close();
		
		return outputStream.toString("UTF-8");
	}
	
	@Override
	public MapProxy deserialize(String data) {
		objectMap.set(new HashMap<Integer, Object>());
		
		objectMap.set(null);
		
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> InternalXomSerializer<T> getXomSerializer(T object) {
		Class<?> clazz = object.getClass();
		
		if (Proxy.isProxyClass(clazz) || proxyFactory.isProxiable(object))
			return (InternalXomSerializer<T>)mapProxySerializer;
		else if (clazz.isArray())
			return (InternalXomSerializer<T>) arraySerializer;
		else if (clazz.isEnum())
			return (InternalXomSerializer<T>) enumSerializer;
		else if (List.class.isInstance(object))
			return (InternalXomSerializer<T>) listSerializer;
		else if (Map.class.isInstance(object))
			return (InternalXomSerializer<T>) mapSerializer;
		else if (Set.class.isInstance(object))
			return (InternalXomSerializer<T>) setSerializer;
		else if (PrimitiveXomSerializer.tagNameByClass.containsKey(clazz))
			return (InternalXomSerializer<T>) primitiveSerializer;
		else
			throw new IllegalArgumentException("object");
	}
	
	@Override
	public Map<Object, Integer> getIdentityMap() {
		if (identityMap.get() == null)
			throw new IllegalStateException();
		
		return identityMap.get();
	}

	@Override
	public int getNextObjectId() {
		if (objectCounter.get() == null)
			throw new IllegalStateException();
		
		int nextId = objectCounter.get();
		
		objectCounter.set(nextId + 1);
		
		return nextId;
	}

	@Override
	public Map<Integer, Object> getObjectMap() {
		if (objectMap.get() == null)
			throw new IllegalStateException();
		
		return objectMap.get();
	}
}
