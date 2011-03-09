package com.medx.framework.proxy.serialization.kryo;

import java.nio.ByteBuffer;
import java.util.HashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.MapSerializer;
import com.medx.framework.proxy.MapProxyFactory;

public class MapKryoSerializer extends MapSerializer {
	private static final byte MAP_PROXY = 0;
	private static final byte GENERAL_MAP = 1;
	
	private final MapProxyFactory proxyFactory;
	private final Serializer mapProxySerializer;
	
	public MapKryoSerializer(Kryo kryo, MapProxyFactory proxyFactory, Serializer mapProxySerializer) {
		super(kryo);
		
		this.proxyFactory = proxyFactory;
		this.mapProxySerializer = mapProxySerializer;
		
		setKeyClass(null);
		setKeysCanBeNull(true);
		
		setValueClass(null);
		setValuesCanBeNull(true);
	}
	
	@Override
	public void writeObjectData (ByteBuffer buffer, Object object) {
		if (!proxyFactory.isProxiable(object)) {
			buffer.put(GENERAL_MAP);
			super.writeObjectData(buffer, object);
		}
		else {
			buffer.put(MAP_PROXY);
			mapProxySerializer.writeObjectData(buffer, object);
		}
	}
	
	public <T> T readObjectData (ByteBuffer buffer, Class<T> type) {
		byte flag = buffer.get();
		
		if (flag == GENERAL_MAP)
			return super.readObjectData(buffer, type);
		else if (flag == MAP_PROXY)
			return mapProxySerializer.readObjectData(buffer, type);
		else
			throw new IllegalArgumentException("buffer");
	}
	
	@Override
	public <T> T newInstance (Kryo kryo, Class<T> type) {
		@SuppressWarnings("unchecked")
		T result = (T) new HashMap<Integer, Object>();
		
        return result;
	}
}
