package com.medx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.medx.framework.bean.Bean;
import com.medx.framework.proxy.serialization.MapProxySerializer;
import com.medx.framework.proxy.serialization.kryo.KryoSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializer;
import com.medx.test.model.order.Order;

public class SerializationTest extends TestData {
	protected static MapProxySerializer<byte[]> kryoSerializer;
	private static MapProxySerializer<String> xomSerializer;
	
	@Test
	public void kryoSerializationTest() {
		byte[] tomData = kryoSerializer.serialize((Bean)tomOrder);
		byte[] polyData = kryoSerializer.serialize((Bean)polyOrder);
		
		System.out.println("Serialized tomOrder size = " + tomData.length);
		System.out.println("Serialized polyOrder size = " + polyData.length);
		
		Order newTomOrder = (Order) kryoSerializer.deserialize(tomData);
		Order newPolyOrder = (Order) kryoSerializer.deserialize(polyData);
		
		byte[] newTomData = kryoSerializer.serialize((Bean)newTomOrder);
		byte[] newPolyData = kryoSerializer.serialize((Bean)newPolyOrder);
		
		assertEquals(tomOrder.getId(), newTomOrder.getId());
		assertEquals(tomOrder.getCustomer(), newTomOrder.getCustomer());
		assertEquals(tomOrder.getItems().size(), newTomOrder.getItems().size());
		
		assertEquals(polyOrder.getId(), newPolyOrder.getId());
		assertEquals(polyOrder.getCustomer(), newPolyOrder.getCustomer());
		assertEquals(polyOrder.getItems().size(), newPolyOrder.getItems().size());
		
		assertArrayEquals(tomData, newTomData);
		assertArrayEquals(polyData, newPolyData);
	}
	
	@Test
	public void xomSerializationTest() {
		String tomData = xomSerializer.serialize((Bean)tomOrder);
		String polyData = xomSerializer.serialize((Bean)polyOrder);
		
		Order newTomOrder = (Order) xomSerializer.deserialize(tomData);
		Order newPolyOrder = (Order) xomSerializer.deserialize(polyData);
		
		String newTomData = xomSerializer.serialize((Bean)newTomOrder);
		String newPolyData = xomSerializer.serialize((Bean)newPolyOrder);
		
		assertEquals(tomOrder.getId(), newTomOrder.getId());
		assertEquals(tomOrder.getCustomer(), newTomOrder.getCustomer());
		assertEquals(tomOrder.getItems().size(), newTomOrder.getItems().size());
		
		assertEquals(polyOrder.getId(), newPolyOrder.getId());
		assertEquals(polyOrder.getCustomer(), newPolyOrder.getCustomer());
		assertEquals(polyOrder.getItems().size(), newPolyOrder.getItems().size());
		
		assertEquals(tomData, newTomData);
		assertEquals(polyData, newPolyData);
	}
	
	@Before
	public void before() {
		super.before();
	}
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		TestData.beforeClass();
		
		kryoSerializer = new KryoSerializer(proxyFactory, modelMetadata);
		xomSerializer = new XomSerializer(proxyFactory, modelMetadata);
	}
}
