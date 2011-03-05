package com.medx.framework.proxy.handler;

import java.util.HashMap;
import java.util.Map;

import com.medx.framework.annotation.handler.NounForm;
import com.medx.framework.attribute.AttrKey;

public final class ClassNounInfo {
	private final Map<String, AttrKey<?>> attrKeyByPlural = new HashMap<String, AttrKey<?>>();
	private final Map<String, AttrKey<?>> attrKeyBySingular = new HashMap<String, AttrKey<?>>();
	
	public NounForm getNounForm(String attrName) {
		//if (attrKeyByPlural.containsKey(attrName) == attrKeyBySingular.containsKey(attrName))
		//	throw new IllegalStateException("attrKeyByPlural | attrKeyBySingular");
		
		if (attrKeyByPlural.containsKey(attrName))
			return NounForm.PLURAL;
		else
			return NounForm.SINGULAR;
	}
	
	public AttrKey<?> getAttrKey(String attrName) {
		//if (attrKeyByPlural.containsKey(attrName) == attrKeyBySingular.containsKey(attrName))
		//	throw new IllegalStateException("attrKeyByPlural | attrKeyBySingular");
		
		if (attrKeyByPlural.containsKey(attrName))
			return attrKeyByPlural.get(attrName);
		else
			return attrKeyBySingular.get(attrName);
	}

	public Map<String, AttrKey<?>> getAttrKeyByPlural() {
		return attrKeyByPlural;
	}

	public Map<String, AttrKey<?>> getAttrKeyBySingular() {
		return attrKeyBySingular;
	}
}
