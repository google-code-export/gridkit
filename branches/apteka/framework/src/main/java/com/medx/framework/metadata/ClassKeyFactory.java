package com.medx.framework.metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ClassKeyFactory {
	public static final class Standard {
		public static final ClassKey BOOLEAN   = new SimpleClassKey(ClassKeyType.STANDARD, Boolean.class);
		public static final ClassKey BYTE      = new SimpleClassKey(ClassKeyType.STANDARD, Byte.class);
		public static final ClassKey CHARACTER = new SimpleClassKey(ClassKeyType.STANDARD, Character.class);
		public static final ClassKey SHORT     = new SimpleClassKey(ClassKeyType.STANDARD, Short.class);
		public static final ClassKey INTEGER   = new SimpleClassKey(ClassKeyType.STANDARD, Integer.class);
		public static final ClassKey LONG      = new SimpleClassKey(ClassKeyType.STANDARD, Long.class);
		public static final ClassKey FLOAT     = new SimpleClassKey(ClassKeyType.STANDARD, Float.class);
		public static final ClassKey DOUBLE    = new SimpleClassKey(ClassKeyType.STANDARD, Double.class);
		public static final ClassKey STRING    = new SimpleClassKey(ClassKeyType.STANDARD, String.class);
	}
	
	public static final class Primitive {
		public static final ClassKey BOOLEAN = new SimpleClassKey(ClassKeyType.PRIMITIVE, boolean.class);
		public static final ClassKey BYTE    = new SimpleClassKey(ClassKeyType.PRIMITIVE, byte.class);
		public static final ClassKey CHAR    = new SimpleClassKey(ClassKeyType.PRIMITIVE, char.class);
		public static final ClassKey SHORT   = new SimpleClassKey(ClassKeyType.PRIMITIVE, short.class);
		public static final ClassKey INT     = new SimpleClassKey(ClassKeyType.PRIMITIVE, int.class);
		public static final ClassKey LONG    = new SimpleClassKey(ClassKeyType.PRIMITIVE, long.class);
		public static final ClassKey FLOAT   = new SimpleClassKey(ClassKeyType.PRIMITIVE, float.class);
		public static final ClassKey DOUBLE  = new SimpleClassKey(ClassKeyType.PRIMITIVE, double.class);
	}
	
	private static final Map<Class<?>, ClassKey> standardMap = new HashMap<Class<?>, ClassKey>();
	private static final Map<Class<?>, ClassKey> primitiveMap = new HashMap<Class<?>, ClassKey>();
	
	static {
		standardMap.put(Boolean.class, Standard.BOOLEAN);
		standardMap.put(Byte.class, Standard.BYTE);
		standardMap.put(Character.class, Standard.CHARACTER);
		standardMap.put(Short.class, Standard.SHORT);
		standardMap.put(Integer.class, Standard.INTEGER);
		standardMap.put(Long.class, Standard.LONG);
		standardMap.put(Float.class, Standard.FLOAT);
		standardMap.put(Double.class, Standard.DOUBLE);
		standardMap.put(String.class, Standard.STRING);
		
		primitiveMap.put(boolean.class, Primitive.BOOLEAN);
		primitiveMap.put(byte.class, Primitive.BYTE);
		primitiveMap.put(char.class, Primitive.CHAR);
		primitiveMap.put(short.class, Primitive.SHORT);
		primitiveMap.put(int.class, Primitive.INT);
		primitiveMap.put(long.class, Primitive.LONG);
		primitiveMap.put(float.class, Primitive.FLOAT);
		primitiveMap.put(double.class, Primitive.DOUBLE);
	}
	
	public static Map<Class<?>, ClassKey> getStandardMap() {
		return Collections.unmodifiableMap(standardMap);
	}
	
	public static Map<Class<?>, ClassKey> getPrimitiveMap() {
		return Collections.unmodifiableMap(primitiveMap);
	}
	
	public static ClassKey createUserClassKey(int id, int version, Class<?> javaClass) {
		return new UserClassKey(id, version, javaClass);
	}
	
	public static ClassKey createEnumClassKey(Class<?> enumClass) {
		if (!enumClass.isEnum())
			throw new IllegalArgumentException("enumClass");
		
		return new SimpleClassKey(ClassKeyType.ENUM, enumClass);
	}
	
	public static EntryClassKey createEntryClassKey(ClassKey keyClassKey, ClassKey valueClassKey) {
		return new EntryClassKey(keyClassKey, valueClassKey);
	}
	
	public static ClassKey createCollectionClassKey(ClassKey elementKey) {
		return new CompositeClassKey(ClassKeyType.COLLECTION, elementKey);
	}
	
	public static ClassKey createListClassKey(ClassKey elementKey) {
		return new CompositeClassKey(ClassKeyType.LIST, elementKey);
	}
	
	public static ClassKey createSetClassKey(ClassKey elementKey) {
		return new CompositeClassKey(ClassKeyType.SET, elementKey);
	}
	
	public static ClassKey createMapClassKey(EntryClassKey elementKey) {
		return new CompositeClassKey(ClassKeyType.MAP, elementKey);
	}
	
	public static ClassKey createArrayClassKey(ClassKey elementKey) {
		return new CompositeClassKey(ClassKeyType.ARRAY, elementKey);
	}
}
