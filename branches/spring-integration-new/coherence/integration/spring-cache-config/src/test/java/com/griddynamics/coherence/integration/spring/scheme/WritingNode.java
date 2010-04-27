package com.griddynamics.coherence.integration.spring.scheme;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tangosol.net.NamedCache;

public class WritingNode {

	public static void main(String[] args) {
		System.setProperty("tangosol.coherence.wka", "localhost");
		
		ApplicationContext context = new ClassPathXmlApplicationContext("scheme/example-context.xml");
		NamedCache cache = context.getBean("simpleDistributedCache", NamedCache.class);
		
		String res = (String) cache.get("aaa");
		System.out.println(String.format("'%s'", res));
		
		cache.put("aaa", res+"b");
		
		res = (String) cache.get("aaa");
		System.out.println(String.format("'%s'", res));
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
