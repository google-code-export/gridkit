package org.gridkit.gemfire.search.demo.model;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbFactory {
    private static final JAXBContext jaxbContext;

    static {
        JAXBContext localJaxbContext = null;

        try {
            localJaxbContext = JAXBContext.newInstance(Fts.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

        jaxbContext = localJaxbContext;
    }

    public static Marshaller createMarshaller() {
        try {
            return jaxbContext.createMarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static Unmarshaller createUnmarshaller() {
        try {
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
