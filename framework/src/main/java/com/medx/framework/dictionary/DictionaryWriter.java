package com.medx.framework.dictionary;

import java.io.File;
import java.io.Writer;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.medx.framework.dictionary.model.Dictionary;

public class DictionaryWriter {
	private Marshaller marshaller;
	
	public DictionaryWriter() throws JAXBException {
		marshaller = DictionaryJaxbContext.getInstance().createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	}
	
	public void writeDictionary(String fileName, Dictionary dictionary) throws JAXBException {
		marshaller.marshal(dictionary, new File(fileName));
	}
	
	public void writeDictionary(Writer writer, Dictionary dictionary) throws JAXBException {
		marshaller.marshal(dictionary, writer);
	}
}
