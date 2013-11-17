package org.gridkit.lab.orchestration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.gridkit.lab.orchestration.script.ScriptExecutor.Box;
import org.gridkit.lab.orchestration.util.NamedThreadFactory;

public class Sleep implements ExecutableAction {
    private static ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(
        1, new NamedThreadFactory("Sleep-Executor", true)
    );
    
    private SourceRef ref;
    private long timeout;
    private TimeUnit unit;
    
    public Sleep(SourceRef ref, long timeout, TimeUnit unit) {
        this.ref = ref;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public void execute(Box box) {
        try {
            EXECUTOR.schedule(new Executor(box), timeout, unit);
        } catch (RuntimeException e) {
            box.failure(e);
        }
    }

    @Override
    public String getId() {
        return ref.getId();
    }
    
    @Override
    public String toString() {
        return ref.toString();
    }
    
    private static class Executor implements Runnable {
        private Box box;

        public Executor(Box box) {
            this.box = box;
        }

        @Override
        public void run() {
            box.success();            
        }
    }
}
