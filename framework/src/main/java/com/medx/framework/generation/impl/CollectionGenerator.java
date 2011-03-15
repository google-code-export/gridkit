package com.medx.framework.generation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.medx.framework.generation.GenerationContext;
import com.medx.framework.generation.Generator;
import com.medx.framework.metadata.ClassKey;

public class CollectionGenerator implements Generator<Collection<?>> {
	private final Generator<Set<?>> setGenerator = new SetGenerator();
	private final Generator<List<?>>  listGenerator = new ListGenerator();
	
	@Override
	public List<Collection<?>> generate(ClassKey classKey, GenerationContext context) {
		List<Collection<?>> result = new ArrayList<Collection<?>>();
		
		result.addAll(setGenerator.generate(classKey, context));
		result.addAll(listGenerator.generate(classKey, context));
		
		return result;
	}
}
