package org.gridkit.gatling.remoting.bootstraper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class HalloWorld {
	public static void main(String[] args) throws IOException {
		System.out.println("Hallo world");
		
//		for(URL url: ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs()) {
//			System.out.println(url);
//		}
//		
//		Enumeration en = Thread.currentThread().getContextClassLoader().getResources("META-INF/MANIFEST.MF");
//		while(en.hasMoreElements()) {
//			URL x = (URL) en.nextElement();
//			System.out.println(x.toString());
//			InputStream is = x.openStream();
//			while(true) {
//				int c = is.read();
//				if (c >= 0) {
//					System.out.print((char)c);
//				}
//				else {
//					break;
//				}
//			}
//			is.close();
//			System.out.println();
//		}
//
//		System.out.println();
//
//		en = Thread.currentThread().getContextClassLoader().getResources(Bootstraper.class.getName().replace('.', '/') + ".class");
//		while(en.hasMoreElements()) {
//			URL x = (URL) en.nextElement();
//			System.out.println(x.toString());
//		}
	}	
}
