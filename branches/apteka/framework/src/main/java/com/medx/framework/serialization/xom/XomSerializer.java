package com.medx.framework.serialization.xom;

import static com.medx.framework.util.CastUtil.cast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import com.medx.framework.bean.Bean;
import com.medx.framework.bean.BeanManager;
import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.serialization.BeanSerializer;
import com.medx.framework.serialization.xom.internal.ArrayXomSerializer;
import com.medx.framework.serialization.xom.internal.EnumXomSerializer;
import com.medx.framework.serialization.xom.internal.ListXomSerializer;
import com.medx.framework.serialization.xom.internal.MapProxyXomSerializer;
import com.medx.framework.serialization.xom.internal.MapXomSerializer;
import com.medx.framework.serialization.xom.internal.NullXomSerializer;
import com.medx.framework.serialization.xom.internal.PrimitiveXomSerializer;
import com.medx.framework.serialization.xom.internal.SetXomSerializer;

public class XomSerializer implements BeanSerializer<String>, XomSerializationContext {
	private final ThreadLocal<Integer> objectCounter = new ThreadLocal<Integer>();
	private final ThreadLocal<Map<Object, Integer>> identityMap = new ThreadLocal<Map<Object,Integer>>();
	private final ThreadLocal<Map<Integer, Object>> objectMap = new ThreadLocal<Map<Integer, Object>>();
	
	@SuppressWarnings("unchecked")
	private final EnumXomSerializer<?> enumSerializer = new EnumXomSerializer();
	private final ArrayXomSerializer arraySerializer = new ArrayXomSerializer();
	private final ListXomSerializer<?> listSerializer = new ListXomSerializer<Object>();
	private final MapXomSerializer<?, ?> mapSerializer = new MapXomSerializer<Object, Object>();
	private final SetXomSerializer<?> setSerializer = new SetXomSerializer<Object>();
	private final PrimitiveXomSerializer primitiveSerializer = new PrimitiveXomSerializer();
	private final NullXomSerializer nullSerializer = new NullXomSerializer();
	
	private final MapProxyXomSerializer mapProxySerializer;
	
	private final BeanManager proxyFactory;
	
	public XomSerializer(BeanManager proxyFactory, ModelMetadata modelMetadata) {
		this.proxyFactory = proxyFactory;
		this.mapProxySerializer = new MapProxyXomSerializer(modelMetadata);
	}
	
	@Override
	public String serialize(Bean mapProxy) {
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
	
	private String serializeInternal(Bean mapProxy) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Serializer serializer = new Serializer(outputStream, "UTF-8");
		serializer.setIndent(4);
		serializer.write(new Document(mapProxySerializer.serialize(mapProxy, this)));
		
		outputStream.close();
		
		String result = outputStream.toString("UTF-8");
		
		return result.substring(result.indexOf("\n") + 1, result.length() - 1);
	}
	
	@Override
	public Bean deserialize(String data) {
		objectMap.set(new HashMap<Integer, Object>());
		
		Bean result = null;
		
		try {
			result = proxyFactory.createBean(deserializeInternal(data));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		objectMap.set(null);
		
		return result;
	}
	
	public Map<Integer, Object> deserializeInternal(String data) throws ValidityException, ParsingException, IOException {
		Builder parser = new Builder();
		
		Document doc = parser.build(new StringReader(data));
		
		Map<Integer, Object> result = cast(mapProxySerializer.deserialize(doc.getRootElement(), this));
		
		return result;
	}
	
	@Override
	public <T> InternalXomSerializer<T> getXomSerializer(T object) {
		if (object == null)
			return cast(nullSerializer);
		
		Class<?> clazz = object.getClass();
		
		if (Proxy.isProxyClass(clazz) || proxyFactory.isBeanMap(object))
			return cast(mapProxySerializer);
		else if (clazz.isArray())
			return cast(arraySerializer);
		else if (clazz.isEnum())
			return cast(enumSerializer);
		else if (List.class.isInstance(object))
			return cast(listSerializer);
		else if (Map.class.isInstance(object))
			return cast(mapSerializer);
		else if (Set.class.isInstance(object))
			return cast(setSerializer);
		else if (PrimitiveXomSerializer.supportedClasses.contains(clazz))
			return cast(primitiveSerializer);
		else
			throw new IllegalArgumentException("object");
	}
	
	@Override
	public <T> InternalXomSerializer<T> getXomSerializer(Element element) {
		String tag = element.getLocalName().toLowerCase();
		
		if (MapProxyXomSerializer.TAG.equals(tag))
			return cast(mapProxySerializer);
		else if (NullXomSerializer.TAG.equals(tag))
			return cast(nullSerializer);
		else if (ArrayXomSerializer.TAG.equals(tag))
			return cast(arraySerializer);
		else if (EnumXomSerializer.TAG.equals(tag))
			return cast(enumSerializer);
		else if (ListXomSerializer.TAG.equals(tag))
			return cast(listSerializer);
		else if (MapXomSerializer.TAG.equals(tag))
			return cast(mapSerializer);
		else if (SetXomSerializer.TAG.equals(tag))
			return cast(setSerializer);
		else if (PrimitiveXomSerializer.supportedTags.contains(tag))
			return cast(primitiveSerializer);
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
