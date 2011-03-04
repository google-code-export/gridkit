package com.medx.framework.proxy.serialization.kryo;

import java.lang.reflect.InvocationHandler;
import java.util.HashMap;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.MapSerializer;

public class MapProxyKryoSerializer extends MapSerializer {
	public MapProxyKryoSerializer(Kryo kryo) {
		super(kryo);
		
		setKeyClass(Integer.class);
		setKeysCanBeNull(false);
		
		setValueClass(null);
		setValuesCanBeNull(true);
	}
	
	@Override
	public <T> T newInstance (Kryo kryo, Class<T> type) {
		if (type != InvocationHandler.class)
			throw new IllegalArgumentException("");
		
		@SuppressWarnings("unchecked")
		T result = (T) new HashMap<Integer, Object>();
		
        return result;
	}
}
