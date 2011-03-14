package com.medx.framework.proxy.serialization.kryo;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;
import com.esotericsoftware.kryo.Serializer;
import com.medx.framework.bean.Bean;
import com.medx.framework.bean.BeanManager;
import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.proxy.serialization.MapProxySerializer;

public class KryoSerializer implements MapProxySerializer<byte[]> {
    private static final Class<?>[] supportedClasses = {boolean[].class, byte[].class, char[].class, short[].class,
    	int[].class, long[].class, float[].class, double[].class, Boolean[].class, Byte[].class, Character[].class,
    	Short[].class, Long[].class, Float[].class, Double[].class, String[].class, ArrayList.class, LinkedList.class, 
    	HashSet.class, TreeSet.class};

    public static final String OBJECT_MAP = "OBJECT_MAP";
    public static final String IDENTITY_MAP = "IDENTITY_MAP";
    public static final String OBJECT_COUNTER = "OBJECT_COUNTER";
    
    private final Kryo kryo;
    
    private final ThreadLocal<ObjectBuffer> objectBuffer;
    
    private final BeanManager proxyFactory;
    
	private final ModelMetadata modelMetadata;
    
	public KryoSerializer(BeanManager proxyFactory, ModelMetadata modelMetadata) {
		this(proxyFactory, modelMetadata, 1024);
	}
    
	public KryoSerializer(BeanManager proxyFactory, ModelMetadata modelMetadata, int capacity) {
		this(proxyFactory, modelMetadata, capacity, capacity);
	}
    
	public KryoSerializer(BeanManager proxyFactory, ModelMetadata modelMetadata, final int initialCapacity, final int maxCapacity) {
		this.proxyFactory = proxyFactory;
		this.modelMetadata = modelMetadata;
		
		this.kryo = createKryo();

		this.objectBuffer = new ThreadLocal<ObjectBuffer>() {
			protected ObjectBuffer initialValue () {
				return new ObjectBuffer(kryo, initialCapacity, maxCapacity);
			}
		};
	}
	
	@SuppressWarnings("static-access")
	public Bean deserialize(byte[] data) {
		kryo.getContext().putTemp(OBJECT_MAP, new HashMap<Integer, Object>());
		
		@SuppressWarnings("unchecked")
		Map<Integer, Object> rawData = (Map<Integer, Object>)objectBuffer.get().readObjectData(data, InvocationHandler.class);
		
		return proxyFactory.createBean(rawData);
	}

	@SuppressWarnings("static-access")
	public byte[] serialize(Bean mapProxy) {
		kryo.getContext().putTemp(OBJECT_COUNTER, new Integer(0));
		kryo.getContext().putTemp(IDENTITY_MAP, new IdentityHashMap<Object, Integer>());
		
		return objectBuffer.get().writeObjectData(mapProxy);
	}

	private Kryo createKryo() {
		Kryo kryo = new Kryo();
		
		for (Class<?> clazz : supportedClasses)
			kryo.register(clazz);
		
		Serializer mapProxySerializer = new MapProxyKryoSerializer(kryo, modelMetadata);
		Serializer mapSerializer = new MapKryoSerializer(kryo, proxyFactory, mapProxySerializer);

		kryo.register(HashMap.class, mapSerializer);
		kryo.register(TreeMap.class, mapSerializer);
		kryo.register(InvocationHandler.class, mapProxySerializer);
		
		return kryo;
	}
}
