package com.medx.framework.attribute;

import static java.lang.String.format;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.medx.framework.util.ClassUtil;

public class XmlAttrKeyRegistry implements AttrKeyRegistry {
	private static final Logger log = LoggerFactory.getLogger(XmlAttrKeyRegistry.class);
	
	private static final Map<String, Class<?>> typeShortcuts = new HashMap<String, Class<?>>();
	
	static {
		typeShortcuts.put("BOOL", Boolean.class);
		typeShortcuts.put("BOOLEAN", Boolean.class);
		typeShortcuts.put("BYTE", Byte.class);
		typeShortcuts.put("INT", Integer.class);
		typeShortcuts.put("INTEGER", Integer.class);
		typeShortcuts.put("LONG", Long.class);
		typeShortcuts.put("CHAR", Character.class);
		typeShortcuts.put("CHARACTER", Character.class);
		typeShortcuts.put("STRING", String.class);
		typeShortcuts.put("FLOAT", Float.class);
		typeShortcuts.put("DOUBLE", Double.class);
	}
	
	private ConcurrentMap<Integer, AttrKey<?>> attrKeyById = new ConcurrentHashMap<Integer, AttrKey<?>>();
	private ConcurrentMap<String, AttrKey<?>> attrKeyByName = new ConcurrentHashMap<String, AttrKey<?>>();

	public XmlAttrKeyRegistry(InputStream... inputStreams) {
		for (InputStream is : inputStreams)
			loadDictionary(is);
	}
	
	public void loadDictionary(InputStream is) {
		Builder parser = new Builder();
		
		Document dictionary = null;
		
		try {
			dictionary = parser.build(is);
		} catch (Exception e) {
			log.warn(format("Failed to load dictionary from InputStream"), e);
		}
		
		parseNodes(dictionary.query("/attributes/attribute[@id and name and type]"));
	}
	
	private synchronized void parseNodes(Nodes nodes) {
		for (int i=0; i < nodes.size(); ++i) {
			Element node = (Element)nodes.get(i);
			
			int id = Integer.valueOf(node.getAttribute("id").getValue());
			
			if (attrKeyById.containsKey(id)) {
				log.warn(format("AttrKey with id '%d' already presented", i));
				continue;
			}
			
			String name = node.getChildElements("name").get(0).getValue();
			
			if (attrKeyByName.containsKey(name)) {
				log.warn(format("AttrKey with name '%s' already presented", name));
				continue;
			}
			
			String clazz = ClassUtil.getRawType(node.getChildElements("type").get(0).getValue());
			Class<?> type = null;
			
			if (typeShortcuts.containsKey(clazz.toUpperCase()))
				type = typeShortcuts.get(clazz.toUpperCase());
			else
				try {
					type = Class.forName(clazz);
				} catch (ClassNotFoundException e) {
					log.warn(format("Failded to find type '%s'", clazz));
					continue;
				}
			
			String description = null;
			
			if (node.getChildElements("description").size() > 0)
				description = node.getChildElements("description").get(0).getValue();
			
			AttrKey<?> attrKey = new AttrKey<Object>(id, name, 0, type, description);
			
			attrKeyById.put(id, attrKey);
			attrKeyByName.put(name, attrKey);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> AttrKey<T> getAttrKey(int id) {
		return (AttrKey<T>)attrKeyById.get(id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> AttrKey<T> getAttrKey(String name) {
		return (AttrKey<T>)attrKeyByName.get(name);
	}
}
