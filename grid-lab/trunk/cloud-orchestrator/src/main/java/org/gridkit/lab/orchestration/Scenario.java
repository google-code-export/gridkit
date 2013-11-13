package org.gridkit.lab.orchestration;

import java.io.Serializable;
import java.rmi.Remote;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    
    public void play(long timeout, TimeUnit unit) throws TimeoutException {
        script.execute(new ScenarioExecutor(), timeout, unit);
    }

    private class ScenarioExecutor implements ScriptExecutor {
        @Override
        public void execute(ScriptAction rawAction, Box box) {
            if (rawAction instanceof Checkpoint) {
                Checkpoint action = (Checkpoint)rawAction;
                log.info("Checkpoint '" + action.getName() + "' reached");
                box.success();
            } else if (rawAction instanceof ExecutableAction) {
                ExecutableAction action = (ExecutableAction)rawAction;
                action.execute(box);
            } else {
                SourceAction source = (SourceAction)rawAction;
                ViNodeAction viAction = (ViNodeAction)rawAction;
                
                String[] nodes = platform.vinode(viAction.getScope());

                String logTarget = source.getSource() + " at " + ClassOps.location(source.getLocation());

                if (nodes.length == 0) {
                    log.info("No nodes found for execution of " + logTarget);
                    box.success();
                } else {
                    String aboutMsg   = "About to execute " + logTarget;
                    String successMsg = "Execution of " + logTarget + " finished successfully";
                    String failureMsg = "Execution of " + logTarget + " finished with error";

                    log.info(aboutMsg);
                    RemoteBox remoteBox = new LoggingRemoteBox(box, nodes.length, successMsg, failureMsg);
                    Runnable executor = new ViNodeActionExecutor(viAction.getExecutor(), remoteBox);
                    platform.cloud().nodes(nodes).submit(executor);
                }
            }
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
    
    private static class LoggingRemoteBox implements RemoteBox {
        private Box delegate;
        private int counter;
        private String successMsg;
        private String failureMsg;

        public LoggingRemoteBox(Box delegate, int counter, String successMsg, String failureMsg) {
            this.delegate = delegate;
            this.counter = counter;
            this.successMsg = successMsg;
            this.failureMsg = failureMsg;
        }
        
        @Override
        public synchronized void success() {
            counter -= 1;
            if (counter == 0 && delegate != null) {
                delegate.success();
                delegate = null;
                if (successMsg != null) {
                    log.info(successMsg);
                }
            }
        }

        @Override
        public synchronized void failure(Throwable e) {
            if (delegate != null) {
                delegate.failure(e);
                delegate = null;
                if (failureMsg != null) {
                    log.info(failureMsg);
                }
            }
        }
    }
}
