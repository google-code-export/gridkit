package com.medx.framework.generation.impl;

import java.util.ArrayList;
import java.util.List;

import com.medx.framework.generation.GenerationContext;
import com.medx.framework.generation.Generator;
import com.medx.framework.metadata.ClassKey;
import com.medx.framework.util.CastUtil;

public class EnumGenerator implements Generator<Enum<?>> {
	@Override
	public List<Enum<?>> generate(ClassKey classKey, GenerationContext context) {
		Object[] constants = classKey.getJavaClass().getEnumConstants();
		
		List<Enum<?>> result = new ArrayList<Enum<?>>();
		
		result.add(null);
		
		if (constants.length > 0)
			result.add(CastUtil.<Enum<?>>cast(constants[0]));
		
		return result;
	}
}
