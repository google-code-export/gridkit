package org.gridkit.lab.orchestration;

import java.util.Collections;

import org.gridkit.lab.orchestration.script.Checkpoint.LogListener;
import org.gridkit.lab.orchestration.script.Script;
import org.gridkit.lab.orchestration.script.ScriptBuilder;

public class ScenarioBuilder implements Platform.ScriptConstructor {
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
        builder.from(name).setListener(LogListener.INSTANCE);
    }
    
    public void fromStart() {
        builder.fromStart().setListener(LogListener.INSTANCE);
    }
    
    public void join(String name) {
        builder.join(name).setListener(LogListener.INSTANCE);
    }
    
    public void joinFinish() {
        builder.joinFinish().setListener(LogListener.INSTANCE);
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
        return new Scenario(script);
    }
    
    public class RemoteNode {
        private final Scope scope;
        
        public RemoteNode(Scope scope) {
            this.scope = scope;
        }

        public <T> T deploy(T prototype) {
            RemoteBean.Deploy bean = platform.newRemoteBean(scope, prototype, ClassOps.stackTraceElement(1));
            builder.create(bean.getScenarioBean(), Collections.emptyList());
            return bean.getProxy();
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
}
