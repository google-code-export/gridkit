package com.griddynamics.coherence.support;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.sun.org.apache.xerces.internal.dom.DOMImplementationImpl;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.*;

public class CoherenceNamespaceHandler extends NamespaceHandlerSupport {

    public CoherenceNamespaceHandler() {
        registerBeanDefinitionParser("cache-config",
                new CoherenceBeanDefinitionParser());
    }

    public void init() {
    }

    private static class CoherenceBeanDefinitionParser extends
            AbstractSimpleBeanDefinitionParser {

        protected Class getBeanClass(Element element) {
//            return SpringDefaultConfigurableCacheFactory.class;
            return SpringAwareCacheFactory.class;
        }

        protected void doParse(Element element, ParserContext parserContext,
                               BeanDefinitionBuilder builder) {
            super.doParse(element, parserContext, builder);

            try {
//                deletePrefixRecursive(element);
//                Transformer transformer = TransformerFactory.newInstance().newTransformer();
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                transformer.transform(new DOMSource(element), new StreamResult(out));

                DOMImplementation domImpl = new DOMImplementationImpl();
                Document doc = domImpl.createDocument(null, null, null);

                Node root = doc.adoptNode(element);
                deletePrefixRecursive(root);

                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                transformer.transform(new DOMSource(root), new StreamResult(out));


                String xml = new String(out.toByteArray(), "UTF-8");
//                System.out.println(xml);
                builder.addConstructorArgValue(xml);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void deletePrefixRecursive(Node node) {
        if (node.getPrefix() != null)
            node.setPrefix(null);
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
            deletePrefixRecursive(list.item(i));
    }
}
