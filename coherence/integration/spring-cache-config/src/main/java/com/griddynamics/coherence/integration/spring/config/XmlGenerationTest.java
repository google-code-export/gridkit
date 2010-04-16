package com.griddynamics.coherence.integration.spring.config;

import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * @author Dmitri Babaev
 */
public class XmlGenerationTest {

	public static void main(String[] args) throws Exception {
		CacheConfig config = new CacheConfig();
		config.cacheMappings = Arrays.asList(new CacheMapping("test", new CachingScheme("ssas")) );

		JAXBContext jc = JAXBContext.newInstance(CacheConfig.class);
		Marshaller marshaller = jc.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(config, System.out);
	}

}
