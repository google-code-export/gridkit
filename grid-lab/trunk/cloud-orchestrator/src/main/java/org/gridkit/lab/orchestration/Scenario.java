package org.gridkit.lab.orchestration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.gridkit.lab.orchestration.script.Script;

public class Scenario {
    private final Script script;

    public Scenario(Script script) {
        this.script = script;
    }
    
    public void play() {
        script.execute();
    }
    
    public void play(long timeout, TimeUnit unit) throws TimeoutException {
        script.execute(timeout, unit);
    }
}
