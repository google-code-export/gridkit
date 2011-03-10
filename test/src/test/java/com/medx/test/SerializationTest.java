package com.medx.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.medx.framework.proxy.MapProxy;
import com.medx.framework.proxy.serialization.MapProxySerializer;
import com.medx.framework.proxy.serialization.kryo.KryoSerializer;
import com.medx.framework.proxy.serialization.xom.XomSerializer;
import com.medx.test.model.order.Order;

public class SerializationTest extends TestData {
	protected static MapProxySerializer<byte[]> kryoSerializer;
	private static MapProxySerializer<String> xomSerializer;
	
	@Test
	public void kryoSerializationTest() {
		byte[] tomData = kryoSerializer.serialize((MapProxy)tomOrder);
		byte[] polyData = kryoSerializer.serialize((MapProxy)polyOrder);
		
		System.out.println("Serialized tomOrder size = " + tomData.length);
		System.out.println("Serialized polyOrder size = " + polyData.length);
		
		Order newTomOrder = (Order) kryoSerializer.deserialize(tomData);
		Order newPolyOrder = (Order) kryoSerializer.deserialize(polyData);
		
		assertEquals(tomOrder.getId(), newTomOrder.getId());
		assertEquals(tomOrder.getCustomer(), newTomOrder.getCustomer());
		assertEquals(tomOrder.getItems().size(), newTomOrder.getItems().size());
		
		assertEquals(polyOrder.getId(), newPolyOrder.getId());
		assertEquals(polyOrder.getCustomer(), newPolyOrder.getCustomer());
		assertEquals(polyOrder.getItems().size(), newPolyOrder.getItems().size());
	}
	
	@Test
	public void xomSerializationTest() {
		String tomData = xomSerializer.serialize((MapProxy)tomOrder);
		String polyData = xomSerializer.serialize((MapProxy)polyOrder);
		
		System.out.println(tomData);
		System.out.println(polyData);
		
		/*Order newTomOrder = (Order) kryoSerializer.deserialize(tomData);
		Order newPolyOrder = (Order) kryoSerializer.deserialize(polyData);
		
		assertEquals(tomOrder.getId(), newTomOrder.getId());
		assertEquals(tomOrder.getCustomer(), newTomOrder.getCustomer());
		assertEquals(tomOrder.getItems().size(), newTomOrder.getItems().size());
		
		assertEquals(polyOrder.getId(), newPolyOrder.getId());
		assertEquals(polyOrder.getCustomer(), newPolyOrder.getCustomer());
		assertEquals(polyOrder.getItems().size(), newPolyOrder.getItems().size());*/
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
