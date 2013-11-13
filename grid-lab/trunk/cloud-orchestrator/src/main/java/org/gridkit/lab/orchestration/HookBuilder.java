package org.gridkit.lab.orchestration;

import java.io.Serializable;

import org.gridkit.lab.orchestration.script.Script;
import org.gridkit.lab.orchestration.script.ScriptAction;
import org.gridkit.lab.orchestration.script.ScriptBuilder;
import org.gridkit.lab.orchestration.script.ScriptExecutor;
import org.gridkit.vicluster.ViNode;

public abstract class HookBuilder implements Platform.ScriptConstructor {    
    private Platform platform;
    private Scope scope;
    private String hookName;
    private ScriptBuilder builder;

    public HookBuilder(Platform platform, Scope scope, String hookName) {
        this.platform = platform;
        this.scope = scope;
        this.hookName = hookName;
        this.builder = new ScriptBuilder();
    }

    public <T> T deploy(T prototype) {
        RemoteBean.Deploy bean = platform.newRemoteBean(scope, prototype, ClassOps.stackTraceElement(1));
        builder.action(bean);
        return bean.getProxy(platform);
    }

    public void build() {
        ScriptHook executor = new ScriptHook(builder.build(), scope);
        platform.clearScriptConstructor();
        builder = null;
        addHook(platform.cloud().node("**"), hookName, executor);
    }
    
    protected abstract void addHook(ViNode node, String name, Runnable hook);
    
    public ScriptBuilder getScriptBuilder() {
        return builder;
    }
    
    protected static class Startup extends HookBuilder {
        public Startup(Platform platform, Scope scope, String hookName) {
            super(platform, scope, hookName);
        }

        @Override
        protected void addHook(ViNode node, String name, Runnable hook) {
            node.addStartupHook(name, hook, true);
        }
    }
    
    protected static class Shutdown extends HookBuilder {
        public Shutdown(Platform platform, Scope scope, String hookName) {
            super(platform, scope, hookName);
        }

        @Override
        protected void addHook(ViNode node, String name, Runnable hook) {
            node.addShutdownHook(name, hook, true);
        }
    }
    
    private static class ScriptHook implements Runnable, Serializable {
        private static final long serialVersionUID = -1392214150235120525L;
        
        private final Script script;
        private final Scope scope;
        
        public ScriptHook(Script script, Scope scope) {
            this.script = script;
            this.scope = scope;
        }

        @Override
        public void run() {
            if (isScopeNode(scope)) {
                script.execute(new HookExecutor());
            }
        }
    }
    
    private static boolean isScopeNode(Scope scope) {
        return scope.contains(getNodeName());
    }
    
    private static String getNodeName() {
        return System.getProperty("vinode.name");
    }
    
    private static class HookExecutor implements ScriptExecutor {
        @Override
        public void execute(ScriptAction rawAction, Box box) {
            if (rawAction instanceof ViNodeAction) {
                ViNodeAction action = (ViNodeAction)rawAction;
                try {
                    action.getExecutor().call();
                } catch (Exception e) {
                    box.failure(e);
                    return;
                }
                box.success();
            } else {
                box.success();
            }
        }
    }
}
