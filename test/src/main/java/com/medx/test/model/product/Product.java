package com.medx.test.model.product;

import com.medx.framework.annotation.DictType;
import com.medx.test.model.Taggable;

@DictType
public interface Product extends Taggable {
	String getTitle();
	void setTitle(String title);
	
	double getPrice();
	void setPrice(double price);
}
