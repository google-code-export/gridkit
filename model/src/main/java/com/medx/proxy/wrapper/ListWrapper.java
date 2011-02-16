package com.medx.proxy.wrapper;

import java.util.ArrayList;
import java.util.List;

import com.medx.util.CastUtil;

public class ListWrapper implements Wrapper {
	public boolean isWrappable(Object object) {
		return object != null && object.getClass().isInstance(List.class);
	}
	
	public List<?> wrap(Object object, Wrapper objectWrapper) {
		List<Object> list = CastUtil.<List<Object>>cast(object);
		
		ArrayList<Object> result = new ArrayList<Object>(list.size());
		
		for (Object element : list)
			if (objectWrapper.isWrappable(element))
				result.add(objectWrapper.wrap(element, objectWrapper));
			else
				result.add(element);
		
		return result;
	}
}
