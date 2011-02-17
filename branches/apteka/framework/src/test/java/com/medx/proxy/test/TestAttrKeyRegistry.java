package com.medx.proxy.test;

import org.junit.Ignore;

import com.medx.attribute.AttrKey;
import com.medx.attribute.AttrKeyRegistry;

@Ignore
public class TestAttrKeyRegistry implements AttrKeyRegistry {
	@Override
	@SuppressWarnings("unchecked")
	public <T> AttrKey<T> getAttrKey(int id_) {
		Integer id = id_;
		
		if (id.equals(TestDictionary.Id.customerName))
			return (AttrKey<T>)TestDictionary.customerName;
		else if (id.equals(TestDictionary.Id.orderCustomer))
			return (AttrKey<T>)TestDictionary.orderCustomer;
		if (id.equals(TestDictionary.Id.orderId))
			return (AttrKey<T>)TestDictionary.orderId;
		if (id.equals(TestDictionary.Id.orderItemPrice))
			return (AttrKey<T>)TestDictionary.orderItemPrice;
		if (id.equals(TestDictionary.Id.orderItems))
			return (AttrKey<T>)TestDictionary.orderItems;
		if (id.equals(TestDictionary.Id.orderItemTitle))
			return (AttrKey<T>)TestDictionary.orderItemTitle;
		else
			throw new IllegalArgumentException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> AttrKey<T> getAttrKey(String name) {
		if (name.equals(TestDictionary.Text.customerName))
			return (AttrKey<T>)TestDictionary.customerName;
		else if (name.equals(TestDictionary.Text.orderCustomer))
			return (AttrKey<T>)TestDictionary.orderCustomer;
		if (name.equals(TestDictionary.Text.orderId))
			return (AttrKey<T>)TestDictionary.orderId;
		if (name.equals(TestDictionary.Text.orderItemPrice))
			return (AttrKey<T>)TestDictionary.orderItemPrice;
		if (name.equals(TestDictionary.Text.orderItems))
			return (AttrKey<T>)TestDictionary.orderItems;
		if (name.equals(TestDictionary.Text.orderItemTitle))
			return (AttrKey<T>)TestDictionary.orderItemTitle;
		else
			throw new IllegalArgumentException();
	}
}
