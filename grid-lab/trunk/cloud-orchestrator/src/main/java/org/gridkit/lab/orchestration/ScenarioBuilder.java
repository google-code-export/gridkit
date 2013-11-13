package org.gridkit.lab.orchestration;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.gridkit.lab.orchestration.script.Checkpoint;
import org.gridkit.lab.orchestration.script.CycleDetectedException;
import org.gridkit.lab.orchestration.script.Script;
import org.gridkit.lab.orchestration.script.ScriptAction;
import org.gridkit.lab.orchestration.script.ScriptBuilder;
import org.gridkit.lab.orchestration.script.ScriptExecutor.Box;
import org.gridkit.lab.orchestration.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScenarioBuilder implements Platform.ScriptConstructor {
    private static final Logger log = LoggerFactory.getLogger(ScenarioBuilder.class);
    
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
        String location = ClassOps.location(ClassOps.stackTraceElement(1));
        join("sync-" + (nextSync++) + " at " + location);
    }
    
    public void from(String name) {
        builder.from(name);
    }
    
    public void fromStart() {
        builder.fromStart();
    }
    
    public void join(String name) {
        try {
            builder.join(name);
        } catch (CycleDetectedException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cycle detected during scenario construction:\n");
            Iterator<ScriptAction> iter = e.getCycle().iterator();
            
            while (iter.hasNext()) {
                ScriptAction rawAction = iter.next();
                
                sb.append("\t");
                
                if (rawAction instanceof Checkpoint) {
                    Checkpoint action = (Checkpoint)rawAction;
                    sb.append("\tCheckpoint " + action.getName());
                } else if (rawAction instanceof SourceAction) {
                    SourceAction action = (SourceAction)rawAction;
                    sb.append("\t" + action.getSource() + " at " + ClassOps.location(action.getLocation()));
                } else {
                    sb.append("\t" + rawAction);
                }
                
                if (iter.hasNext()) {
                    sb.append("\n");
                }
            }
            
            log.error(sb.toString());
            throw e;
        }
    }
    
    public void joinFinish() {
        builder.joinFinish();
    }
    
    public void sleep(long timeout, TimeUnit unit) {
        builder.action(new Sleep(timeout, unit));
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
            RemoteBean.Deploy bean = platform.newRemoteBean(scope, prototype, ClassOps.stackTraceElement(1));
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
    
    private static class Sleep implements ExecutableAction, Runnable {
        private final long timeout;
        private final TimeUnit unit;
        private final String id = UUID.randomUUID().toString();
        
        private Box box;
        
        public Sleep(long timeout, TimeUnit unit) {
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
        public Object getId() {
            return id;
        }
    }
}
