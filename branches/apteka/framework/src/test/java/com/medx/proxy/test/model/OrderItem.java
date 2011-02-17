package com.medx.proxy.test.model;

import com.medx.attribute.annotation.AttrKey;
import com.medx.proxy.test.TestDictionary;

public interface OrderItem {
	@AttrKey(TestDictionary.Text.orderItemTitle)
	String getTitle();
	
	@AttrKey(TestDictionary.Text.orderItemTitle)
	void setTitle(String title);
	
	@AttrKey(TestDictionary.Text.orderItemPrice)
	double getPrice();
	
	@AttrKey(TestDictionary.Text.orderItemPrice)
	void setPrice(double price);
}
