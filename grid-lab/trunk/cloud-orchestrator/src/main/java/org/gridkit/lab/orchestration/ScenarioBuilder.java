package org.gridkit.lab.orchestration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.gridkit.lab.orchestration.script.Script;
import org.gridkit.lab.orchestration.script.ScriptBuilder;
import org.gridkit.lab.orchestration.script.ScriptExecutor.Box;
import org.gridkit.lab.orchestration.util.ClassOps;
import org.gridkit.lab.orchestration.util.NamedThreadFactory;

public class ScenarioBuilder implements Platform.ScriptConstructor {    
    private static ScheduledExecutorService SLEEP_EXECUTOR = Executors.newScheduledThreadPool(
        1, new NamedThreadFactory("Scenario-Builder-Sleep-Executor", true)
    );
    
    private Platform platform;
    private ScriptBuilder builder;
    private LocalNode localNode;
    private int nextSync;
    
    protected ScenarioBuilder(Platform platform) {
        this.platform = platform;
        this.builder = new ScriptBuilder();
        this.localNode = new LocalNode();
        this.nextSync = 0;
    }

    public void sync() {
        String location = ClassOps.toString(ClassOps.location(1));
        join("sync-" + (nextSync++) + " at " + location);
    }
    
    public SectionOptions from(String name) {
        builder.from(name);
        return new SectionOptions();
    }
    
    public SectionOptions fromStart() {
        builder.fromStart();
        return new SectionOptions();
    }
    
    public SectionOptions join(String name) {
        builder.join(name);
        return new SectionOptions();
    }
    
    public SectionOptions joinFinish() {
        builder.joinFinish();
        return new SectionOptions();
    }

    public void sleep(long timeout, TimeUnit unit) {
        builder.action(new Sleep(timeout, unit, ClassOps.location(1)));
    }
    
    public void sleep(long millis) {
        builder.action(new Sleep(millis, TimeUnit.MILLISECONDS, ClassOps.location(1)));
    }
        
    public RemoteNode node(Scope scope) {
        return new RemoteNode(scope);
    }
    
    public RemoteNode node(String pattern) {
        return node(Scopes.pattern(pattern)); 
    }
    
    public LocalNode local() {
        return localNode;
    }
    
    public ScriptBuilder getScriptBuilder() {
        return builder;
    }
    
    public Scenario build() {
        Script script = builder.build();
        platform.clearScriptConstructor();
        builder = null;
        return new Scenario(script, platform);
    }
    
    public class RemoteNode {
        private final Scope scope;
        
        public RemoteNode(Scope scope) {
            this.scope = scope;
        }

        public <T> T deploy(T prototype) {
            RemoteBean.Deploy bean = RemoteBean.newDeploy(prototype, scope, ClassOps.location(1));
            builder.action(bean);
            return bean.getProxy(platform);
        }
        
        public <T> T bean(T bean) {
            return platform.node(scope).bean(bean);
        }
    }
    
    public class LocalNode {
        public <T> T deploy(T prototype) {
            return null;
        }
    }
    
    public class SectionOptions {
        public SectionOptions par() {
            builder.par();
            return this;
        }
        
        public SectionOptions seq() {
            builder.seq();
            return this;
        }
        //TODO scope empty section
    }
    
    private static class Sleep implements ExecutableAction, Runnable {
        private final SourceRef ref;
        private final long timeout;
        private final TimeUnit unit;
        
        private Box box;
        
        public Sleep(long timeout, TimeUnit unit, StackTraceElement location) {
            this.ref = new SourceRef("sleep(" + timeout + " " + toString(unit) + ")", location);
            this.timeout = timeout;
            this.unit = unit;
        }

        @Override
        public void execute(Box box) {
            this.box = box;
            try {
                SLEEP_EXECUTOR.schedule(this, timeout, unit);
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
        
        private static String toString(TimeUnit unit) {
            if (TimeUnit.NANOSECONDS == unit) {
                return "ns";
            } else if (TimeUnit.MICROSECONDS == unit) {
                return "us";
            } else if (TimeUnit.MILLISECONDS == unit) {
                return "ms";
            } else if (TimeUnit.SECONDS == unit) {
                return "s";
            } else if (TimeUnit.MINUTES == unit) {
                return "m";
            } else if (TimeUnit.HOURS == unit) {
                return "h";
            } else if (TimeUnit.DAYS == unit) {
                return "d";
            } else {
                throw new IllegalArgumentException();
            }
        }
    }
}
