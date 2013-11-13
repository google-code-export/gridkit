package org.gridkit.lab.orchestration;

import org.gridkit.lab.orchestration.script.ScriptAction;

public interface SourceAction extends ScriptAction {
    StackTraceElement getLocation();
    
    String getSource();
}
