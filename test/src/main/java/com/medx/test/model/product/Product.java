package com.medx.test.model.product;

import com.medx.framework.annotation.ModelClass;
import com.medx.test.model.Taggable;

@ModelClass
public interface Product extends Taggable {
	String getTitle();
	void setTitle(String title);
	
	double getPrice();
	void setPrice(double price);
}
