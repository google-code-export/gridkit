package com.medx.framework.generation.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.medx.framework.generation.GenerationContext;
import com.medx.framework.generation.Generator;
import com.medx.framework.metadata.ClassKey;

public class StandardAndPrimitiveGenerator implements Generator<Object> {
	private static final Map<Class<?>, List<Object>> resultMap = new HashMap<Class<?>, List<Object>>();
	
	static {
		resultMap.put(Boolean.class, Arrays.<Object>asList(null, true, false));
		resultMap.put(Byte.class, Arrays.<Object>asList(null, (byte)0, (byte)1));
		resultMap.put(Character.class, Arrays.<Object>asList(null, 'a', 'z'));
		resultMap.put(Short.class, Arrays.<Object>asList(null, (short)0, (short)1));
		resultMap.put(Integer.class, Arrays.<Object>asList(null, (int)0, (int)1));
		resultMap.put(Long.class, Arrays.<Object>asList(null, (long)0, (long)1));
		resultMap.put(Float.class, Arrays.<Object>asList(null, (float)0.0, (long)1));
		resultMap.put(Double.class, Arrays.<Object>asList(null, (double)0.0, (double)1));
		resultMap.put(String.class, Arrays.<Object>asList(null, "", "a", "abc"));

		resultMap.put(boolean.class, Arrays.<Object>asList(false, true));
		resultMap.put(byte.class, Arrays.<Object>asList((byte)0, (byte)1));
		resultMap.put(char.class, Arrays.<Object>asList('a', 'z'));
		resultMap.put(short.class, Arrays.<Object>asList((short)0, (short)1));
		resultMap.put(int.class, Arrays.<Object>asList((int)0, (int)1));
		resultMap.put(long.class, Arrays.<Object>asList((long)0, (long)1));
		resultMap.put(float.class, Arrays.<Object>asList((float)0.0, (long)1));
		resultMap.put(double.class, Arrays.<Object>asList((double)0.0, (double)1));
	}
	
	@Override
	public List<Object> generate(ClassKey classKey, GenerationContext context) {
		return resultMap.get(classKey.getJavaClass());
	}
}
