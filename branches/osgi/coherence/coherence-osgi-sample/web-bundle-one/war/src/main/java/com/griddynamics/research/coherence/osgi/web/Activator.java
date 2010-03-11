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
package com.griddynamics.research.coherence.osgi.web;

import org.osgi.framework.AllServiceListener;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class Activator implements BundleActivator {

    private static Object cacheFactoryService = null;

    private Activator.CacheServiceListener cacheServiceListener;

    public void start(final BundleContext bundleContext) throws Exception {
        cacheServiceListener = new Activator.CacheServiceListener(bundleContext);
        bundleContext.addServiceListener(cacheServiceListener, "(applicationId=app1)");
    }

    public void stop(BundleContext bundleContext) throws Exception {
        bundleContext.removeServiceListener(cacheServiceListener);
    }

    public static Object getCacheFactoryService() {
        return cacheFactoryService;
    }

    private class CacheServiceListener implements AllServiceListener {

        private final BundleContext bundleContext;

        public CacheServiceListener(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        public void serviceChanged(ServiceEvent serviceEvent) {
            if (serviceEvent.getType() != ServiceEvent.UNREGISTERING) {
                cacheFactoryService = bundleContext.getService(serviceEvent.getServiceReference());
            }
        }
    }
}
