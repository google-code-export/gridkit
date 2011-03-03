package com.medx.test.model.order;

import java.util.List;

import com.medx.framework.annotation.ModelClass;
import com.medx.test.model.customer.Customer;

@ModelClass
public interface Order {
	int getId();
	void setId(int id);
	
	Customer getCustomer();
	void setCustomer(Customer customer);
	
	List<OrderItem> getItems();
	void setItems(List<OrderItem> items);
}
