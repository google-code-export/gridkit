package com.medx.framework.generation;

import java.util.List;

import com.medx.framework.metadata.ClassKey;

public interface Generator<T> {
	List<? extends T> generate(ClassKey classKey, GenerationContext context);
}
