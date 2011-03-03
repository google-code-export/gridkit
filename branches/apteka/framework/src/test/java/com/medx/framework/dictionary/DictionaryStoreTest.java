package com.medx.framework.dictionary;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.dictionary.model.TypeDescriptor;

@Ignore
public class DictionaryStoreTest {
	@Test
	public void testRead() throws JAXBException, SAXException, IOException {
		DictionaryReader ds = new DictionaryReader();
		
		Dictionary dictionary = ds.readDictionary("src/test/resources/xml/test-dictionary.xml");
		
		System.out.println(dictionary.getAttributeDescriptors().size());
		System.out.println(dictionary.getTypeDescriptors().size());
	}
	
	@Test
	public void testStore() throws JAXBException {
		DictionaryWriter ds = new DictionaryWriter();
		
		Dictionary dictionary = new Dictionary();
		
		dictionary.setAttributeDescriptors(new ArrayList<AttributeDescriptor>());
		dictionary.setTypeDescriptors(new ArrayList<TypeDescriptor>());
		dictionary.setVersion(1);
		
		TypeDescriptor typeDescriptor = new TypeDescriptor();
		typeDescriptor.setClazz("qqq");
		typeDescriptor.setId(1);
		typeDescriptor.setVersion(0);
		
		dictionary.getTypeDescriptors().add(typeDescriptor);
		
		ds.writeDictionary("/tmp/d.xml", dictionary);
	}
}
