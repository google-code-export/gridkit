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
import com.medx.framework.proxy.MapProxy;
import com.medx.framework.proxy.MapProxyFactory;
import com.medx.framework.proxy.serialization.MapProxyBinarySerializer;

public class KryoSerializer implements MapProxyBinarySerializer {
    private static final Class<?>[] supportedClasses = {boolean[].class, byte[].class, char[].class, short[].class,
    	int[].class, long[].class, float[].class, double[].class, Boolean[].class, Byte[].class, Character[].class,
    	Short[].class, Long[].class, Float[].class, Double[].class, String[].class, ArrayList.class, LinkedList.class, 
    	HashMap.class, TreeMap.class, HashSet.class, TreeSet.class};

    private final Kryo kryo;
    
    private ThreadLocal<ObjectBuffer> objectBuffer;
    
    private final MapProxyFactory proxyFactory;
    
	public KryoSerializer(MapProxyFactory proxyFactory) {
		this(proxyFactory, 1024);
	}
    
	public KryoSerializer(MapProxyFactory proxyFactory, int capacity) {
		this(proxyFactory, capacity, capacity);
	}
    
	public KryoSerializer(MapProxyFactory proxyFactory, final int initialCapacity, final int maxCapacity) {
		this.proxyFactory = proxyFactory;
		
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

	private static Kryo createKryo() {
		Kryo kryo = new Kryo();
		
		for (Class<?> clazz : supportedClasses)
			kryo.register(clazz);
		
		kryo.register(InvocationHandler.class, new MapProxyKryoSerializer(kryo));
		
		return kryo;
	}
}
