package com.medx.framework.proxy.serialization;

import com.medx.framework.proxy.MapProxy;

public interface MapProxySerializer<T> {
	T serialize(MapProxy mapProxy);
	
	MapProxy deserialize(T data);
}
