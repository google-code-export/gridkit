package com.medx.proxy.test;

import org.junit.Ignore;

import com.medx.proxy.test.model.Customer;
import com.medx.proxy.test.model.Order;
import com.medx.proxy.test.model.OrderItem;
import com.medx.type.TypeRegistry;

@Ignore
public class TestTypeRegistry implements TypeRegistry {
	@Override
	public Class<?> getType(int id) {
		if (id == 0)
			return Customer.class;
		else if (id == 1)
			return Order.class;
		else if (id == 2)
			return OrderItem.class;
		
		throw new IllegalArgumentException();
	}

	@Override
	public Class<?> getType(String name) {
		return null;
	}

	@Override
	public int getTypeId(Class<?> clazz) {
		return 0;
	}

	@Override
	public String getTypeName(Class<?> clazz) {
		return null;
	}
}
