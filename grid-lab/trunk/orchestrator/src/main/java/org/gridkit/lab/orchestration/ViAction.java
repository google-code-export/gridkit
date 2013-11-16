package org.gridkit.lab.orchestration;

import java.util.concurrent.Callable;

import org.gridkit.lab.orchestration.script.ScriptAction;

public interface ViAction extends ScriptAction {
    Scope getScope();
    
    Callable<Void> getExecutor();
}
