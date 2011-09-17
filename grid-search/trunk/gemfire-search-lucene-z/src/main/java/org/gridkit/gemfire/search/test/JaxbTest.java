package org.gridkit.gemfire.search.test;

import org.gridkit.search.gemfire.benchmark.model.Fts;
import org.gridkit.search.gemfire.benchmark.model.JaxbFactory;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbTest {
    public static void main(String[] args) throws JAXBException {
        Marshaller marshaller = JaxbFactory.createMarshaller();
        Unmarshaller unmarshaller = JaxbFactory.createUnmarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        Fts fts = (Fts)unmarshaller.unmarshal(ClassLoader.getSystemClassLoader().getResourceAsStream("fts.sample.xml"));

        marshaller.marshal(fts, System.out);
    }
}
