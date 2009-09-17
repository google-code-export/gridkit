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
package org.questora.osgi.samples.corebundle;

import com.tangosol.net.DefaultCacheFactoryBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class MyCacheFactoryBuilder extends DefaultCacheFactoryBuilder {

    private final Map<ClassLoader, String> scopeNameMapping = new HashMap<ClassLoader, String>();

    public MyCacheFactoryBuilder() {
        super(STRATEGY_RENAME_UNIQUELY);
    }

    @Override
    protected String getScopeName(ClassLoader classLoader) {
        String result = scopeNameMapping.get(classLoader);
        if (result != null)
            return result;

        result = "Scope" + classLoader.toString();
        scopeNameMapping.put(classLoader, result);

        return result;
    }
}
