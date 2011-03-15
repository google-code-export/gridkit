package com.medx.framework.proxy.serialization.kryo;

import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.IntSerializer;
import com.medx.framework.bean.Bean;
import com.medx.framework.bean.BeanManager;
import com.medx.framework.metadata.ClassKeyType;
import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.metadata.TypedAttrKey;

//TODO implement key compaction using delta with minimum key value
public class MapProxyKryoSerializer extends Serializer {
	private final Kryo kryo;
	
	private final ModelMetadata modelMetadata;
	
	public MapProxyKryoSerializer(Kryo kryo, ModelMetadata modelMetadata) {
		this.kryo = kryo;
		this.modelMetadata = modelMetadata;
	}
	
	@Override
	@SuppressWarnings("static-access")
	public void writeObjectData(ByteBuffer buffer, Object object) {
		@SuppressWarnings("unchecked")
		Map<Object, Integer> identityMap = (Map<Object, Integer>) kryo.getContext().getTemp(KryoSerializer.IDENTITY_MAP);
		int objectCounter = (Integer) kryo.getContext().getTemp(KryoSerializer.OBJECT_COUNTER);
		
		object = Proxy.isProxyClass(object.getClass()) ? ((Bean)object).asMap() : object;
		
		if (identityMap.containsKey(object)) {
			IntSerializer.put(buffer, identityMap.get(object), true);
		}
		else {
			IntSerializer.put(buffer, objectCounter, true);
			kryo.getContext().putTemp(KryoSerializer.OBJECT_COUNTER, objectCounter + 1);
			identityMap.put(object, objectCounter);
			
			@SuppressWarnings("unchecked")
			Map<Integer, Object> backendMap = (Map<Integer, Object>) object;
			writeMapProxyData(buffer, backendMap);
		}
	}
	
	@Override
	public <T> T readObjectData(ByteBuffer buffer, Class<T> type) {
		@SuppressWarnings({"static-access", "unchecked"})
		Map<Integer, Object> objectMap = (Map<Integer, Object>)kryo.getContext().getTemp(KryoSerializer.OBJECT_MAP);
		
		Object result = null;
		
		int objectId = IntSerializer.get(buffer, true);
		
		if (objectMap.containsKey(objectId))
			result = objectMap.get(objectId);
		else {
			Map<Integer, Object> backendMap = new HashMap<Integer, Object>();
			objectMap.put(objectId, backendMap);
			readMapProxyData(buffer, backendMap);
			result = backendMap;
		}
		
		@SuppressWarnings("unchecked")
		T tResult = (T) result;
		
		return tResult;
	}
	
	public void writeMapProxyData(ByteBuffer buffer, Map<Integer, Object> backendMap) {
        int length = backendMap.size() - 1; // minus one for BeanManager.BEAN_KEY
        
        IntSerializer.put(buffer, length, true);
        
        if (length == 0)
        	return;
        
        for (Map.Entry<Integer, Object> entry : backendMap.entrySet()) {
        	int key = entry.getKey();
        	
        	if (BeanManager.BEAN_KEY.equals(key))
        		continue;
        	
        	IntSerializer.put(buffer, key, true);
        	
        	if (modelMetadata.getClassKey(key) != null)
        		continue;
        	
        	IntSerializer intSerializer = new IntSerializer(true);
        	
        	if (modelMetadata.getAttrKey(key).getClassKey().getType() == ClassKeyType.ENUM)
        		intSerializer.writeObject(buffer, entry.getValue() == null ? null : ((Enum<?>)entry.getValue()).ordinal());
        	else if (modelMetadata.getClassKey(modelMetadata.getAttrKey(key).getClassKey().getJavaClass()) != null)
        		writeObject(buffer, entry.getValue());
        	else
        		kryo.writeClassAndObject(buffer, entry.getValue());
        }
	}
	
	public void readMapProxyData(ByteBuffer buffer, Map<Integer, Object> backendMap) {
		int length = IntSerializer.get(buffer, true);
        
		for (int i = 0; i < length; i++) {
			Integer key = IntSerializer.get(buffer, true);
			
			if (modelMetadata.getClassKey(key) != null) {
				backendMap.put(key, Boolean.TRUE);
			}
			else {
				Object value = null;
				
				TypedAttrKey attrKey = modelMetadata.getAttrKey(key);
				
				IntSerializer intSerializer = new IntSerializer(true);
				
				if (attrKey.getClassKey().getType() == ClassKeyType.ENUM) {
					Integer ordinal = intSerializer.readObject(buffer, Integer.class);
					value = ordinal == null ? null : attrKey.getClassKey().getJavaClass().getEnumConstants()[ordinal];
				}
				else if (modelMetadata.getClassKey(attrKey.getClassKey().getJavaClass()) != null)
					value = readObject(buffer, null);
				else
					value = kryo.readClassAndObject(buffer);
				
				backendMap.put(key, value);
			}
        }
		
		backendMap.put(BeanManager.BEAN_KEY, Boolean.TRUE);
	}
}
