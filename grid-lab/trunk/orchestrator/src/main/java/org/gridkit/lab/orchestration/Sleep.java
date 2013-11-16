package org.gridkit.lab.orchestration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.gridkit.lab.orchestration.script.ScriptExecutor.Box;
import org.gridkit.lab.orchestration.util.NamedThreadFactory;

public class Sleep implements ExecutableAction, Runnable {
    private static ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(
        1, new NamedThreadFactory("Sleep-Executor", true)
    );
    
    private final SourceRef ref;
    private final long timeout;
    private final TimeUnit unit;
    
    private Box box;
    
    public Sleep(SourceRef ref, long timeout, TimeUnit unit) {
        this.ref = ref;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public void execute(Box box) {
        this.box = box;
        try {
            EXECUTOR.schedule(this, timeout, unit);
        } catch (RuntimeException e) {
            box.failure(e);
        }
    }

    @Override
    public void run() {
        box.success();
    }

    @Override
    public String getId() {
        return ref.getId();
    }
    
    @Override
    public String toString() {
        return ref.toString();
    }

}
