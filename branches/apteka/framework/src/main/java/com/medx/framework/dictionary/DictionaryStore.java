package com.medx.framework.dictionary;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.medx.framework.dictionary.model.Dictionary;

public class DictionaryStore {
	private final JAXBContext jaxbContext;
	
	private final Marshaller marshaller;
	private final Unmarshaller unmarshaller;
	
	public DictionaryStore() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(Dictionary.class.getPackage().getName());
		
		marshaller = jaxbContext.createMarshaller();
		unmarshaller = jaxbContext.createUnmarshaller();
	}
	
	public Dictionary loadDictionary(String fileName) throws JAXBException {
		return (Dictionary) unmarshaller.unmarshal(new File(fileName));
	}
	
	public void storeDictionary(String fileName, Dictionary dictionary) throws JAXBException {
		marshaller.marshal(dictionary, new File(fileName));
	}
}
