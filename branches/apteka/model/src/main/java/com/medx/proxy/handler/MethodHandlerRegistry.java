package com.medx.proxy.handler;

import java.lang.reflect.Method;

public interface MethodHandlerRegistry {
	MethodHandler getMethodHandler(Method method);
}
