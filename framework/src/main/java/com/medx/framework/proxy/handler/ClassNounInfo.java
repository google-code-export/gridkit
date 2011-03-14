package com.medx.framework.proxy.handler;

import java.util.HashMap;
import java.util.Map;

import com.medx.framework.annotation.handler.NounForm;
import com.medx.framework.metadata.TypedAttrKey;

public final class ClassNounInfo {
	private final Map<String, TypedAttrKey> attrKeyByUnknown = new HashMap<String, TypedAttrKey>();
	
	private final Map<String, TypedAttrKey> attrKeyByPlural = new HashMap<String, TypedAttrKey>();
	private final Map<String, TypedAttrKey> attrKeyBySingular = new HashMap<String, TypedAttrKey>();
	
	public NounForm getNounForm(String attrName) {
		if (attrKeyByPlural.containsKey(attrName) && attrKeyBySingular.containsKey(attrName))
			throw new IllegalStateException("attrKeyByPlural | attrKeyBySingular");
		
		if (attrKeyByPlural.containsKey(attrName))
			return NounForm.PLURAL;
		else if (attrKeyBySingular.containsKey(attrName))
			return NounForm.SINGULAR;
		else
			return NounForm.UNKNOWN;
	}
	
	public TypedAttrKey getAttrKey(String attrName) {
		if (attrKeyByPlural.containsKey(attrName) && attrKeyBySingular.containsKey(attrName))
			throw new IllegalStateException("attrKeyByPlural | attrKeyBySingular");
		
		if (attrKeyByPlural.containsKey(attrName))
			return attrKeyByPlural.get(attrName);
		else if (attrKeyBySingular.containsKey(attrName))
			return attrKeyBySingular.get(attrName);
		else
			return attrKeyByUnknown.get(attrName);
	}

	public Map<String, TypedAttrKey> getAttrKeyByPlural() {
		return attrKeyByPlural;
	}

	public Map<String, TypedAttrKey> getAttrKeyBySingular() {
		return attrKeyBySingular;
	}

	public Map<String, TypedAttrKey> getAttrKeyByUnknown() {
		return attrKeyByUnknown;
	}
}
