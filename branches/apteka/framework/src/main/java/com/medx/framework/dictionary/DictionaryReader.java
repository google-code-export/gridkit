package com.medx.framework.dictionary;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.medx.framework.dictionary.model.AttributeDescriptor;
import com.medx.framework.dictionary.model.Dictionary;
import com.medx.framework.dictionary.model.TypeDescriptor;

public class DictionaryReader {
	private static final Logger log = LoggerFactory.getLogger(DictionaryReader.class);
	
	private JAXBContext jaxbContext;
	
	private Unmarshaller unmarshaller;
	
	private ValidationEventCollector validationCollector = new ValidationEventCollector();
	
	public DictionaryReader() throws JAXBException, SAXException, IOException {
		jaxbContext = Dictionary.getJaxbContext();
		
		unmarshaller = jaxbContext.createUnmarshaller();
		
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		InputStream schemaStream = DictionaryWriter.class.getResourceAsStream("/xml/framework/dictionary.xsd");
		
		Schema schema = null;
		try {
			schema = schemaFactory.newSchema(new StreamSource(schemaStream));
		}
		finally {
			schemaStream.close();
		}
		
		unmarshaller.setSchema(schema);
		unmarshaller.setEventHandler(validationCollector);
	}
	
	public Dictionary readDictionary(String fileName) throws JAXBException {
		Dictionary result = null;
		
		try {
			result = (Dictionary) unmarshaller.unmarshal(new File(fileName));
		}
		catch (UnmarshalException e) {
			loadValidationErrors(validationCollector);
			validationCollector.reset();
			throw e;
		}
		
		completeDictionary(result);
		
		return result;
	}
	
	private static void loadValidationErrors(ValidationEventCollector validationCollector) {
		for(ValidationEvent event : validationCollector.getEvents() ){
			ValidationEventLocator locator = event.getLocator();
			
			String message = event.getMessage();
			int line = locator.getLineNumber();
			int column = locator.getColumnNumber();
			
			log.warn(format("Error at line %d column %d: %s", line, column, message));
		}
	}
	
	private static void completeDictionary(Dictionary dictionary) {
		compleateTypeDescriptors(dictionary);
		compleateAttributeDescriptors(dictionary);
	}
	
	private static void compleateAttributeDescriptors(Dictionary dictionary) {
		if (dictionary.getAttributeDescriptors() == null)
			dictionary.setAttributeDescriptors(new ArrayList<AttributeDescriptor>());
		
		for (AttributeDescriptor attributeDescriptor : dictionary.getAttributeDescriptors()) {
			if (attributeDescriptor.getVersion() == null)
				attributeDescriptor.setVersion(dictionary.getVersion());
			
			if (attributeDescriptor.getDescription() == null)
				attributeDescriptor.setDescription("");
		}
	}
	
	private static void compleateTypeDescriptors(Dictionary dictionary) {
		if (dictionary.getTypeDescriptors() == null)
			dictionary.setTypeDescriptors(new ArrayList<TypeDescriptor>());
		
		for (TypeDescriptor typeDescriptor : dictionary.getTypeDescriptors())
			if (typeDescriptor.getVersion() == null)
				typeDescriptor.setVersion(dictionary.getVersion());
	}
}
