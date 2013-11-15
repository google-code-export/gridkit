package org.gridkit.lab.orchestration;

import org.gridkit.lab.orchestration.script.ScriptAction;
import org.gridkit.lab.orchestration.script.ScriptExecutor.Box;

public interface ExecutableAction extends ScriptAction {
    void execute(Box box);
}
