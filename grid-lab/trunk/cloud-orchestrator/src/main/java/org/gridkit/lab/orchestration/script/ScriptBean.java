package org.gridkit.lab.orchestration.script;

import org.gridkit.util.concurrent.Box;

public interface ScriptBean {
    Object getRef();
    
    void create(Box<Void> box);
}
