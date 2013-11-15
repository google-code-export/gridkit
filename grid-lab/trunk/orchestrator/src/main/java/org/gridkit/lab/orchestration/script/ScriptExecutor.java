package org.gridkit.lab.orchestration.script;

public interface ScriptExecutor {
    
    void execute(ScriptAction action, Box box);
    
    public interface Box {
        void success();
        
        void failure(Throwable e);
    }
}
