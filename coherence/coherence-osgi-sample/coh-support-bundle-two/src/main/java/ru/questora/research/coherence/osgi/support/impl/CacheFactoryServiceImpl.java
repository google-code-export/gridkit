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

import ru.questora.research.coherence.osgi.support.api.two.CacheFactoryService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.CacheFactory;
import com.tangosol.run.xml.XmlHelper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class CacheFactoryServiceImpl implements CacheFactoryService, ApplicationContextAware, DisposableBean {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        final Resource coherenceXmlConfigResource = applicationContext.getResource("cache-config.xml");
        try {
            CacheFactory.getCacheFactoryBuilder().setCacheConfiguration(
                    getMappedClassLoader(),
                    XmlHelper.loadXml(new FileInputStream(coherenceXmlConfigResource.getFile()))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NamedCache getCache(String name) {
        return CacheFactory.getCache(name, getMappedClassLoader());
    }

    private ClassLoader getMappedClassLoader() {
        return this.getClass().getClassLoader();
    }

    @Override
    public void destroy() throws Exception {
        CacheFactory.getCacheFactoryBuilder().releaseAll(getMappedClassLoader());
    }
}
