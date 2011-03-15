package com.medx.framework.serialization.xom.internal;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import com.medx.framework.bean.Bean;
import com.medx.framework.bean.BeanManager;
import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.metadata.TypedAttrKey;
import com.medx.framework.serialization.xom.InternalXomSerializer;
import com.medx.framework.serialization.xom.XomSerializationContext;

public class MapProxyXomSerializer implements InternalXomSerializer<Object> {
	public static final String TAG = "object";
	
	private final ModelMetadata modelMetadata;
	
	public MapProxyXomSerializer(ModelMetadata modelMetadata) {
		this.modelMetadata = modelMetadata;
	}

	@Override
	public Element serialize(Object mapProxy, XomSerializationContext context) {
		@SuppressWarnings("unchecked")
		Map<Integer, Object> backendMap = Proxy.isProxyClass(mapProxy.getClass()) ? ((Bean)mapProxy).asMap() : (Map<Integer, Object>)mapProxy;
		
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
	
	@Override
	public Object deserialize(Element element, XomSerializationContext context) {
		Attribute idref = element.getAttribute("idref");
		
		if (idref != null) {
			Integer objectId = Integer.valueOf(idref.getValue());
			
			if (context.getObjectMap().containsKey(objectId))
				return context.getObjectMap().get(objectId);
			else
				throw new IllegalStateException();
		}
		else {
			Attribute id = element.getAttribute("id");
			Integer objectId = Integer.valueOf(id.getValue());
			
			Map<Integer, Object> backendMap = new HashMap<Integer, Object>();
			context.getObjectMap().put(objectId, backendMap);
			deserializeBackendMap(backendMap, element, context);
			
			return backendMap;
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
	        	
	       	if (BeanManager.BEAN_KEY.equals(key))
	       		continue;
	       	
	       	if (modelMetadata.getClassKey(key) != null) {
	       		Element classElement = new Element("class");
	       		classes.appendChild(classElement);
	       		
	       		classElement.appendChild(modelMetadata.getClassKey(key).getJavaClass().getCanonicalName());
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
	
	private void deserializeBackendMap(Map<Integer, Object> backendMap, Element element, XomSerializationContext context) {
		deserializeBackendMapClasses(backendMap, element, context);
		deserializeBackendMapAttributes(backendMap, element, context);
		backendMap.put(BeanManager.BEAN_KEY, Boolean.TRUE);
	}
	
	private void deserializeBackendMapClasses(Map<Integer, Object> backendMap, Element element, XomSerializationContext context) {
		Element classesElement = element.getChildElements("classes").get(0);
		Elements classes = classesElement.getChildElements();
		
		for (int i = 0; i < classes.size(); ++i) {
			String clazz = classes.get(i).getValue();
			try {
				backendMap.put(modelMetadata.getClassKey(Class.forName(clazz)).getId(), Boolean.TRUE);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private void deserializeBackendMapAttributes(Map<Integer, Object> backendMap, Element element, XomSerializationContext context) {
		Element attributesElement = element.getChildElements("attributes").get(0);
		Elements attributes = attributesElement.getChildElements();
		
		for (int i = 0; i < attributes.size(); ++i) {
			TypedAttrKey attrKey = modelMetadata.getAttrKey(attributes.get(i).getAttributeValue("name"));
			
			InternalXomSerializer<Object> attrSerializer = context.getXomSerializer(attributes.get(i).getChildElements().get(0));
			
			backendMap.put(attrKey.getId(), attrSerializer.deserialize(attributes.get(i).getChildElements().get(0), context));
		}
	}
}
