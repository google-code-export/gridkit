package com.medx.type;

import static java.lang.String.format;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.medx.util.TextUtil;

public class XmlTypeRegistry implements TypeRegistry {
	private static final Logger log = LoggerFactory.getLogger(XmlTypeRegistry.class);
	
	private ConcurrentMap<Integer, Class<?>> classById = new ConcurrentHashMap<Integer, Class<?>>();
	private ConcurrentMap<String, Class<?>> classByName = new ConcurrentHashMap<String, Class<?>>();

	private ConcurrentMap<Class<?>, Integer> idByClass = new ConcurrentHashMap<Class<?>, Integer>();
	private ConcurrentMap<Class<?>, String> nameByClass = new ConcurrentHashMap<Class<?>, String>();
	
	public XmlTypeRegistry(InputStream... inputStreams) {
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
		
		parseNodes(dictionary.query("/types/type[@id and name and class]"));
	}
	
	private synchronized void parseNodes(Nodes nodes) {
		for (int i=0; i < nodes.size(); ++i) {
			Element node = (Element)nodes.get(i);
			
			int id = Integer.valueOf(node.getAttribute("id").getValue());
			
			if (classById.containsKey(id)) {
				log.warn(format("Type with id '%d' already presented", i));
				continue;
			}
			
			String name = node.getChildElements("name").get(0).getValue();
			
			if (classByName.containsKey(name)) {
				log.warn(format("Type with name '%s' already presented", name));
				continue;
			}
			
			String className = TextUtil.getRawType(node.getChildElements("class").get(0).getValue());
			Class<?> clazz = null;
			
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				log.warn(format("Failded to find type '%s'", className));
				continue;
			}
			
			classById.put(id, clazz);
			classByName.put(name, clazz);
			
			idByClass.put(clazz, id);
			nameByClass.put(clazz, name);
		}
	}
	
	@Override
	public Class<?> getType(int id) {
		return classById.get(id);
	}

	@Override
	public Class<?> getType(String name) {
		return classByName.get(name);
	}

	@Override
	public int getTypeId(Class<?> clazz) {
		return idByClass.get(clazz);
	}

	@Override
	public String getTypeName(Class<?> clazz) {
		return nameByClass.get(clazz);
	}
}
