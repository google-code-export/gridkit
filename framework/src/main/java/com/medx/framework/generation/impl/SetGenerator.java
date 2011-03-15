package com.medx.framework.generation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.medx.framework.generation.GenerationContext;
import com.medx.framework.generation.Generator;
import com.medx.framework.metadata.ClassKey;

public class SetGenerator implements Generator<Set<?>> {
	@Override
	public List<Set<?>> generate(ClassKey classKey, GenerationContext context) {
		Generator<?> elementGenerator = context.getGenerator(classKey.getElementKey());
		
		List<?> elements = elementGenerator.generate(classKey.getElementKey(), context);
		
		List<Set<?>> result = new ArrayList<Set<?>>();
		
		result.add(null);
		result.add(new HashSet<Object>(0));
		
		HashSet<Object> bigSet = new HashSet<Object>();
		
		for (Object element : elements)
			if (element != null) {
				result.add(new HashSet<Object>(Collections.singletonList(element)));
				bigSet.add(element);
			}
		
		if (bigSet.size() > 1)
			result.add(bigSet);
		
		return result;
	}
}
