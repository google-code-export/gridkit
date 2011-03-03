package com.medx.test.model;

import java.util.List;

import com.medx.framework.annotation.DictType;

@DictType
public interface Taggable {
	List<String> getTags();
	
	void setTags(List<String> tags);
}
