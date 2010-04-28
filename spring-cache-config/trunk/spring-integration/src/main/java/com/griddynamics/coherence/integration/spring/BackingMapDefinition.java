package com.griddynamics.coherence.integration.spring;

import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.util.ObservableMap;

/**
 * @author Dmitri Babaev
 */
public interface BackingMapDefinition {
	ObservableMap newBackingMap(BackingMapManagerContext context);
}
