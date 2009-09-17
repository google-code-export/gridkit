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
package ru.questora.coherence.osgi;

import com.tangosol.net.NamedCache;
import com.tangosol.net.CacheFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class Activator implements BundleActivator {

    private static NamedCache testCache;

    private static BundleContext bundleContext;

    public void start(BundleContext bundleContext) throws Exception {
        if (testCache != null)
            throw new IllegalStateException("testCache must not be set twice");
        this.bundleContext = bundleContext;

        final MyCacheFactoryBuilder builder = new MyCacheFactoryBuilder();
        CacheFactory.setCacheFactoryBuilder(builder);
        
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    public static NamedCache getTestCache() {
        return testCache;
    }
}
