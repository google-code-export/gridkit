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
package com.griddynamics.research.coherence.osgi.support;

import com.griddynamics.research.coherence.osgi.corebundle.AbstractSupportBundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Properties;

import com.griddynamics.research.coherence.osgi.service.service.api.CacheFactoryService;
import com.griddynamics.research.coherence.osgi.service.service.impl.CacheFactoryServiceImpl;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class SupportBundleActivator extends AbstractSupportBundleActivator {

    private ClassLoader classLoader;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        classLoader = this.getClass().getClassLoader();

        final Properties properties = new Properties();
        properties.setProperty("applicationId", "app2");
        bundleContext.registerService(
                CacheFactoryService.class.getName(),
                new CacheFactoryServiceImpl(classLoader),
                properties);

        super.start(bundleContext);
    }

    @Override
    protected ClassLoader getClassLoader(BundleContext bundleContext) throws ClassNotFoundException {
        return classLoader;
    }
}
