package org.gridkit.lab.orchestration;

import java.util.ArrayList;
import java.util.List;

import org.gridkit.lab.orchestration.script.ScriptBuilder;
import org.gridkit.lab.orchestration.util.ClassOps;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViNodeSet;
import org.gridkit.vicluster.ViProps;

public class Platform {
    
    protected interface ScriptConstructor {
        ScriptBuilder getScriptBuilder();
        
        Scope getScope();
    }
        
    private final ViNodeSet cloud;
    
    private ScriptConstructor constructor = null;
        
    public Platform(ViNodeSet cloud) {
        this.cloud = cloud;
    }
    
    public Platform() {
        this(CloudFactory.createCloud());
        ViProps.at(cloud.node("**")).setIsolateType();
    }
    
    public ViNodeSet cloud() {
        return cloud;
    }
    
    public RemoteOps at(Scope scope) {
        return new RemoteOps(scope);
    }
    
    public RemoteOps at(String pattern) {
        return at(Scopes.pattern(pattern));
    }
    
    public ScenarioBuilder newScenario() {
        ScenarioBuilder result = new ScenarioBuilder(this);
        this.constructor = result;
        return result;
    }

    private HookBuilder onStart(Scope scope, StackTraceElement location) {
        HookBuilder result = new HookBuilder.Startup(this, scope, location);
        this.constructor = result;
        return result;
    }
    
    private HookBuilder onShutdown(Scope scope, StackTraceElement location) {
        HookBuilder result = new HookBuilder.Shutdown(this, scope, location);
        this.constructor = result;
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
    
    public HookBuilder onStart() {
        return onStart(Scopes.any(), ClassOps.location(1));
    }
    
    public HookBuilder onShutdown() {
        return onShutdown(Scopes.any(), ClassOps.location(1));
    }
    
    public class RemoteOps {
        private Scope scope;

        public RemoteOps(Scope scope) {
            this.scope = scope;
        }
        
        public <T> T deploy(T prototype) {
            RemoteBean.Deploy bean = RemoteBean.newDeploy(prototype, scope, ClassOps.location(1));
            execute(bean);
            return bean.<T>getProxy(Platform.this);
        }
        
        public <T> T bean(T bean) {
            RemoteBean.ProxyHandler handler = BeanProxy.getHandler(
                bean, RemoteBean.ProxyHandler.class
            );
            
            if (handler != null) {
                return handler.getBean().onScope(scope).getProxy(Platform.this);
            } else {
                throw new IllegalArgumentException();
            }
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
        
    public ScriptConstructor getScriptConstructor() {
        return constructor;
    }
    
    protected void clearScriptConstructor() {
        constructor = null;
    }
    
    protected void execute(ViAction action) {
        cloud.nodes(vinode(action.getScope())).exec(action.getExecutor());
    }
}
