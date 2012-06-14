package org.gridkit.util.vicontrol.marshal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Assert;

import org.junit.Test;

public class SmartViMarshalerTest {

	@Test
	public void test_serialization() throws IOException, ClassNotFoundException {				
		
		final Object taskId = "task";
		final int subId = 10;
		Runnable task = new Runnable() {
			int x = 100;
			
			@Override
			public void run() {
			}
			
			public String toString() {
				return taskId + "." + subId + "." + x;
			}
		};
		
		Marshaled obj = new SmartViMarshaler().marshal(task);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.close();
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);
		Marshaled env = (Marshaled) ois.readObject();
		
		Assert.assertEquals("task.10.100", env.unmarshal().toString());		
	}		
}
