package org.gridkit.lab.tentacle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SampleSchema {

	private List<Method> methods;
	
	public SampleSchema(Class<? extends Sample> type) {
		Map<String, Method> methods = new LinkedHashMap<String, Method>();
		collectMethods(methods, type);
		this.methods = Collections.unmodifiableList(new ArrayList<Method>(methods.values()));
	}

	private void collectMethods(Map<String, Method> methods, Class<?> type) {
		if (type != null && type != Object.class) {
			collectMethods(methods, type.getSuperclass());
			for(Class<?> c: type.getInterfaces()) {
				collectMethods(methods, c);
			}
			if (type.isInterface()) {
				for(Method m: type.getDeclaredMethods()) {
					if (m.getParameterTypes().length == 0 && m.getReturnType() != void.class) {
						if (!methods.containsKey(m.getName())) {
							methods.put(m.getName(), m);
						}
					}
				}
			}
		}
	}

	public List<Method> getMethods() {
		return methods;
	}
	
	public Object[] extract(Sample target) {
		Object[] projection = new Object[methods.size()];
		for(int i = 0; i != projection.length; ++i) {
			try {
				projection[i] = methods.get(i).invoke(target);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return projection;
	}	
}
