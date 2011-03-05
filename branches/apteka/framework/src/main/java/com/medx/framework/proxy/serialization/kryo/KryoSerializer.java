package com.medx.framework.proxy.serialization.kryo;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;
import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.proxy.MapProxy;
import com.medx.framework.proxy.MapProxyFactory;
import com.medx.framework.proxy.serialization.MapProxyBinarySerializer;

public class KryoSerializer implements MapProxyBinarySerializer {
    private static final Class<?>[] supportedClasses = {boolean[].class, byte[].class, char[].class, short[].class,
    	int[].class, long[].class, float[].class, double[].class, Boolean[].class, Byte[].class, Character[].class,
    	Short[].class, Long[].class, Float[].class, Double[].class, String[].class, ArrayList.class, LinkedList.class, 
    	HashMap.class, TreeMap.class, HashSet.class, TreeSet.class};

    private final Kryo kryo;
    
    private final ThreadLocal<ObjectBuffer> objectBuffer;
    
    private final MapProxyFactory proxyFactory;
    
	private final ModelMetadata modelMetadata;
    
	public KryoSerializer(MapProxyFactory proxyFactory, ModelMetadata modelMetadata) {
		this(proxyFactory, modelMetadata, 1024);
	}
    
	public KryoSerializer(MapProxyFactory proxyFactory, ModelMetadata modelMetadata, int capacity) {
		this(proxyFactory, modelMetadata, capacity, capacity);
	}
    
	public KryoSerializer(MapProxyFactory proxyFactory, ModelMetadata modelMetadata, final int initialCapacity, final int maxCapacity) {
		this.proxyFactory = proxyFactory;
		this.modelMetadata = modelMetadata;
		
		this.kryo = createKryo();

		this.objectBuffer = new ThreadLocal<ObjectBuffer>() {
			protected ObjectBuffer initialValue () {
				return new ObjectBuffer(kryo, initialCapacity, maxCapacity);
			}
		};
	}
	
	public MapProxy deserialize(byte[] data) {
		@SuppressWarnings("unchecked")
		Map<Integer, Object> rawData = (Map<Integer, Object>)objectBuffer.get().readObjectData(data, InvocationHandler.class);
		return proxyFactory.createMapProxy(rawData);
	}

	public byte[] serialize(MapProxy mapProxy) {
		return objectBuffer.get().writeObjectData(mapProxy);
	}

	private Kryo createKryo() {
		Kryo kryo = new Kryo();
		
		for (Class<?> clazz : supportedClasses)
			kryo.register(clazz);
		
		//kryo.register(InvocationHandler.class, new MapProxyKryoSerializer(kryo));
		kryo.register(InvocationHandler.class, new AdvancedMapProxyKryoSerializer(kryo, modelMetadata));
		
		return kryo;
	}
}
