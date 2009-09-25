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

import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.run.xml.XmlHelper;

import java.io.IOException;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class ByValueConfigurablePofContext extends ConfigurablePofContext {

    public ByValueConfigurablePofContext(String config) throws IOException {
        super(XmlHelper.loadXml(config));
    }
}
