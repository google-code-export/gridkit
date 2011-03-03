package com.medx.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;

import com.medx.framework.attribute.AttrKeyRegistry;
import com.medx.framework.attribute.AttrKeyRegistryImpl;
import com.medx.framework.dictionary.DictionaryReader;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.proxy.MapProxyFactory;
import com.medx.framework.proxy.MapProxyFactoryImpl;
import com.medx.framework.proxy.handler.CachingMethodHandlerFactory;
import com.medx.framework.proxy.handler.MethodHandlerFactory;
import com.medx.framework.type.TypeRegistry;
import com.medx.framework.type.TypeRegistryImpl;
import com.medx.test.model.BlanksFactory;
import com.medx.test.model.customer.Customer;
import com.medx.test.model.customer.Sex;
import com.medx.test.model.order.Order;
import com.medx.test.model.order.OrderItem;
import com.medx.test.model.product.Product;

@Ignore
public class TestData {
	protected static Dictionary dictionary;
	protected static TypeRegistry typeRegistry;
	protected static AttrKeyRegistry attrKeyRegistry;
	protected static MethodHandlerFactory methodHandlerFactory;
	protected static MapProxyFactory proxyFactory;
	
	@BeforeClass
	protected static void beforeClass() throws Exception {
		dictionary = (new DictionaryReader()).readDictionary("src/main/java/xml/medx-test-dictionary.xml");
		typeRegistry = new TypeRegistryImpl(dictionary);
		attrKeyRegistry = new AttrKeyRegistryImpl(dictionary);
		methodHandlerFactory = new CachingMethodHandlerFactory(attrKeyRegistry);
		proxyFactory = new MapProxyFactoryImpl(typeRegistry, methodHandlerFactory);
	}
	
	protected Map<Integer, Object> tomCustomerMap;
	protected Map<Integer, Object> polyCustomerMap;
	
	protected Map<Integer, Object> tvProductMap;
	protected Map<Integer, Object> laptopProductMap;
	protected Map<Integer, Object> phoneProductMap;
	
	protected Map<Integer, Object> tvOrderItemMap;
	protected Map<Integer, Object> laptopOrderItemMap;
	protected Map<Integer, Object> phoneOrderItemMap;
	
	protected Map<Integer, Object> tomOrderMap;
	protected Map<Integer, Object> polyOrderMap;
	
	protected Customer tomCustomer;
	protected Customer polyCustomer;
	
	protected Product tvProduct;
	protected Product laptopProduct;
	protected Product phoneProduct;
	
	protected OrderItem tvOrderItem;
	protected OrderItem laptopOrderItem;
	protected OrderItem phoneOrderItem;
	
	protected Order tomOrder;
	protected Order polyOrder;
	
	protected void before() {
		tomCustomerMap = BlanksFactory.createCustomer("Tom", Sex.MALE);
		polyCustomerMap = BlanksFactory.createCustomer("Poly", Sex.FEMALE);
		
		tvProductMap = BlanksFactory.createProduct("tv", 3.0, Arrays.asList("samsung", "led"));
		laptopProductMap = BlanksFactory.createProduct("laptop",2.0, Arrays.asList("apple", "air"));
		phoneProductMap = BlanksFactory.createProduct("phone",1.0, Arrays.asList("google", "nexus"));
		
		tvOrderItemMap = BlanksFactory.createOrderItem(tvProductMap, 1);
		laptopOrderItemMap = BlanksFactory.createOrderItem(laptopProductMap, 1);
		phoneOrderItemMap = BlanksFactory.createOrderItem(phoneProductMap, 2);
		
		@SuppressWarnings("unchecked")
		List<Map<Integer, Object>> tomOrderItems = Arrays.asList(tvOrderItemMap);
		tomOrderMap = BlanksFactory.createOrder(1, tomCustomerMap, tomOrderItems);
		
		@SuppressWarnings("unchecked")
		List<Map<Integer, Object>> polyOrderItems = Arrays.asList(laptopOrderItemMap, phoneOrderItemMap);
		polyOrderMap = BlanksFactory.createOrder(1, tomCustomerMap, polyOrderItems);
		
		tomCustomer = proxyFactory.createMapProxy(tomCustomerMap);
		polyCustomer = proxyFactory.createMapProxy(polyCustomerMap);
		
		tvProduct = proxyFactory.createMapProxy(tvProductMap);
		laptopProduct = proxyFactory.createMapProxy(laptopProductMap);
		phoneProduct = proxyFactory.createMapProxy(phoneProductMap);
		
		tvOrderItem = proxyFactory.createMapProxy(tvOrderItemMap);
		laptopOrderItem = proxyFactory.createMapProxy(laptopOrderItemMap);
		phoneOrderItem = proxyFactory.createMapProxy(phoneOrderItemMap);
		
		tomOrder = proxyFactory.createMapProxy(tomOrderMap);
		polyOrder = proxyFactory.createMapProxy(polyOrderMap);
	}
}
