/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.profile.distributed;


/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class ClusterProperty extends ClusterPropertyKey {

    private Object value;
    
    protected ClusterProperty() {
        // for serialization
    }
    
    public ClusterProperty(ClusterPropertyKey key, Object value) {
        super(key);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return super.toString() + " -> " + value;
    }
}
