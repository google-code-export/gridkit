package org.gridkit.lab.orchestration.script;

import java.util.List;

public class CycleDetectedException extends RuntimeException {
    private static final long serialVersionUID = 305061584714268176L;
    
    private final List<ScriptAction> cycle;

    public CycleDetectedException(List<ScriptAction> cycle) {
        this.cycle = cycle;
    }
    
    public List<ScriptAction> getCycle() {
        return cycle;
    }
}
