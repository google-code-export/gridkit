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
package ru.questora.research.coherence.osgi.support.impl;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.run.xml.XmlHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import ru.questora.research.coherence.osgi.support.api.CacheFactoryService;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class CacheFactoryServiceImpl implements CacheFactoryService, ApplicationContextAware, DisposableBean {

    private final ClassLoader classLoader = this.getClass().getClassLoader();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        final Resource coherenceXmlConfigResource = applicationContext.getResource("cache-config.xml");
        try {
            CacheFactory.getCacheFactoryBuilder().setCacheConfiguration(
                    classLoader,
                    XmlHelper.loadXml(new FileInputStream(coherenceXmlConfigResource.getFile()))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NamedCache getCache(String name) {
        return CacheFactory.getCache(name, classLoader);
    }

    @Override
    public void destroy() throws Exception {
        CacheFactory.getCacheFactoryBuilder().releaseAll(classLoader);
    }
}
