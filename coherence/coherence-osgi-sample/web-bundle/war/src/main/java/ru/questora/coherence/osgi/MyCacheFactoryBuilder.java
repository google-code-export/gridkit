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

import com.tangosol.net.DefaultCacheFactoryBuilder;
import com.tangosol.net.ConfigurableCacheFactory;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class MyCacheFactoryBuilder extends DefaultCacheFactoryBuilder {

    public MyCacheFactoryBuilder() {
        super(DefaultCacheFactoryBuilder.STRATEGY_RENAME_UNIQUELY);
    }


    public ConfigurableCacheFactory getConfigurableCacheFactory(ClassLoader classLoader) {
        return super.getConfigurableCacheFactory(classLoader);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public ConfigurableCacheFactory getConfigurableCacheFactory(String s, ClassLoader classLoader) {
        return super.getConfigurableCacheFactory(s, classLoader);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
