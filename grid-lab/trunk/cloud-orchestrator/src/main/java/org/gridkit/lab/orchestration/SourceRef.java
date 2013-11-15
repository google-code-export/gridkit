package org.gridkit.lab.orchestration;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import org.gridkit.lab.orchestration.util.ClassOps;

public class SourceRef implements Serializable {
    private static final long serialVersionUID = -816014720806663680L;

    private static AtomicLong NEXT_ID = new AtomicLong(0);
    
    private final String value;
    private final String id;
    
    public SourceRef(String target, StackTraceElement location) {
        this.value = target + " at " + ClassOps.toString(location);
        this.id = this.value + " # " + NEXT_ID.getAndIncrement();
    }
    
    public String getId() {
        return id;
    }
    
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        SourceRef other = (SourceRef) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
