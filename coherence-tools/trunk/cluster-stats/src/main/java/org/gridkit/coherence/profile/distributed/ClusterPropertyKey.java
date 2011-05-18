/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.profile.distributed;

import java.io.Serializable;

import com.tangosol.util.Binary;
import com.tangosol.util.UID;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class ClusterPropertyKey implements Serializable {
	private static final long serialVersionUID = 1L;
	
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
