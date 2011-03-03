package com.medx.framework.test.model;

import java.util.List;

import com.medx.framework.annotation.ModelClass;

@ModelClass
public interface Order {
	int getId();
	
	void setId(int id);
	
	Customer getCustomer();
	
	void setCustomer(Customer customer);
	
	List<OrderItem> getItems(); 
	
	void setItems(List<OrderItem> items);
}
