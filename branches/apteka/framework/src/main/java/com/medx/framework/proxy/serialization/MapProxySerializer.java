package com.medx.framework.proxy.serialization;

import com.medx.framework.bean.Bean;

public interface MapProxySerializer<T> {
	T serialize(Bean mapProxy);
	
	Bean deserialize(T data);
}
