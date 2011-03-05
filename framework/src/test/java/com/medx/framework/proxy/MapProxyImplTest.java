package com.medx.framework.proxy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.medx.framework.attribute.AttrMap;
import com.medx.framework.dictionary.DictionaryReader;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.metadata.ModelMetadata;
import com.medx.framework.metadata.ModelMetadataImpl;
import com.medx.framework.proxy.handler.CachingMethodHandlerFactory;
import com.medx.framework.proxy.handler.MethodHandlerFactory;
import com.medx.framework.proxy.serialization.MapProxyBinarySerializer;
import com.medx.framework.proxy.serialization.kryo.KryoSerializer;
import com.medx.framework.test.TestDictionary;
import com.medx.framework.test.model.Customer;
import com.medx.framework.test.model.Order;
import com.medx.framework.test.model.OrderItem;

public class MapProxyImplTest {
	private static final double DELTA = 0.0001;
	
	private static final Dictionary dictionary;
	
	static {
		try {
			dictionary = (new DictionaryReader()).readDictionary("src/test/resources/xml/test-dictionary.xml");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static ModelMetadata modelMetadata = new ModelMetadataImpl(dictionary);
	private static MethodHandlerFactory methodHandlerFactory = new CachingMethodHandlerFactory(modelMetadata);
	private static MapProxyFactory proxyFactory = new MapProxyFactoryImpl(modelMetadata, methodHandlerFactory);
	private static MapProxyBinarySerializer kryoSerializer = new KryoSerializer(proxyFactory, modelMetadata);
	
	public static Map<Integer, Object> customerMap = null;
	public static Map<Integer, Object> orderMap = null;
	
	public static Map<Integer, Object> orderItem1Map = null;
	public static Map<Integer, Object> orderItem2Map = null;
	public static Map<Integer, Object> orderItem3Map = null;
	
	@Before
	public void before() {
		customerMap = new HashMap<Integer, Object>();
		
		int[] customerTags = {1,2,3};
		String[] customerTitles = {"a", "b", "c"}; 
		
		customerMap.put(6, true);
		customerMap.put(MapProxyFactory.PROXIABLE_KEY, true);
		
		customerMap.put(TestDictionary.Id.customerName, "Ted");
		customerMap.put(TestDictionary.Id.customerTags, customerTags);
		customerMap.put(TestDictionary.Id.customerTitles, customerTitles);
		
		orderMap = new HashMap<Integer, Object>();
		
		orderMap.put(7, true);
		orderMap.put(MapProxyFactory.PROXIABLE_KEY, true);
		
		orderMap.put(TestDictionary.Id.orderId, 0);
		orderMap.put(TestDictionary.Id.orderCustomer, customerMap);
		
		orderItem1Map = new HashMap<Integer, Object>(); 
		
		orderItem1Map.put(8, true);
		orderItem1Map.put(MapProxyFactory.PROXIABLE_KEY, true);
		
		orderItem1Map.put(TestDictionary.Id.orderItemTitle, "clock");
		orderItem1Map.put(TestDictionary.Id.orderItemPrice, 1.0);
		
		orderItem2Map = new HashMap<Integer, Object>(); 
		
		orderItem2Map.put(8, true);
		orderItem2Map.put(MapProxyFactory.PROXIABLE_KEY, true);
		
		orderItem2Map.put(TestDictionary.Id.orderItemTitle, "mouse");
		orderItem2Map.put(TestDictionary.Id.orderItemPrice, 2.0);
		
		orderItem3Map = new HashMap<Integer, Object>(); 
		
		orderItem3Map.put(8, true);
		orderItem3Map.put(MapProxyFactory.PROXIABLE_KEY, true);
		
		orderItem3Map.put(TestDictionary.Id.orderItemTitle, "keyboard");
		orderItem3Map.put(TestDictionary.Id.orderItemPrice, 3.0);
		
		List<Map<Integer, Object>> orderItems = new ArrayList<Map<Integer,Object>>();
		
		orderItems.add(orderItem1Map);
		orderItems.add(orderItem2Map);
		
		orderMap.put(TestDictionary.Id.orderItems, orderItems);
	}
	
	@Test
	public void test1() {
		Customer customer = proxyFactory.createMapProxy(customerMap);
		assertEquals("Ted", customer.getName());
		customer.setName("Ralph");
		assertEquals("Ralph", customer.getName());
	}
	
	@Test
	public void test2() {
		Order order = proxyFactory.createMapProxy(orderMap);
		assertEquals("Ted", order.getCustomer().getName());
		order.getCustomer().setName("Ralph");
		assertEquals("Ralph", order.getCustomer().getName());
	}
	
	@Test
	public void test3() {
		Order order = proxyFactory.createMapProxy(orderMap);
		assertEquals(0, order.getId());
		order.setId(1);
		assertEquals(1, order.getId());
	}
	
	@Test
	public void test4() {
		Order order = proxyFactory.createMapProxy(orderMap);
		
		assertEquals("clock", order.getItems().get(0).getTitle());
		assertEquals(1.0, order.getItems().get(0).getPrice(), DELTA);
		
		assertEquals("mouse", order.getItems().get(1).getTitle());
		assertEquals(2.0, order.getItems().get(1).getPrice(), DELTA);
		
		order.getItems().get(0).setTitle("clock!");
		order.getItems().get(0).setPrice(-1.0);
		
		order.getItems().get(1).setTitle("mouse!");
		order.getItems().get(1).setPrice(-2.0);
		
		assertEquals("clock!", order.getItems().get(0).getTitle());
		assertEquals(-1.0, order.getItems().get(0).getPrice(), DELTA);
		
		assertEquals("mouse!", order.getItems().get(1).getTitle());
		assertEquals(-2.0, order.getItems().get(1).getPrice(), DELTA);
	}
	
	@Test
	public void test5() {
		Order order = proxyFactory.createMapProxy(orderMap);
		OrderItem orderItem = proxyFactory.createMapProxy(orderItem3Map);
		
		order.setItems(Collections.singletonList(orderItem));

		assertEquals(1, order.getItems().size());
		assertEquals("keyboard", order.getItems().get(0).getTitle());
		assertEquals(3.0, order.getItems().get(0).getPrice(), DELTA);
	}
	
	@Test
	public void test6() {
		System.out.println(proxyFactory.createMapProxy(orderMap).toString());
	}
	
	@Test
	public void test7(){
		AttrMap order = (AttrMap)proxyFactory.createMapProxy(orderMap);
		
		assertEquals(Integer.valueOf(0), order.getAttribute(TestDictionary.orderId));
		
		order.setAttribute(TestDictionary.orderId, 1);
		
		assertEquals(Integer.valueOf(1), order.getAttribute(TestDictionary.orderId));
	}
	
	@Test
	public void test8() {
		Customer customer = proxyFactory.createMapProxy(customerMap);
		
		int[] customerTags = {1,2,3};
		String[] customerTitles = {"a", "b", "c"}; 
		
		assertArrayEquals(customerTags, customer.getTags());
		assertArrayEquals(customerTitles, customer.getTitles());
	}
	
	@Test
	public void test9() {
		MapProxy orderItem1 = proxyFactory.createMapProxy(orderItem1Map);
		
		byte[] data = kryoSerializer.serialize(orderItem1);
		
		System.out.println("Serialized array size = " + data.length);
		
		MapProxy orderItem2 = kryoSerializer.deserialize(data);
		
		assertEquals(orderItem1, orderItem2);
	}
}
