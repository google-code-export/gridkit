package com.medx.framework.dictionary;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.medx.framework.dictionary.model.Dictionary;

public class DictionaryJaxbContext {
	private static JAXBContext jaxbContext;
	
	public static synchronized JAXBContext getInstance() throws JAXBException {
		if (jaxbContext == null)
			jaxbContext = JAXBContext.newInstance(Dictionary.class.getPackage().getName(), Dictionary.class.getClassLoader());
		
		return jaxbContext;
	}
}
