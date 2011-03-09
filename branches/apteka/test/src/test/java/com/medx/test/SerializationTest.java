package com.medx.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.medx.framework.proxy.MapProxy;
import com.medx.framework.proxy.serialization.MapProxyBinarySerializer;
import com.medx.framework.proxy.serialization.kryo.KryoSerializer;

public class SerializationTest extends TestData {
	protected static MapProxyBinarySerializer kryoSerializer;
	
	@Test
	public void kryoSerializationTest() {
		byte[] tomData = kryoSerializer.serialize((MapProxy)tomOrder);
		byte[] polyData = kryoSerializer.serialize((MapProxy)polyOrder);
		
		System.out.println("Serialized tomOrder size = " + tomData.length);
		System.out.println("Serialized polyOrder size = " + polyData.length);
		
		MapProxy newTomOrder = kryoSerializer.deserialize(tomData);
		MapProxy newPolyOrder = kryoSerializer.deserialize(polyData);
		
		assertEquals(tomOrder, newTomOrder);
		assertEquals(polyOrder, newPolyOrder);
		
		assertFalse(tomOrder.equals(newPolyOrder));
		assertFalse(polyOrder.equals(newTomOrder));
	}
	
	@Before
	public void before() {
		super.before();
	}
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		TestData.beforeClass();
		
		kryoSerializer = new KryoSerializer(proxyFactory, modelMetadata);
	}
}
