package com.medx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.medx.framework.attribute.AttrMap;
import com.medx.framework.proxy.MapProxy;
import com.medx.test.dictionary.Taggable;
import com.medx.test.dictionary.customer.Customer;
import com.medx.test.dictionary.order.OrderItem;
import com.medx.test.model.customer.Sex;

public class MapProxyTest extends TestData {
	private static final double DELTA = 0.0001;
	
	@Test
	public void equalityTest() {
		assertTrue(tomOrder.equals(proxyFactory.createMapProxy(new HashMap<Integer, Object>(tomOrderMap))));
		
		assertFalse(tomOrder.equals(polyOrder));
		assertFalse(tvProduct.equals(laptopProduct));
		assertFalse(phoneProduct.equals(phoneOrderItem));
	}
	
	@Test
	public void simpleAttributeGetTest() {
		assertEquals("Tom", tomCustomer.getName());
		assertEquals(Sex.FEMALE, ((AttrMap)polyCustomer).getAttribute(Customer.sex));
		assertEquals(new Integer(2), ((Map)phoneOrderItem).get(OrderItem.quantity.getId()));
		
		assertEquals(Arrays.asList("apple", "air"), laptopProduct.getTags());
		assertEquals(Arrays.asList("samsung", "led"), ((AttrMap)tvProduct).getAttribute(Taggable.tags));
		assertEquals(Arrays.asList("google", "nexus"), ((Map)phoneProduct).get(Taggable.tags.getId()));
	}
	
	@Test
	public void indirectAttributeGetTest() {
		assertEquals("tv", tvOrderItem.getProduct().getTitle());
		assertEquals(1.0, phoneOrderItem.getProduct().getPrice(), DELTA);
		assertEquals(Arrays.asList("apple", "air"), polyOrder.getItems().get(0).getProduct().getTags());
	}
	
	@Test
	public void simpleAttributeSetByIntTest() {
		tomCustomer.setName("TomTom");
		((AttrMap)polyCustomer).setAttribute(Customer.sex, Sex.MALE);
		((Map)phoneOrderItem).put(OrderItem.quantity.getId(), new Integer(3));
		
		assertEquals("TomTom", tomCustomer.getName());
		assertEquals(Sex.MALE, ((AttrMap)polyCustomer).getAttribute(Customer.sex));
		assertEquals(new Integer(3), ((Map)phoneOrderItem).get(OrderItem.quantity.getId()));
	}

	@Test
	public void simpleAttributeSetByNameTest() {
		tomCustomer.setName("TomTom");
		((AttrMap)polyCustomer).setAttribute(Customer.sex, Sex.MALE);
		((Map)phoneOrderItem).put(OrderItem.quantity.getName(), new Integer(3));
		
		assertEquals("TomTom", tomCustomer.getName());
		assertEquals(Sex.MALE, ((AttrMap)polyCustomer).getAttribute(Customer.sex));
		assertEquals(new Integer(3), ((Map)phoneOrderItem).get(OrderItem.quantity.getId()));
	}

	@Test
	public void simpleAttributeSetByKeyTest() {
		tomCustomer.setName("TomTom");
		((AttrMap)polyCustomer).setAttribute(Customer.sex, Sex.MALE);
		((Map)phoneOrderItem).put(OrderItem.quantity, new Integer(3));
		
		assertEquals("TomTom", tomCustomer.getName());
		assertEquals(Sex.MALE, ((AttrMap)polyCustomer).getAttribute(Customer.sex));
		assertEquals(new Integer(3), ((Map)phoneOrderItem).get(OrderItem.quantity.getId()));
	}

	@Test
	public void attributeSetMapSizeTest() {
		tomCustomer.setName("TomTom");
		((AttrMap)polyCustomer).setAttribute(Customer.sex, Sex.MALE);
		((Map)phoneOrderItem).put(OrderItem.quantity.getId(), new Integer(3));
		
		assertEquals(3, ((Map)phoneOrderItem).size());
	}
	
	@Test
	public void indirectAttributeSetTest() {
		tvOrderItem.getProduct().setTitle("tvtv");
		phoneOrderItem.getProduct().setPrice(2.0);
		polyOrder.getItems().get(0).getProduct().setTags(Arrays.asList("apple", "air", "trash"));
		
		assertEquals("tvtv", tvOrderItem.getProduct().getTitle());
		assertEquals(2.0, phoneOrderItem.getProduct().getPrice(), DELTA);
		assertEquals(Arrays.asList("apple", "air", "trash"), polyOrder.getItems().get(0).getProduct().getTags());
	}
	
	@Test
	public void compositeCollectionAccessTest() {
		assertEquals(2, polyOrder.getItems().size());
		
		assertTrue(polyOrder.getItems().contains(laptopOrderItem));
		assertFalse(polyOrder.getItems().contains(tvOrderItem));
	}
	
	@Test
	public void compositeCollectionManipulationTest() {
		com.medx.test.model.order.OrderItem tvOrderItemCopy = proxyFactory.createMapProxy(tvOrderItemMap);
		
		polyOrder.getItems().add(tvOrderItemCopy);
		assertTrue(polyOrder.getItems().contains(tvOrderItem));
		
		tomOrder.getItems().clear();
		assertEquals(0, tomOrder.getItems().size());
	}
	
	@Test
	public void dynamicCastTest() {
		com.medx.test.model.Taggable taggable = ((MapProxy)tomCustomer).cast(com.medx.test.model.Taggable.class);
		
		assertTrue(com.medx.test.model.customer.Customer.class.isInstance(taggable));
		
		taggable.setTags(Collections.singletonList("man"));
		
		assertEquals(Collections.singletonList("man"), taggable.getTags());
		assertEquals(Collections.singletonList("man"), ((AttrMap)taggable).getAttribute(Taggable.tags));
		assertEquals(Collections.singletonList("man"), ((Map)taggable).get(Taggable.tags.getId()));
	}
	
	@Before
	public void before() {
		super.before();
	}
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		TestData.beforeClass();
	}
}
