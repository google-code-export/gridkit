package org.gridkit.nimble.platform;

import java.util.concurrent.ConcurrentMap;

public interface AttributeContext {
    ConcurrentMap<String, Object> getAttributesMap();
}
