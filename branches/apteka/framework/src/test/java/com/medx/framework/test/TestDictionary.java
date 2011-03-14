package com.medx.framework.test;

import java.util.List;

import org.junit.Ignore;

import com.medx.framework.metadata.ClassKey;
import com.medx.framework.metadata.ClassKeyFactory;
import com.medx.framework.metadata.UserAttrKey;
import com.medx.framework.test.model.Customer;
import com.medx.framework.test.model.Order;
import com.medx.framework.test.model.OrderItem;

@Ignore
public class TestDictionary {
	public static final class Type {
		public static final ClassKey customerType = ClassKeyFactory.createUserClassKey(6, 1, Customer.class);
		public static final ClassKey orderType = ClassKeyFactory.createUserClassKey(7, 1, Order.class);
		public static final ClassKey orderItem = ClassKeyFactory.createUserClassKey(8, 1, OrderItem.class);
	}
	
	public static final UserAttrKey<String> customerName = new UserAttrKey<String>(0, "testmodel.Customer.name", 1);
	public static final UserAttrKey<Integer> orderId = new UserAttrKey<Integer>(1, "testmodel.Order.id", 1);
	public static final UserAttrKey<String> orderItemTitle = new UserAttrKey<String>(2, "testmodel.OrderItem.title", 1);
	public static final UserAttrKey<Double> orderItemPrice = new UserAttrKey<Double>(3, "testmodel.OrderItem.price", 1);
	public static final UserAttrKey<Customer> orderCustomer = new UserAttrKey<Customer>(4, "testmodel.Order.customer", 1);
	public static final UserAttrKey<List<OrderItem>> orderItems = new UserAttrKey<List<OrderItem>>(5, "testmodel.Order.items", 1);
	public static final UserAttrKey<int[]> customerTags = new UserAttrKey<int[]>(9, "testmodel.Customer.tags", 1);
	public static final UserAttrKey<String[]> customerTitles = new UserAttrKey<String[]>(10, "testmodel.Customer.titles", 1);
	
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
