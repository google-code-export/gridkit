package org.gridkit.lab.orchestration.script;

import org.gridkit.util.concurrent.Box;

public interface ScriptAction {
    void execute(Box<Void> box);
}
