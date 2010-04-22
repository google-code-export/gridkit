package com.griddynamics.coherence.integration.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.tangosol.net.NamedCache;

public class WritingNode {

	public static void main(String[] args) {
		System.setProperty("tangosol.coherence.wka", "localhost");
		
		ApplicationContext context = new GenericXmlApplicationContext("classpath:/test-context.xml");
		NamedCache cache = context.getBean("simpleDistributedCache", NamedCache.class);
		
		cache.put("aaa", "bbb");
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
