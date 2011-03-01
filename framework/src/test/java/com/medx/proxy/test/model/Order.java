package com.medx.proxy.test.model;

import java.util.List;

import com.medx.framework.type.annotation.DictType;

@DictType(packageCutPrefix = "com.medx.proxy.test")
public interface Order {
	int getId();
	
	void setId(int id);
	
	Customer getCustomer();
	
	void setCustomer(Customer customer);
	
	List<OrderItem> getItems(); 
	
	void setItems(List<OrderItem> items);
}
