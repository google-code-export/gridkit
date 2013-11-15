package org.gridkit.lab.orchestration;

import java.io.Serializable;
import java.util.UUID;

import org.gridkit.lab.orchestration.script.Checkpoint;
import org.gridkit.lab.orchestration.script.Script;
import org.gridkit.lab.orchestration.script.ScriptAction;
import org.gridkit.lab.orchestration.script.ScriptBuilder;
import org.gridkit.lab.orchestration.script.ScriptExecutor;
import org.gridkit.lab.orchestration.util.ClassOps;
import org.gridkit.vicluster.ViNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HookBuilder implements Platform.ScriptConstructor {    
    private static final Logger log = LoggerFactory.getLogger(HookBuilder.class);
    
    private Platform platform;
    private Scope scope;
    private StackTraceElement location;
    private ScriptBuilder builder;

    public HookBuilder(Platform platform, Scope scope, StackTraceElement location) {
        this.platform = platform;
        this.scope = scope;
        this.location = location;
        this.builder = new ScriptBuilder();
        this.builder.seq();
    }

    public <T> T deploy(T prototype) {
        RemoteBean.Deploy bean = RemoteBean.newDeploy(prototype, scope, ClassOps.location(1));
        builder.action(bean);
        return bean.getProxy(platform);
    }

    public void build() {
        ScriptHook executor = new ScriptHook(builder.build(), scope, location);
        platform.clearScriptConstructor();
        builder = null;
        addHook(platform.cloud().node("**"), UUID.randomUUID().toString(), executor);
    }
    
    protected abstract void addHook(ViNode node, String name, Runnable hook);
    
    public ScriptBuilder getScriptBuilder() {
        return builder;
    }
    
    protected static class Startup extends HookBuilder {
        public Startup(Platform platform, Scope scope, StackTraceElement location) {
            super(platform, scope, location);
        }

        @Override
        protected void addHook(ViNode node, String name, Runnable hook) {
            node.addStartupHook(name, hook, false);
        }
    }
    
    protected static class Shutdown extends HookBuilder {
        public Shutdown(Platform platform, Scope scope, StackTraceElement location) {
            super(platform, scope, location);
        }

        @Override
        protected void addHook(ViNode node, String name, Runnable hook) {
            node.addShutdownHook(name, hook, false);
        }
    }
    
    private static class ScriptHook implements Runnable, Serializable {
        private static final long serialVersionUID = -1392214150235120525L;
        
        private final Script script;
        private final Scope scope;
        private final String location;
        
        public ScriptHook(Script script, Scope scope, StackTraceElement location) {
            this.script = script;
            this.scope = scope;
            this.location = ClassOps.toString(location);
        }

        @Override
        public void run() {
            if (isScopeNode(scope)) {
                log.info("About to execute hook at " + location);
                try {
                    script.execute(new HookExecutor());
                } catch (RuntimeException e) {
                    log.error("Execution of hook at " + location + " finished with exception - " + e);
                    throw e;
                }
                log.info("Execution of hook at " + location + " finished successfully");
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
        public void execute(ScriptAction action, Box box) {
            if (action instanceof ViNodeAction) {
                execute((ViNodeAction)action, box);
            } else if (action instanceof Checkpoint) {
                execute((Checkpoint)action, box);
            } else {
                box.failure(new IllegalArgumentException("unknown action class " + action.getClass()));
            }
        }
        
        private void execute(ViNodeAction action, Box box) {
            try {
                action.getExecutor().call();
            } catch (Exception e) {
                box.failure(e);
                return;
            }
            box.success();
        }

        private void execute(Checkpoint checkpoint, Box box) {
            if (ScriptBuilder.START.equals(checkpoint.getName()) || ScriptBuilder.FINISH.equals(checkpoint.getName())) {
                box.success();
            } else {
                box.failure(new IllegalArgumentException("checkpoint [" + checkpoint.getName() + "] in hook script"));
            }
        }
    }
}
