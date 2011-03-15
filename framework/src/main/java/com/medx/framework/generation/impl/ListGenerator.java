package com.medx.framework.generation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.medx.framework.generation.GenerationContext;
import com.medx.framework.generation.Generator;
import com.medx.framework.metadata.ClassKey;

public class ListGenerator implements Generator<List<?>> {
	@Override
	public List<List<?>> generate(ClassKey classKey, GenerationContext context) {
		Generator<?> elementGenerator = context.getGenerator(classKey.getElementKey());
		
		List<?> elements = elementGenerator.generate(classKey.getElementKey(), context);
		
		List<List<?>> result = new ArrayList<List<?>>();
		
		result.add(null);
		result.add(new ArrayList<Object>(0));
		
		for (Object element : elements)
			result.add(new ArrayList<Object>(Collections.singletonList(element)));
		
		if (elements.size() > 1)
			result.add(new ArrayList<Object>(elements));
		
		return result;
	}
}
