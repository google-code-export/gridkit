package org.gridkit.lab.orchestration.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Script implements Serializable {
    private static final long serialVersionUID = 9125786087888501718L;
    
    private final ScriptGraph graph;

    public Script(ScriptGraph graph) {
        this.graph = graph;
    }

    public void execute(ScriptExecutor executor) {
        new Execution(executor).execute();
    }
    
    public void execute(ScriptExecutor executor, long timeout, TimeUnit unit) throws TimeoutException {
        throw new UnsupportedOperationException();
    }
    
    private class Execution {
        private Set<Object> nextSteps;
        private Set<Object> prevSteps;
        private BlockingQueue<ActionBox> queue;
        private ScriptExecutor executor;
        
        public Execution(ScriptExecutor executor) {
            this.nextSteps = graph.getActionIds();
            this.prevSteps = new HashSet<Object>();
            this.queue = new LinkedBlockingQueue<ActionBox>();
            this.executor = executor;
        }
        
        public void execute() {
            while (!nextSteps.isEmpty()) {
                for (Object actionId : nextStep()) {
                    start(actionId);
                }

                ActionBox doneBox;
                try {
                    doneBox = queue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                
                if (doneBox.isError()) {
                    throw new RuntimeException(doneBox.getError());
                } else {
                    finish(doneBox.getAction());
                }
            }
        }
        
        private void start(Object actionId) {
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
        
        private Collection<Object> nextStep() {
            Collection<Object> result = new ArrayList<Object>();
            
            for (Object actionId : nextSteps) {
                Set<Object> deps = graph.getDependencies(actionId);
                
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
