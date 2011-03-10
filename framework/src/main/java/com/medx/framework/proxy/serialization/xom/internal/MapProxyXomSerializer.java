package com.medx.framework.proxy.serialization.xom.internal;

import java.lang.reflect.Proxy;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Element;

import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.proxy.MapProxy;
import com.medx.framework.proxy.MapProxyFactory;
import com.medx.framework.proxy.serialization.xom.InternalXomSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializationContext;

public class MapProxyXomSerializer implements InternalXomSerializer<Object> {
	private final ModelMetadata modelMetadata;
	
	public MapProxyXomSerializer(ModelMetadata modelMetadata) {
		this.modelMetadata = modelMetadata;
	}

	@Override
	public Element serialize(Object mapProxy, XomSerializationContext context) {
		@SuppressWarnings("unchecked")
		Map<Integer, Object> backendMap = Proxy.isProxyClass(mapProxy.getClass()) ? ((MapProxy)mapProxy).getBackendMap() : (Map<Integer, Object>)mapProxy;
		
		if (context.getIdentityMap().containsKey(backendMap)) {
			Element result = new Element("object");
			result.addAttribute(new Attribute("idref", Integer.valueOf(context.getIdentityMap().get(backendMap)).toString()));
			return result;
		}
		else {
			int objectId = context.getNextObjectId();
			context.getIdentityMap().put(backendMap, objectId);
			return serializeBackendMap(backendMap, objectId, context);
		}
	}
	
	private Element serializeBackendMap(Map<Integer, Object> backendMap, int objectId, XomSerializationContext context) {
		Element result = new Element("object");

		result.addAttribute(new Attribute("id", Integer.valueOf(objectId).toString()));

		Element classes = new Element("classes");
		Element attributes = new Element("attributes");
			
		result.appendChild(classes);
		result.appendChild(attributes);
			
		for (Map.Entry<Integer, Object> entry : backendMap.entrySet()) {
			int key = entry.getKey();
	        	
	       	if (MapProxyFactory.PROXIABLE_KEY.equals(key))
	       		continue;
	       	
	       	if (modelMetadata.getTypeKey(key) != null) {
	       		Element classElement = new Element("class");
	       		classes.appendChild(classElement);
	       		
	       		classElement.appendChild(modelMetadata.getTypeKey(key).getClazz().getCanonicalName());
	       	}
	        else if (modelMetadata.getAttrKey(key) != null) {
	        	Element attributeElement = new Element("attribute");
	        	attributeElement.addAttribute(new Attribute("name", modelMetadata.getAttrKey(key).getName()));
	        	attributes.appendChild(attributeElement);
	        	
	        	InternalXomSerializer<Object> attributeSerializer = context.getXomSerializer(entry.getValue());
	        	attributeElement.appendChild(attributeSerializer.serialize(entry.getValue(), context));
	        }
	        else
	        	throw new IllegalArgumentException("mapProxy");
		}
		
		return result;
	}
}
