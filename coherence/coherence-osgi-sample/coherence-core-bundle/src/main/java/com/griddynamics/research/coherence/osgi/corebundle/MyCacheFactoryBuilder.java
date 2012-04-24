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

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.DefaultCacheFactoryBuilder;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class MyCacheFactoryBuilder extends DefaultCacheFactoryBuilder {

    public MyCacheFactoryBuilder() {
        super(STRATEGY_RENAME_UNIQUELY);
    }

    @Override
	public ConfigurableCacheFactory getSingletonFactory() {
		return super.getSingletonFactory();
	}

	@Override
    protected String getScopeName(ClassLoader classLoader) {
        return "Scope(" + classLoader.toString() + ")";
    }
}
