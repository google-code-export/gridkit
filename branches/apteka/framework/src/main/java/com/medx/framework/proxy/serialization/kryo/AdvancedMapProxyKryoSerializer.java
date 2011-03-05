package com.medx.framework.proxy.serialization.kryo;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.IntSerializer;
import com.medx.framework.attribute.AttrKey;
import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.proxy.MapProxyFactory;

public class AdvancedMapProxyKryoSerializer extends Serializer {
	private final Kryo kryo;
	
	private final ModelMetadata modelMetadata;
	
	public AdvancedMapProxyKryoSerializer(Kryo kryo, ModelMetadata modelMetadata) {
		this.kryo = kryo;
		this.modelMetadata = modelMetadata;
	}

	@Override
	public void writeObjectData(ByteBuffer buffer, Object object) {
		@SuppressWarnings("unchecked")
		Map<Integer, Object> backendMap = (Map<Integer, Object>) object;
		
		backendMap.remove(MapProxyFactory.PROXIABLE_KEY);
		
        int length = backendMap.size();
        
        IntSerializer.put(buffer, length, true);
        
        if (length == 0)
        	return;
        
        for (Map.Entry<Integer, Object> entry : backendMap.entrySet()) {
        	int key = entry.getKey();
        	
        	IntSerializer.put(buffer, key, true);
        	
        	if (modelMetadata.getTypeKey(key) != null)
        		continue;
        	
        	if (entry.getValue().getClass().isEnum())
        		IntSerializer.put(buffer, ((Enum<?>)entry.getValue()).ordinal(), true);
        	else if (modelMetadata.getTypeKey(modelMetadata.getAttrKey(key).getClazz()) != null)
        		writeObject(buffer, entry.getValue());
        	else
        		kryo.writeClassAndObject(buffer, entry.getValue());
        }
        
        backendMap.put(MapProxyFactory.PROXIABLE_KEY, Boolean.TRUE);
	}
	
	@Override
	public <T> T readObjectData(ByteBuffer buffer, Class<T> type) {
		Map<Integer, Object> backendMap = new HashMap<Integer, Object>();
		
		int length = IntSerializer.get(buffer, true);
        
		for (int i = 0; i < length; i++) {
			Integer key = IntSerializer.get(buffer, true);
			
			if (modelMetadata.getTypeKey(key) != null) {
				backendMap.put(key, Boolean.TRUE);
			}
			else {
				Object value = null;
				
				AttrKey<?> attrKey = modelMetadata.getAttrKey(key);
				
				if (attrKey.getClazz().isEnum())
					value = attrKey.getClazz().getEnumConstants()[IntSerializer.get(buffer, true)];
				else if (modelMetadata.getTypeKey(attrKey.getClazz()) != null)
					value = readObject(buffer, null);
				else
					value = kryo.readClassAndObject(buffer);
				
				backendMap.put(key, value);
			}
        }
		
		backendMap.put(MapProxyFactory.PROXIABLE_KEY, Boolean.TRUE);
		
		@SuppressWarnings("unchecked")
		T result = (T) backendMap;
		return result;
	}
}
