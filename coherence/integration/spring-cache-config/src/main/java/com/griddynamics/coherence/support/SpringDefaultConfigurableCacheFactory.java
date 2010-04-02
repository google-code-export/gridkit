package com.griddynamics.coherence.support;

import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.run.xml.SimpleParser;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: stryuber
 * Date: 31.03.2010
 * Time: 13:39:32
 * When you use namespaces in spring the only way to resolve property placeholders
 * is to pass them in bean definition class in String format. This class receive
 * xml config in String forman and then jast converts to XMLDocument
 */
class SpringDefaultConfigurableCacheFactory extends DefaultConfigurableCacheFactory {
    SpringDefaultConfigurableCacheFactory(String xmlConfig) throws IOException {
        super(new SimpleParser().parseXml(xmlConfig));
    }
}
