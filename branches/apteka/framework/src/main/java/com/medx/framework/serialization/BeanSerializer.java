package com.medx.framework.serialization;

import com.medx.framework.bean.Bean;

public interface BeanSerializer<T> {
	T serialize(Bean bean);
	
	Bean deserialize(T data);
}
