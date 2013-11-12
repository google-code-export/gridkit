package org.gridkit.lab.orchestration.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.gridkit.util.concurrent.Box;

public class Script implements Serializable {
    private static final long serialVersionUID = 9125786087888501718L;
    
    private final List<Edge> edges;

    public Script(List<Edge> edges) {
        this.edges = edges;
    }

    public void execute() {
        new Execution().execute();
    }
    
    public void execute(long timeout, TimeUnit unit) throws TimeoutException {
        
    }
    
    public static class Edge implements Serializable {
        private static final long serialVersionUID = -2805529944634989587L;
        
        private ScriptAction from;
        private ScriptAction to;
        
        public ScriptAction getFrom() {
            return from;
        }
        
        public void setFrom(ScriptAction from) {
            this.from = from;
        }
        
        public ScriptAction getTo() {
            return to;
        }
        
        public void setTo(ScriptAction to) {
            this.to = to;
        }
    }
    
    public Set<ScriptAction> getDependencies(ScriptAction action) {
        Set<ScriptAction> result = new HashSet<ScriptAction>();
        
        for (Edge edge : edges) {
            if (edge.getTo().equals(action)) {
                result.add(edge.getFrom());
            }
        }
        
        return result;
    }
    
    public Set<ScriptAction> getActions() {
        Set<ScriptAction> result = new HashSet<ScriptAction>();
        
        for (Edge edge : edges) {
            result.add(edge.getFrom());
            result.add(edge.getTo());
        }
        
        return result;
    }
      
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Script[\n");
        for (Edge edge : edges) {
            sb.append('\t');
            sb.append(edge);
            sb.append('\n');
        }
        sb.append("]");
        return sb.toString();
    }
    
    private class Execution {
        private Set<ScriptAction> nextSteps;
        private Set<ScriptAction> prevSteps;
        private BlockingQueue<ActionBox> queue;
        
        public Execution() {
            this.nextSteps = getActions();
            this.prevSteps = new HashSet<ScriptAction>();
            this.queue = new LinkedBlockingQueue<ActionBox>();
        }
        
        public void execute() {
            while (!nextSteps.isEmpty()) {
                for (ScriptAction action : nextStep()) {
                    start(action);
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
        
        private void start(ScriptAction action) {
            if (!prevSteps.contains(action)) {
                ActionBox box = new ActionBox(action, queue);
                action.execute(box);
                prevSteps.add(action);
            }
        }
        
        private void finish(ScriptAction action) {
            nextSteps.remove(action);
        }
        
        private Collection<ScriptAction> nextStep() {
            Collection<ScriptAction> result = new ArrayList<ScriptAction>();
            
            for (ScriptAction action : nextSteps) {
                Set<ScriptAction> deps = getDependencies(action);
                
                deps.retainAll(nextSteps);
                
                if (deps.isEmpty()) {
                    result.add(action);
                }
            }
            
            return result;
        }
    }
    
    private static class ActionBox implements Box<Void> {
        private final ScriptAction action;
        private final BlockingQueue<ActionBox> queue;

        private volatile Throwable error = null;
        
        public ActionBox(ScriptAction action, BlockingQueue<ActionBox> queue) {
            this.action = action;
            this.queue = queue;
        }

        @Override
        public void setData(Void data) {
            queue.add(this);
        }

        @Override
        public void setError(Throwable error) {
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
