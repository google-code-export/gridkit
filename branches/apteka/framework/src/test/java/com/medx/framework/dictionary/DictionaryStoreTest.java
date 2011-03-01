package com.medx.framework.dictionary;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.dictionary.model.TypeDescriptor;

public class DictionaryStoreTest {
	@Test
	public void testLoad() throws JAXBException {
		DictionaryStore ds = new DictionaryStore();
		
		Dictionary dictionary = ds.loadDictionary("src/test/resources/xml/test-dictionary.xml");
		//Dictionary dictionary = ds.loadDictionary("/tmp/d.xml");
		
		System.out.println(dictionary.getAttributeDescriptors().size());
		System.out.println(dictionary.getTypeDescriptors().size());
	}
	
	@Test
	public void testStore() throws JAXBException {
		DictionaryStore ds = new DictionaryStore();
		
		Dictionary dictionary = new Dictionary();
		
		dictionary.setAttributeDescriptors(new ArrayList<AttributeDescriptor>());
		dictionary.setTypeDescriptors(new ArrayList<TypeDescriptor>());
		dictionary.setVersion(1);
		
		TypeDescriptor typeDescriptor = new TypeDescriptor();
		typeDescriptor.setClazz("qqq");
		typeDescriptor.setId(1);
		typeDescriptor.setVersion(0);
		
		dictionary.getTypeDescriptors().add(typeDescriptor);
		
		ds.storeDictionary("/tmp/d.xml", dictionary);
	}
}
