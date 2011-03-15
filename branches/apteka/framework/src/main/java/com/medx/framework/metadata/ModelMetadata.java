package com.medx.framework.metadata;

import java.util.List;
import java.util.Set;

public interface ModelMetadata {
	TypedAttrKey getAttrKey(int id);
	TypedAttrKey getAttrKey(String name);
	
	ClassKey getClassKey(int id);
	ClassKey getClassKey(Class<?> clazz);
	
	List<TypedAttrKey> getAttrKeys(Class<?> clazz);
	
	Set<Integer> getTypeIds(Set<Integer> candidates);
}
