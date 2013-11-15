package org.gridkit.lab.orchestration;

import java.util.ArrayList;
import java.util.List;

import org.gridkit.lab.orchestration.script.ScriptAction;
import org.gridkit.lab.orchestration.script.ScriptBuilder;
import org.gridkit.lab.orchestration.util.ClassOps;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViNodeSet;

public class Platform {
    
    protected interface ScriptConstructor {
        ScriptBuilder getScriptBuilder();
    }
        
    private final ViNodeSet cloud;
    
    private ScriptConstructor scriptCtor = null;
        
    public Platform(ViNodeSet cloud) {
        this.cloud = cloud;
    }
    
    public Platform() {
        this(CloudFactory.createCloud());
    }
    
    public ViNodeSet cloud() {
        return cloud;
    }
    
    public RemoteNode node(Scope scope) {
        return new RemoteNode(scope);
    }
    
    public RemoteNode node(String pattern) {
        return node(Scopes.pattern(pattern));
    }
    
    public ScenarioBuilder newScenario() {
        ScenarioBuilder result = new ScenarioBuilder(this);
        this.scriptCtor = result;
        return result;
    }

    private HookBuilder onStart(Scope scope, StackTraceElement location) {
        HookBuilder result = new HookBuilder.Startup(this, scope, location);
        this.scriptCtor = result;
        return result;
    }
    
    private HookBuilder onShutdown(Scope scope, StackTraceElement location) {
        HookBuilder result = new HookBuilder.Shutdown(this, scope, location);
        this.scriptCtor = result;
        return result;
    }
    
    public HookBuilder onStart(String pattern) {
        return onStart(Scopes.pattern(pattern), ClassOps.location(1));
    }
    
    public HookBuilder onShutdown(String pattern) {
        return onShutdown(Scopes.pattern(pattern), ClassOps.location(1));
    }
    
    public HookBuilder onStart(Scope scope) {
        return onStart(scope, ClassOps.location(1));
    }
    
    public HookBuilder onShutdown(Scope scope) {
        return onShutdown(scope, ClassOps.location(1));
    }
    
    public class RemoteNode {
        private Scope scope;

        public RemoteNode(Scope scope) {
            this.scope = scope;
        }
        
        public <T> T deploy(T prototype) {
            RemoteBean.Deploy bean = RemoteBean.newDeploy(prototype, scope, ClassOps.location(1));
            execute(bean);
            return bean.getProxy(Platform.this);
        }
        
        public <T> T bean(T bean) {
            throw new UnsupportedOperationException();
        }
    }
    
    protected String[] vinode(Scope scope) {
        List<String> names = new ArrayList<String>();
        
        for (ViNode node : cloud.listNodes("**")) {
            String name = node.toString();
            if (scope.contains(name)) {
                names.add(name);
            }
        }
        
        return names.toArray(new String[names.size()]);
    }
    
    protected void invoke(ScriptAction action, List<String> refs) {
        if (scriptCtor instanceof HookBuilder) {
            scriptCtor.getScriptBuilder().action(action, refs);
        } else if (scriptCtor instanceof ScenarioBuilder) {
            scriptCtor.getScriptBuilder().action(action, refs);
        } else if (action instanceof ViNodeAction){
            execute((ViNodeAction)action);
        } else {
            throw new IllegalArgumentException();
        }
    }
            
    protected void clearScriptConstructor() {
        scriptCtor = null;
    }
    
    private void execute(ViNodeAction action) {
        cloud.nodes(vinode(action.getScope())).exec(action.getExecutor());
    }
}
