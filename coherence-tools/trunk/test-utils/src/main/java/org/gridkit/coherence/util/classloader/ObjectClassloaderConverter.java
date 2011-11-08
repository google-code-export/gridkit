package org.gridkit.coherence.util.classloader;

import java.lang.reflect.Field;

class ObjectClassloaderConverter {

	private String[] include;
	private String[] exclude;
	
	private final ClassLoader clFrom;
	private final ClassLoader clTo;
	
	private final Map<Class, Converter>
	
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
	
	private interface Converter {
		
		Object convert(Object object);
		
	}
	
	private class NoConvertion implements Converter {
		@Override
		public Object convert(Object object) {
			return object;
		}
	}
	
	private abstract class ReflectionConverter implements Converter {
		
		private Field[] in;
		private Field[] out;
		
		
		
	}
}
