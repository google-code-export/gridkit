package com.medx.framework.generation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.medx.framework.generation.GenerationContext;
import com.medx.framework.generation.Generator;
import com.medx.framework.metadata.ClassKey;

public class MapGenerator implements Generator<Map<?, ?>> {
	@Override
	public List<Map<?, ?>> generate(ClassKey classKey, GenerationContext context) {
		Generator<?> keyGenerator = context.getGenerator(classKey.getMapElementKey().getKeyClassKey());
		Generator<?> valueGenerator = context.getGenerator(classKey.getMapElementKey().getValueClassKey());
		
		List<?> keys = keyGenerator.generate(classKey.getMapElementKey().getKeyClassKey(), context);
		List<?> values = valueGenerator.generate(classKey.getMapElementKey().getValueClassKey(), context);
		
		List<Map<?, ?>> result = new ArrayList<Map<?, ?>>();
		
		result.add(null);
		result.add(new HashMap<Object, Object>());
		
		Map<Object, Object> bigMap = new HashMap<Object, Object>();
		
		for (Object key : keys)
			for (Object value : values)
				if (key != null) {
					result.add(new HashMap<Object, Object>(Collections.singletonMap(key, value)));
					bigMap.put(key, value);
				}
		
		if (bigMap.size() > 1)
			result.add(bigMap);
		
		return result;
	}
}
