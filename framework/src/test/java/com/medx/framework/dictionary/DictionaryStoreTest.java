package com.medx.framework.dictionary;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.medx.framework.dictionary.model.Dictionary;

public class DictionaryStoreTest {
	@Test
	public void testLoad() throws JAXBException {
		DictionaryStore ds = new DictionaryStore();
		
		Dictionary dictionary = ds.loadDictionary("src/test/resources/xml/test-dictionary.xml");
	}
}
