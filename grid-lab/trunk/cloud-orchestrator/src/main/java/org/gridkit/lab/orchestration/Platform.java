package org.gridkit.lab.orchestration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.gridkit.lab.orchestration.BeanProxy.Argument;
import org.gridkit.lab.orchestration.BeanRegistry.BeanRef;
import org.gridkit.lab.orchestration.script.ScriptAction;
import org.gridkit.lab.orchestration.script.ScriptBuilder;
import org.gridkit.nanocloud.CloudFactory;
import org.gridkit.vicluster.ViNode;
import org.gridkit.vicluster.ViNodeSet;

public class Platform {
    
    protected interface ScriptConstructor {
        ScriptBuilder getScriptBuilder();
    }
    
    private static AtomicInteger nextPlatformId = new AtomicInteger(0);
    
    private final ViNodeSet cloud;
    
    private ScriptConstructor scriptCtor = null;
    
    private int id = nextPlatformId.getAndIncrement();
    private int nextBeanId = 0;
    private int nextStartupHook = 0;
    private int nextShutdownHook = 0;
    
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

    public HookBuilder onStart(Scope scope) {
        String name = "platform-startup-" + id + "-" + (nextStartupHook++);
        HookBuilder result = new HookBuilder.Startup(this, scope, name);
        this.scriptCtor = result;
        return result;
    }
    
    public HookBuilder onShutdown(Scope scope) {
        String name = "platform-shutdown-" + id + "-" + (nextShutdownHook++);
        HookBuilder result = new HookBuilder.Shutdown(this, scope, name);
        this.scriptCtor = result;
        return result;
    }
    
    public HookBuilder onStart(String pattern) {
        return onStart(Scopes.pattern(pattern));
    }
    
    public HookBuilder onShutdown(String pattern) {
        return onShutdown(Scopes.pattern(pattern));
    }
    
    public class RemoteNode {
        private Scope scope;

        public RemoteNode(Scope scope) {
            this.scope = scope;
        }
        
        public <T> T deploy(T prototype) {
            RemoteBean.Deploy bean = newRemoteBean(scope, prototype, ClassOps.stackTraceElement(1));
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
    
    protected void invoke(ScriptAction action, List<Object> refs) {
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
    
    protected RemoteBean.Deploy newRemoteBean(Scope scope, Object prototype, StackTraceElement createPoint) {
        return new RemoteBean.Deploy(
            prototype, nextBeanRef(), scope, createPoint
        );
    }
    
    protected RemoteBean.Invoke newRemoteBean(Scope scope, RemoteBean target,
                                              Method method, List<Argument> args,
                                              StackTraceElement createPoint) {
        return new RemoteBean.Invoke(
            nextBeanRef(), scope, target, method, args, createPoint
        );
    }
        
    private BeanRef nextBeanRef() {
        return new BeanRef("" + (nextBeanId++) + "-" + id);
    }
    
    private void execute(ViNodeAction action) {
        cloud.nodes(vinode(action.getScope())).exec(action.getExecutor());
    }
}
