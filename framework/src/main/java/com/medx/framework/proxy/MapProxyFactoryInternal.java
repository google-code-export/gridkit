package com.medx.framework.proxy;

import com.medx.framework.bean.BeanManager;
import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.proxy.handler.MethodHandlerFactory;

interface MapProxyFactoryInternal extends BeanManager {
	ModelMetadata getModelMetadata();
	
	MethodHandlerFactory getMethodHandlerFactory();
}
