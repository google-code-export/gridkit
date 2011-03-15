package com.medx.framework.generation;

import java.util.Set;

import com.medx.framework.metadata.ClassKey;

public interface GenerationContext {
	Set<Class<?>> getClassStack();
	
	Generator<?> getGenerator(ClassKey classKey);
}
