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
package org.questora.osgi.samples.support;

import org.questora.osgi.samples.corebundle.AbstractSupportBundleActivator;
import org.osgi.framework.BundleContext;
import ru.questora.osgi.samples.service.api.CacheFactoryService;
import ru.questora.osgi.samples.service.impl.CacheFactoryServiceImpl;

import java.util.Properties;

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
        properties.setProperty("applicationId", "app1");
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
