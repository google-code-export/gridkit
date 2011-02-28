package com.medx.proxy.handler;

import java.lang.reflect.Method;

import com.medx.attribute.AttrKey;
import com.medx.attribute.AttrKeyRegistry;
import com.medx.util.DictUtil;
import com.medx.util.TextUtil;

public class MethodHandlerUtil {
	public static MethodHandler createMethodHandler(String camelPrefix, int attributeId) {
		if (GetMethodHandler.getPrefix().equals(camelPrefix))
			return new GetMethodHandler(attributeId);
		else if (SetMethodHandler.getPrefix().equals(camelPrefix))
			return new SetMethodHandler(attributeId);
		else
			throw new RuntimeException("Unknown camel prefix - " + camelPrefix);
	}
	
	public static AttrKey<?> getAttrKey(Method method, AttrKeyRegistry attrKeyRegistry) {
		String attrName = TextUtil.getCamelPostfix(method.getName());
		
		return attrKeyRegistry.getAttrKey(DictUtil.getAttrName(method.getDeclaringClass(), attrName));
	}
}
