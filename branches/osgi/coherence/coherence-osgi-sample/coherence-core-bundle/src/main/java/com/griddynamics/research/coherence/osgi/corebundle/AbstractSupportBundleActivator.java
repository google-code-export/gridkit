/*
* Copyright (c) 2006-2009 Grid Dynamics, Inc.
* 2030 Bent Creek Dr., San Ramon, CA 94582
* All Rights Reserved.
*
* This software is the confidential and proprietary information of
* Grid Dynamics, Inc. ("Confidential Information"). You shall not
* disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with Grid Dynamics.
*/
package com.griddynamics.research.coherence.osgi.corebundle;

import com.tangosol.net.CacheFactory;
import com.tangosol.run.xml.XmlDocument;
import com.tangosol.run.xml.XmlElement;
import com.tangosol.run.xml.XmlHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public abstract class AbstractSupportBundleActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        final URL cacheConfig = bundleContext.getBundle().getResource("cache-config.xml");

        final XmlDocument cacheConfigXml = XmlHelper.loadXml(cacheConfig);

        XmlElement el;
        for (Object o : cacheConfigXml.getElement("caching-schemes").getElementList()) {
            el = (XmlElement) o;
            if ((el = el.getElement("serializer")) != null &&
                    el.getElement("class-name").getString().contains(ByValueConfigurablePofContext.class.getName())) {
                for (Object o1 : el.getElement("init-params").getElementList())
                    if ("String".equalsIgnoreCase(((XmlElement) o1).getElement("param-type").getString())) {
                        final XmlElement configNameElement = ((XmlElement) o1).getElement("param-value");
                        String configName;
                        if ((configName = configNameElement.getString()).contains(".xml")) {
                            final URL pofConfigResource = bundleContext.getBundle().getResource(configName);
                            assert pofConfigResource != null;
                            configNameElement.setString(readFromUrl(pofConfigResource));
                        }
                    }
            }
        }

        CacheFactory.getCacheFactoryBuilder().setCacheConfiguration(
                getClassLoader(bundleContext),
                cacheConfigXml
        );
    }

    protected abstract ClassLoader getClassLoader(BundleContext bundleContext) throws ClassNotFoundException;

    private String readFromUrl(URL url) throws IOException {
        final BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
        StringBuffer stringBuffer = new StringBuffer();
        final int bytesAvailable = inputStream.available();
        byte bytes[] = new byte[bytesAvailable];
        inputStream.read(bytes);
        for (byte b : bytes)
            stringBuffer.append((char) b);
        return stringBuffer.toString();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        CacheFactory.getCacheFactoryBuilder().releaseAll(getClassLoader(bundleContext));
    }
}
