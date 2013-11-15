package org.gridkit.lab.orchestration;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.gridkit.lab.orchestration.script.Checkpoint;
import org.gridkit.lab.orchestration.script.Script;
import org.gridkit.lab.orchestration.script.ScriptAction;
import org.gridkit.lab.orchestration.script.ScriptExecutor;
import org.gridkit.lab.orchestration.script.ScriptExecutor.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scenario {
    private static final Logger log = LoggerFactory.getLogger(Scenario.class);
    
    private final Script script;
    private final Platform platform;
    
    public Scenario(Script script, Platform platform) {
        this.script = script;
        this.platform = platform;
    }
    
    public void play() {
        script.execute(new ScenarioExecutor());
    }
    
    public void play(long millis) {
        script.execute(new ScenarioExecutor(), millis, TimeUnit.MILLISECONDS);
    }
    
    public void play(long timeout, TimeUnit unit) {
        script.execute(new ScenarioExecutor(), timeout, unit);
    }

    private class ScenarioExecutor implements ScriptExecutor {
        @Override
        public void execute(ScriptAction action, Box box) {
            if (action instanceof Checkpoint) {
                execute((Checkpoint)action, box);
            } else if (action instanceof ExecutableAction) {
                execute((ExecutableAction)action, box);
            } else if (action instanceof ViNodeAction) {
                execute((ViNodeAction)action, box);
            } else {
                box.failure(new IllegalArgumentException("unknown action class " + action.getClass()));
            }
        }

        private void execute(Checkpoint checkpoint, Box box) {
            log.info("Checkpoint [" + checkpoint.getName() + "] reached");
            box.success();
        }
        
        private void execute(ExecutableAction action, Box box) {
            action.execute(log(action, box));
        }
        
        private void execute(ViNodeAction action, Box box) {
            String[] nodes = platform.vinode(action.getScope());

            if (nodes.length == 0) {
                log.info("No nodes found for execution of " + action + " on scope " + action.getScope());
                box.success();
            } else {
                RemoteBox remoteBox = new ViNodeBox(log(action, box), nodes.length);
                Runnable executor = new ViNodeActionExecutor(action.getExecutor(), remoteBox);
                platform.cloud().nodes(nodes).submit(executor);
            }
        }

        private LoggingBox log(ScriptAction action, Box box) {
            return new LoggingBox(box, action);
        }
    }
    
    private interface RemoteBox extends Remote, Box {}
    
    private static class ViNodeActionExecutor implements Runnable, Serializable {
        private static final long serialVersionUID = 8398932022328525241L;
        
        private Callable<Void> delegate;
        private RemoteBox box;
        
        public ViNodeActionExecutor(Callable<Void> delegate, RemoteBox box) {
            this.delegate = delegate;
            this.box = box;
        }

        @Override
        public void run() {
            try {
                delegate.call();
            } catch (Exception e) {
                box.failure(e);
                return;
            }
            box.success();
        }
    }
    
    private static class LoggingBox implements Box {
        private Box delegate;
        private ScriptAction target;
        
        public LoggingBox(Box delegate, ScriptAction target) {
            this.delegate = delegate;
            this.target = target;
            log.info("About to execute " + target);
        }
        
        @Override
        public void success() {
            log.info("Execution of " + target + " finished successfully");
            delegate.success();
        }

        @Override
        public void failure(Throwable e) {
            log.error("Execution of " + target + " finished with exception - " + e);
            delegate.failure(e);
        }
    }
    
    private static class ViNodeBox implements RemoteBox {
        private Box delegate;
        private int counter;

        public ViNodeBox(Box delegate, int counter) {
            this.delegate = delegate;
            this.counter = counter;
        }
        
        @Override
        public synchronized void success() {
            counter -= 1;
            if (counter == 0 && delegate != null) {
                delegate.success();
                delegate = null;
            }
        }

        @Override
        public synchronized void failure(Throwable e) {
            if (delegate != null) {
                delegate.failure(e);
                delegate = null;
            }
        }
    }
}
