package org.gridkit.lab.orchestration.script;

import java.io.Serializable;

public class Checkpoint implements ScriptAction, Serializable {
    private static final long serialVersionUID = 7844703208347065282L;
        
    private final String name;
    private final String id;
    
    public Checkpoint(String name) {
        this.name = name;
        this.id = "Checkpoint[" + name + "]";
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String toString() {
        return getId();
    }
}
