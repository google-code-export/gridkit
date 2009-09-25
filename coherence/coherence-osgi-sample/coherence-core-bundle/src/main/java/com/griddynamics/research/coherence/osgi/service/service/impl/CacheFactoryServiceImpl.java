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
package com.griddynamics.research.coherence.osgi.service.service.impl;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.griddynamics.research.coherence.osgi.service.service.api.CacheFactoryService;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class CacheFactoryServiceImpl implements CacheFactoryService {

    private final ClassLoader classLoader;

    public CacheFactoryServiceImpl(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public NamedCache getCache(String name) {
        if (classLoader == null)
            throw new RuntimeException("Bean Not Initialized");
        return CacheFactory.getCache(name, classLoader);
    }
}
