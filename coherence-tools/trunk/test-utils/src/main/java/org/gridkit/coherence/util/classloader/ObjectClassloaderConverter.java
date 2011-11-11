package org.gridkit.coherence.util.classloader;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ObjectClassloaderConverter {

	private String[] include;
	private String[] exclude;
	
	private final ClassLoader clFrom;
	private final ClassLoader clTo;
	
	private final Map<Class, Converter> classMap = new HashMap<Class, Converter>();
	
	private ObjectClassloaderConverter(ClassLoader clFrom, ClassLoader clTo, String[] include, String[] exclude) {
		this.clFrom = clFrom;
		this.clTo = clTo;
		this.include = include;
		this.exclude = exclude;
	}
	
	public Object convert() {
		
	}
	
	private Converter resolveConverter(Class<?> type) {
		
	}

	private Field[] collectFields(Class<?> c) {
		List<Field> result = new ArrayList<Field>();
		collectFields(result, c);
		return result.toArray(new Field[result.size()]);
	}
	
	private void collectFields(List<Field> result, Class<?> c) {
		Class<?> s = c.getSuperclass();
		if (s != Object.class) {
			collectFields(result, s);
		}
		for(Field f: c.getDeclaredFields()) {
			if (!Modifier.isStatic(f.getModifiers())) {
				result.add(f);
			}
		}
	}

	private Field lookupField(String scope, String fname, Class<?> c) {
		if (!scope.equals(c.getName())) {
			if (c != Object.class) {
				return lookupField(scope, fname, c.getSuperclass());
			}
			else {
				return null;
			}
		}
		else {
			
		}
	}
	
	
	private interface Converter {
		
		Object convert(Object object);
		
	}
	
	private class NoConvertion implements Converter {
		@Override
		public Object convert(Object object) {
			return object;
		}
	}
	
	private abstract class ArrayConverter implements Converter {
		
	}
	
	private abstract class ReflectionConverter implements Converter {
		
		private Field[] in;
		private Field[] out;
		
		public ReflectionConverter(Class from, Class to) {
			
			in = collectFields(from);
			out = new Field[in.length];
			
			
		}
		
	}
}
