package com.medx.framework.test;

import java.util.List;

import org.junit.Ignore;

import com.medx.framework.attribute.AttrKey;
import com.medx.framework.metadata.TypeKey;
import com.medx.framework.test.model.Customer;
import com.medx.framework.test.model.Order;
import com.medx.framework.test.model.OrderItem;

@Ignore
public class TestDictionary {
	public static final class Type {
		public static final TypeKey<Customer> customerType = new TypeKey<Customer>(6, 1, Customer.class);
		public static final TypeKey<Customer> orderType = new TypeKey<Customer>(7, 1, Order.class);
		public static final TypeKey<Customer> orderItem = new TypeKey<Customer>(8, 1, OrderItem.class);
	}
	
	public static final AttrKey<String> customerName = new AttrKey<String>(0, "testmodel.Customer.name", 1, String.class);
	public static final AttrKey<Integer> orderId = new AttrKey<Integer>(1, "testmodel.Order.id", 1, Integer.class);
	public static final AttrKey<String> orderItemTitle = new AttrKey<String>(2, "testmodel.OrderItem.title", 1, String.class);
	public static final AttrKey<Double> orderItemPrice = new AttrKey<Double>(3, "testmodel.OrderItem.price", 1, Double.class);
	public static final AttrKey<Customer> orderCustomer = new AttrKey<Customer>(4, "testmodel.Order.customer", 1, Customer.class);
	public static final AttrKey<List<OrderItem>> orderItems = new AttrKey<List<OrderItem>>(5, "testmodel.Order.items", 1, List.class);
	public static final AttrKey<int[]> customerTags = new AttrKey<int[]>(9, "testmodel.Customer.tags", 1, int[].class);
	public static final AttrKey<String[]> customerTitles = new AttrKey<String[]>(10, "testmodel.Customer.titles", 1, String[].class);
	
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
		public static final String customerName = "testmodel.Customer.name";
		public static final String orderId = "testmodel.Order.id";
		public static final String orderItemTitle = "testmodel.OrderItem.title";
		public static final String orderItemPrice = "testmodel.OrderItem.price";
		public static final String orderCustomer = "testmodel.Order.customer";
		public static final String orderItems = "testmodel.Order.items";
		public static final String customerTags = "testmodel.Customer.tags";
		public static final String customerTitles = "testmodel.Customer.titles";
	}
}
