/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.profile.distributed;

import com.tangosol.util.Binary;
import com.tangosol.util.UID;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class ClusterPropertyKey {

    private Binary memberId;
    private String propName;
    
    protected ClusterPropertyKey() {
        // for serializer
    }
    
    public ClusterPropertyKey(UID memberId, String propName) {
        this.memberId = new Binary(memberId.toByteArray());
        this.propName = propName;
    }
    
    public ClusterPropertyKey(ClusterPropertyKey key) {
        this.memberId = key.memberId;
        this.propName = key.propName;
    }

    public UID getMemberId() {
        return new UID(memberId.toByteArray());
    }
    
    public String getPropName() {
        return propName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((memberId == null) ? 0 : memberId.hashCode());
        result = prime * result + ((propName == null) ? 0 : propName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClusterPropertyKey other = (ClusterPropertyKey) obj;
        if (memberId == null) {
            if (other.memberId != null)
                return false;
        } else if (!memberId.equals(other.memberId))
            return false;
        if (propName == null) {
            if (other.propName != null)
                return false;
        } else if (!propName.equals(other.propName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return propName + "(" + memberId.hashCode() + ")";
    }
}
