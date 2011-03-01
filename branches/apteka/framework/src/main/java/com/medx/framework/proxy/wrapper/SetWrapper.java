package com.medx.framework.proxy.wrapper;

import java.util.HashSet;
import java.util.Set;

import com.medx.framework.util.CastUtil;

public class SetWrapper implements CompositeWrapper {
	public boolean isWrappable(Object object) {
		return object != null && Set.class.isInstance(object);
	}

	public Set<?> wrap(Object object, ObjectWrapper objectWrapper) {
		Set<Object> set = CastUtil.cast(object);
		
		Set<Object> result = new HashSet<Object>();
		
		for (Object element : set)
			result.add(objectWrapper.wrap(element));
		
		return result;
	}
}
