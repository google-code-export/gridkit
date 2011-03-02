package com.medx.framework.dictionary;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.medx.framework.dictionary.model.Dictionary;

public class DictionaryWriter {
	private JAXBContext jaxbContext;
	
	private Marshaller marshaller;
	
	public DictionaryWriter() throws JAXBException {
		jaxbContext = Dictionary.getJaxbContext();
		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	}
	
	public void writeDictionary(String fileName, Dictionary dictionary) throws JAXBException {
		marshaller.marshal(dictionary, new File(fileName));
	}
}
