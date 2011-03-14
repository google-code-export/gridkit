package com.medx.framework.proxy.handler;

import com.medx.framework.annotation.handler.NounForm;
import com.medx.framework.metadata.TypedAttrKey;

public class MethodInfo {
	private final String verb;
	private final String attrName;
	private final NounForm nounForm;
	private final TypedAttrKey attrKey;
	private final Class<?>[] parameterTypes;
	
	MethodInfo(String verb, String attrName, NounForm nounForm, TypedAttrKey attrKey, Class<?>[] parameterTypes) {
		this.verb = verb;
		this.attrName = attrName;
		this.nounForm = nounForm;
		this.attrKey = attrKey;
		this.parameterTypes = parameterTypes;
	}

	public String getVerb() {
		return verb;
	}

	public String getAttrName() {
		return attrName;
	}

	public NounForm getNounForm() {
		return nounForm;
	}

	public TypedAttrKey getAttrKey() {
		return attrKey;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}
}
