package com.medx.proxy.test;

import java.util.List;

import org.junit.Ignore;

import com.medx.attribute.AttrKey;
import com.medx.proxy.test.model.Customer;
import com.medx.proxy.test.model.OrderItem;

@Ignore
public class TestDictionary {
	public static final AttrKey<String> customerName = new AttrKey<String>(0, "customerName", 1, String.class);
	public static final AttrKey<Integer> orderId = new AttrKey<Integer>(1, "orderId", 1, Integer.class);
	public static final AttrKey<String> orderItemTitle = new AttrKey<String>(2, "orderItemTitle", 1, String.class);
	public static final AttrKey<Double> orderItemPrice = new AttrKey<Double>(3, "orderItemPrice", 1, Double.class);
	public static final AttrKey<Customer> orderCustomer = new AttrKey<Customer>(4, "orderCustomer", 1, Customer.class);
	public static final AttrKey<List<OrderItem>> orderItems = new AttrKey<List<OrderItem>>(5, "orderItems", 1, List.class);
	
	public static final class Id {
		public static final int customerName = 0;
		public static final int orderId = 1;
		public static final int orderItemTitle = 2;
		public static final int orderItemPrice = 3;
		public static final int orderCustomer = 4;
		public static final int orderItems = 5;
	}
	
	public static final class Text {
		public static final String customerName = "customerName";
		public static final String orderId = "orderId";
		public static final String orderItemTitle = "orderItemTitle";
		public static final String orderItemPrice = "orderItemPrice";
		public static final String orderCustomer = "orderCustomer";
		public static final String orderItems = "orderItems";
	}
}
