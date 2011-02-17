package com.medx.proxy.test.model;

import java.util.List;

import com.medx.attribute.annotation.AttrKey;
import com.medx.proxy.test.TestDictionary;

public interface Order {
	@AttrKey(TestDictionary.Text.orderId)
	int getId();
	
	@AttrKey(TestDictionary.Text.orderId)
	void setId(int id);
	
	@AttrKey(TestDictionary.Text.orderCustomer)
	Customer getCustomer();
	
	@AttrKey(TestDictionary.Text.orderCustomer)
	void setCustomer(Customer customer);
	
	@AttrKey(TestDictionary.Text.orderItems)
	List<OrderItem> getItems(); 
	
	@AttrKey(TestDictionary.Text.orderItems)
	void setItems(List<OrderItem> items);
}
