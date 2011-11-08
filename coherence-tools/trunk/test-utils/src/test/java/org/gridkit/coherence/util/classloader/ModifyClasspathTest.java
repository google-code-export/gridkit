package org.gridkit.coherence.util.classloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

public class ModifyClasspathTest {

	@Test
	public void test_override() throws MalformedURLException {
		
		Isolate node = new Isolate("test-node", "org.gridkit");
		URL jar = getClass().getResource("/marker-override.jar");
		URL path = new URL("jar:" + jar.toString() + "!/");
		node.addToClasspath(path);
		
		node.start();
		
		node.exec(new CheckMarker("Marker from jar"));
		
		new CheckMarker("Default marker").run();
		
		node.stop();		
	}

//	@Test
//	public void test_hide() throws MalformedURLException {
//		
//		Isolate node = new Isolate("test-node", "org.gridkit");
//		
//		node.start();
//		
//		node.exec(new CheckMarker("Marker from jar"));
//		
//		new CheckMarker("Default marker").run();
//		
//		node.stop();		
//	}
	
	@SuppressWarnings("serial")
	public static class CheckMarker implements Runnable, Serializable {

		private String expected;
		
		public CheckMarker(String expected) {
			this.expected = expected;
		}

		@Override
		public void run() {
			try {
				URL url = getClass().getResource("/marker.txt");
				BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));
				Assert.assertEquals(expected, r.readLine());
				r.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}			
		}
	}

	@SuppressWarnings("serial")
	public static class CheckNoMarker implements Runnable, Serializable {
		
		@Override
		public void run() {
			URL url = getClass().getResource("/marker.txt");
			Assert.assertNull(url);
		}
	}
}
