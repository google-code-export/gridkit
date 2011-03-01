package com.medx.proxy.test;

import java.util.List;

import org.junit.Ignore;

import com.medx.framework.attribute.AttrKey;
import com.medx.proxy.test.model.Customer;
import com.medx.proxy.test.model.OrderItem;

@Ignore
public class TestDictionary {
	public static final AttrKey<String> customerName = new AttrKey<String>(0, "model.Customer.name", 1, String.class);
	public static final AttrKey<Integer> orderId = new AttrKey<Integer>(1, "model.Order.id", 1, Integer.class);
	public static final AttrKey<String> orderItemTitle = new AttrKey<String>(2, "model.OrderItem.title", 1, String.class);
	public static final AttrKey<Double> orderItemPrice = new AttrKey<Double>(3, "model.OrderItem.price", 1, Double.class);
	public static final AttrKey<Customer> orderCustomer = new AttrKey<Customer>(4, "model.Order.customer", 1, Customer.class);
	public static final AttrKey<List<OrderItem>> orderItems = new AttrKey<List<OrderItem>>(5, "model.Order.items", 1, List.class);
	public static final AttrKey<int[]> customerTags = new AttrKey<int[]>(9, "model.Customer.tags", 1, int[].class);
	public static final AttrKey<String[]> customerTitles = new AttrKey<String[]>(10, "model.Customer.titles", 1, String[].class);
	
	public static final class Id {
		public static final int customerName = 0;
		public static final int orderId = 1;
		public static final int orderItemTitle = 2;
		public static final int orderItemPrice = 3;
		public static final int orderCustomer = 4;
		public static final int orderItems = 5;
		public static final int customerTags = 9;
		public static final int customerTitles = 10;
	}
	
	public static final class Text {
		public static final String customerName = "model.Customer.name";
		public static final String orderId = "model.Order.id";
		public static final String orderItemTitle = "model.OrderItem.title";
		public static final String orderItemPrice = "model.OrderItem.price";
		public static final String orderCustomer = "model.Order.customer";
		public static final String orderItems = "model.Order.items";
		public static final String customerTags = "model.Customer.tags";
		public static final String customerTitles = "model.Customer.titles";
	}
}
