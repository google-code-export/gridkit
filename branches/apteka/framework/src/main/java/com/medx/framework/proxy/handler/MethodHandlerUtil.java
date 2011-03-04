package com.medx.framework.proxy.handler;

import java.lang.reflect.Method;

import com.medx.framework.attribute.AttrKey;
import com.medx.framework.metadata.AttrKeyRegistry;
import com.medx.framework.util.DictUtil;
import com.medx.framework.util.TextUtil;

public class MethodHandlerUtil {
	public static AttrKey<?> getAttrKey(Method method, AttrKeyRegistry attrKeyRegistry) {
		String attrName = TextUtil.getCamelPostfix(method.getName());
		
		return attrKeyRegistry.getAttrKey(DictUtil.getAttrName(method.getDeclaringClass(), attrName));
	}
}
