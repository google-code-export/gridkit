package org.gridkit.lab.orchestration.script;

import java.io.Serializable;

public class Checkpoint implements ScriptAction, Serializable {
    private static final long serialVersionUID = 7844703208347065282L;
        
    private final String name;
    
    public Checkpoint(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Object getId() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Checkpoint other = (Checkpoint) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
