package org.gridkit.lab.orchestration.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Script implements Serializable {
    private static final long serialVersionUID = 9125786087888501718L;
    
    private final ScriptGraph graph;

    public Script(ScriptGraph graph) {
        this.graph = graph;
    }

    public void execute(ScriptExecutor executor) {
        new Execution(executor, Long.MAX_VALUE).execute();
    }
    
    public void execute(ScriptExecutor executor, long timeout, TimeUnit unit) {
        long timeoutStamp = Math.max(Long.MAX_VALUE, System.currentTimeMillis() + unit.toMillis(timeout));
        new Execution(executor, timeoutStamp).execute();
    }
    
    private class Execution {
        private Set<String> nextSteps;
        private Set<String> prevSteps;
        private BlockingQueue<ActionBox> queue;
        private ScriptExecutor executor;
        private long timeoutStamp;
        
        public Execution(ScriptExecutor executor, long timeoutStamp) {
            this.nextSteps = graph.getIds();
            this.prevSteps = new HashSet<String>();
            this.queue = new LinkedBlockingQueue<ActionBox>();
            this.executor = executor;
            this.timeoutStamp = timeoutStamp;
        }
        
        public void execute() {
            while (!nextSteps.isEmpty()) {
                for (String actionId : nextStep()) {
                    start(actionId);
                }

                ActionBox doneBox;
                try {
                    long timeout = Math.max(timeoutStamp - System.currentTimeMillis(), 1);
                    doneBox = queue.poll(timeout, TimeUnit.MILLISECONDS);
                    if (doneBox == null) {
                        throw new ScriptExecutionException("script execution timeout");
                    }
                } catch (InterruptedException e) {
                    throw new ScriptExecutionException("script execution interrupted", e);
                }
                
                if (doneBox.isError()) {
                    throw new ScriptExecutionException("script execution error", doneBox.getError());
                } else {
                    finish(doneBox.getAction());
                }
            }
        }
        
        private void start(String actionId) {
            if (!prevSteps.contains(actionId)) {
                ScriptAction action = graph.getAction(actionId);
                ActionBox box = new ActionBox(action, queue);
                executor.execute(action, box);
                prevSteps.add(actionId);
            }
        }
        
        private void finish(ScriptAction action) {
            nextSteps.remove(action.getId());
        }
        
        private Collection<String> nextStep() {
            Collection<String> result = new ArrayList<String>();
            
            for (String actionId : nextSteps) {
                Set<String> deps = graph.getDeps(actionId);
                
                deps.retainAll(nextSteps);
                
                if (deps.isEmpty()) {
                    result.add(actionId);
                }
            }
            
            return result;
        }
    }
    
    private static class ActionBox implements ScriptExecutor.Box {
        private final ScriptAction action;
        private final BlockingQueue<ActionBox> queue;

        private volatile Throwable error = null;
        
        public ActionBox(ScriptAction action, BlockingQueue<ActionBox> queue) {
            this.action = action;
            this.queue = queue;
        }

        @Override
        public void success() {
            queue.add(this);
        }

        @Override
        public void failure(Throwable error) {
            this.error = error;
            queue.add(this);
        }
        
        public ScriptAction getAction() {
            return action;
        }

        public boolean isError() {
            return error != null;
        }
        
        public Throwable getError() {
            return error;
        }
    }
}
