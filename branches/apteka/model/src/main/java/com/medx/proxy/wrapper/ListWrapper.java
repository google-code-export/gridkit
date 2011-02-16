package com.medx.proxy.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.medx.proxy.MapProxyFactory;
import com.medx.util.CastUtil;

public class ListWrapper implements Wrapper {
	public boolean isApplicable(Object object) {
		return object != null && object.getClass().isInstance(List.class);
	}
	
	public List<Object> wrap(Object list_) {
		List<Object> list = CastUtil.<List<Object>>cast(list_);
		
		ArrayList<Object> result = new ArrayList<Object>(list.size());
		
		MapProxyFactory proxyFactory = MapProxyFactory.Instance.getInstance();
		
		for (Object element : list)
			if (proxyFactory.isWrappable(element))
				result.add(proxyFactory.createProxy(CastUtil.<Map<Integer, Object>>cast(element)));
			else
				result.add(element);
		
		return result;
	}
}
