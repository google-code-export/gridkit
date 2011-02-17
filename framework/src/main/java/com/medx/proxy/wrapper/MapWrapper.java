package com.medx.proxy.wrapper;

import java.util.HashMap;
import java.util.Map;

import com.medx.util.CastUtil;

public class MapWrapper implements CompositeWrapper {
	public boolean isWrappable(Object object) {
		return object != null && Map.class.isInstance(object);
	}

	public Map<?,?> wrap(Object object, ObjectWrapper objectWrapper) {
		Map<Object, Object> map = CastUtil.cast(object);
		
		Map<Object, Object> result = new HashMap<Object, Object>();
		
		for (Map.Entry<Object, Object> entry : map.entrySet())
			result.put(objectWrapper.wrap(entry.getKey()), objectWrapper.wrap(entry.getValue()));
		
		return result;
	}
}
