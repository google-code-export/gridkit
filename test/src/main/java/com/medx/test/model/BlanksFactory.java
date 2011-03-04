package com.medx.test.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.medx.framework.proxy.MapProxyFactory;
import com.medx.test.dictionary.Taggable;
import com.medx.test.dictionary.customer.Customer;
import com.medx.test.dictionary.order.Order;
import com.medx.test.dictionary.order.OrderItem;
import com.medx.test.dictionary.product.Product;
import com.medx.test.model.customer.Sex;


public class BlanksFactory {
	public static Map<Integer, Object> createCustomer(String name, Sex sex) {
		Map<Integer, Object> result = new HashMap<Integer, Object>();
		
		result.put(MapProxyFactory.PROXIABLE_KEY, true);
		result.put(Customer.Type.descriptor.getId(), true);
		
		result.put(Customer.name.getId(), name);
		result.put(Customer.sex.getId(), sex);
		
		return result;
	}
	
	public static Map<Integer, Object> createProduct(String title, double price, List<String> tags) {
		Map<Integer, Object> result = new HashMap<Integer, Object>();
		
		result.put(MapProxyFactory.PROXIABLE_KEY, true);
		result.put(Product.Type.descriptor.getId(), true);
		result.put(Taggable.Type.descriptor.getId(), true);
		
		result.put(Product.title.getId(), title);
		result.put(Product.price.getId(), price);
		result.put(Taggable.tags.getId(), tags);
		
		return result;
	}
	
	public static Map<Integer, Object> createOrderItem(Map<Integer, Object> product, int quantity) {
		Map<Integer, Object> result = new HashMap<Integer, Object>();
		
		result.put(MapProxyFactory.PROXIABLE_KEY, true);
		result.put(OrderItem.Type.descriptor.getId(), true);
		
		result.put(OrderItem.product.getId(), product);
		result.put(OrderItem.quantity.getId(), quantity);
		
		return result;
	}
	
	public static Map<Integer, Object> createOrder(int id, Map<Integer, Object> customer, List<Map<Integer, Object>> items) {
		Map<Integer, Object> result = new HashMap<Integer, Object>();

		result.put(MapProxyFactory.PROXIABLE_KEY, true);
		result.put(Order.Type.descriptor.getId(), true);
		
		result.put(Order.id.getId(), id);
		result.put(Order.customer.getId(), customer);
		result.put(Order.items.getId(), items);
		
		return result;
	}
}
