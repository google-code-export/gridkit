package com.medx.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.medx.framework.bean.Bean;
import com.medx.framework.generation.BeanGenerator;
import com.medx.framework.serialization.BeanSerializer;
import com.medx.framework.serialization.kryo.KryoSerializer;
import com.medx.framework.serialization.xom.XomSerializer;
import com.medx.test.model.order.Order;

public class SerializationTest extends TestData {
	protected static BeanSerializer<byte[]> kryoSerializer;
	private static BeanSerializer<String> xomSerializer;
	
	@Test
	public void cyclicKryoSerializationTest() {
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
	public void cyclicXomSerializationTest() {
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
	
	@Test
	public void generatedKryo() {
		BeanGenerator beanGenerator = new BeanGenerator(modelMetadata, proxyFactory);

		List<Bean> beans = beanGenerator.generate(Order.class);
		
		for (Bean bean : beans) {
			byte[] beanData = kryoSerializer.serialize(bean);
			
			Bean newBean = (Bean)kryoSerializer.deserialize(beanData);
			
			assertEquals(bean, newBean);
			
			byte[] newbeanData = kryoSerializer.serialize(bean);
			
			assertArrayEquals(beanData, newbeanData);
		}
		
		System.out.println("Generated Kryo tested on size = " + beans.size());
	}
	
	@Test
	public void generatedXom() {
		BeanGenerator beanGenerator = new BeanGenerator(modelMetadata, proxyFactory);

		List<Bean> beans = beanGenerator.generate(Order.class);
		
		for (Bean bean : beans) {
			String beanData = xomSerializer.serialize(bean);
			
			Bean newBean = (Bean)xomSerializer.deserialize(beanData);
			
			assertEquals(bean, newBean);
			
			String newbeanData = xomSerializer.serialize(bean);
			
			assertEquals(beanData, newbeanData);
		}
		
		System.out.println("Generated Xom tested on size = " + beans.size());
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
