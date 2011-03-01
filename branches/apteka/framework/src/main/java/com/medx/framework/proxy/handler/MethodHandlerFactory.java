package com.medx.framework.proxy.handler;

import java.lang.reflect.Method;

public interface MethodHandlerFactory {
	MethodHandler getMethodHandler(Method method);
}
