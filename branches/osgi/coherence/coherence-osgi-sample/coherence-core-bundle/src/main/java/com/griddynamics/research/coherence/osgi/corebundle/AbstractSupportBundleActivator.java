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
import com.tangosol.run.xml.XmlHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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

        CacheFactory.getCacheFactoryBuilder().setCacheConfiguration(
                getClassLoader(bundleContext),
                XmlHelper.loadXml(cacheConfig)
        );
    }

    protected abstract ClassLoader getClassLoader(BundleContext bundleContext) throws ClassNotFoundException;

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        CacheFactory.getCacheFactoryBuilder().releaseAll(getClassLoader(bundleContext));
    }
}
