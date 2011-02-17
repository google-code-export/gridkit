package com.medx.proxy.wrapper;

import java.util.ArrayList;
import java.util.List;

import com.medx.util.CastUtil;

public class ListWrapper implements CompositeWrapper {
	public boolean isWrappable(Object object) {
		return object != null && List.class.isInstance(object);
	}
	
	public List<?> wrap(Object object, ObjectWrapper objectWrapper) {
		List<Object> list = CastUtil.cast(object);
		
		ArrayList<Object> result = new ArrayList<Object>(list.size());
		
		for (Object element : list)
			result.add(objectWrapper.wrap(element));
		
		return result;
	}
}
