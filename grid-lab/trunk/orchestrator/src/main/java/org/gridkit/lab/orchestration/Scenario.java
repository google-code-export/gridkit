package org.gridkit.lab.orchestration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.gridkit.lab.orchestration.script.Checkpoint;
import org.gridkit.lab.orchestration.script.Script;
import org.gridkit.lab.orchestration.script.ScriptAction;
import org.gridkit.lab.orchestration.script.ScriptBuilder;
import org.gridkit.lab.orchestration.script.ScriptExecutor;
import org.gridkit.lab.orchestration.script.ScriptExecutor.Box;
import org.gridkit.lab.orchestration.util.NamedThreadFactory;
import org.gridkit.lab.orchestration.util.ViOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scenario {
    private static ScheduledExecutorService LOG_DEMON_EXECUTOR = Executors.newScheduledThreadPool(
        1, new NamedThreadFactory("Scenario-Log-Demon-Executor", true)
    );
    
    private static final Logger log = LoggerFactory.getLogger(Scenario.class);
    
    private Script script;
    private Platform platform;
    private ScenarioState state;
    private long logDelayMs;
    private Map<String, String> captions;
    
    public Scenario(Script script, Platform platform, Map<String, String> captions) {
        this.script = script;
        this.platform = platform;
        this.state = new ScenarioState();
        this.logDelayMs = 0;
        this.captions = captions;
    }
    
    public void play(long timeout, TimeUnit unit) {
        ScheduledFuture<?> future = null;
        if (logDelayMs > 0) {
            future = LOG_DEMON_EXECUTOR.scheduleWithFixedDelay(
                new LogDemon(), logDelayMs, logDelayMs, TimeUnit.MILLISECONDS
            );
        }
        
        try {
            script.execute(new ScenarioExecutor(), timeout, unit);
        } catch (RuntimeException e) {
            log.error("Scenario execution finished with exception", e);
            throw e;
        } finally {
            if (future != null) {
                future.cancel(false);
            }
        }
    }
    
    public Scenario logDelay(long delay, TimeUnit unit) {
        this.logDelayMs = unit.toMillis(delay);
        return this;
    }
    
    public void play() {
        play(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    
    public void play(long seconds) {
        play(seconds, TimeUnit.SECONDS);
    }

    public Scenario logDelay(long seconds) {
        return logDelay(seconds, TimeUnit.SECONDS);
    }
    
    private class ScenarioExecutor implements ScriptExecutor {
        @Override
        public void execute(ScriptAction action, Box box) {
            if (action instanceof Checkpoint) {
                execute((Checkpoint)action, box);
            } else if (action instanceof ExecutableAction) {
                execute((ExecutableAction)action, box);
            } else if (action instanceof ViAction) {
                execute((ViAction)action, box);
            } else {
                box.failure(new IllegalArgumentException("unknown action class " + action.getClass()));
            }
        }

        private void execute(Checkpoint checkpoint, Box box) {
            if (ScriptBuilder.START.equals(checkpoint.getName())) {
                log.info("About to start scenario execution");
            } else if (ScriptBuilder.FINISH.equals(checkpoint.getName())) {
                log.info("Scenario execution finished successfully");
            } else {
                String name = checkpoint.getName();
                
                if (captions.containsKey(name)) {
                    name = captions.get(name);
                } else {
                    name = "Checkpoint '" + name + "'" ;
                }
                
                if (name != null) {
                    log.info(name + " reached");
                }
            }
                        
            box.success();
        }
        
        private void execute(ExecutableAction action, Box box) {
            log.info("About to execute " + action);
            state.start(action);
            action.execute(wrap(action, box));
        }
        
        private void execute(ViAction action, Box box) {
            String[] nodes = platform.vinode(action.getScope());

            if (nodes.length == 0) {
                log.info("No nodes found for execution of " + action + " on scope " + action.getScope());
                box.success();
            } else {
                log.info("About to execute " + action + " on " + Arrays.asList(nodes));
                state.start(action, nodes);
                ScenarioObserver observer = new ScenarioObserver(action, wrap(action, box));
                ViOps.execute(platform.cloud(), nodes, action.getExecutor(), observer);
            }
        }

        private Box wrap(ScriptAction action, Box box) {
            return new LogBox(new StateBox(box, action), action);
        }
    }
    
    private class ScenarioObserver implements ViOps.ExecutionObserver<Void> {
        private ScriptAction action;
        private Box box;
        private boolean error;
        
        public ScenarioObserver(ScriptAction action, Box box) {
            this.action = action;
            this.box = box;
            this.error = false;
        }

        @Override
        public void completed(String node, Void result) {
            state.finish(action.getId(), node);
        }

        @Override
        public void failed(String node, Throwable e) {
            state.finish(action.getId(), node);
            if (!error) {
                error = true;
                box.failure(e);
            }
        }

        @Override
        public void left(String node) {
            failed(node, new RuntimeException("node '" + node + "' left"));
        }

        @Override
        public void completed() {
            if (!error) {
                box.success();
            }
        }
    }
    
    private static class ScenarioState {
        private Map<String, ActionState> states = new LinkedHashMap<String, ActionState>();
        
        public synchronized void start(ScriptAction action) {
            states.put(action.getId(), new ActionState(action, Collections.<String>emptySet()));
        }
        
        public synchronized void start(ViAction action, String[] nodes) {
            states.put(action.getId(), new ActionState(action, new HashSet<String>(Arrays.asList(nodes))));
        }
        
        public synchronized void finish(String id) {
            states.remove(id);
        }
        
        public synchronized void finish(String id, String node) {
            ActionState state = states.get(id);
            if (state != null) {
                state.finish(node);
            }
        }
        
        public synchronized Collection<ActionState> snapshot() {
            Collection<ActionState> result = new ArrayList<ActionState>(states.size());
            for (ActionState state : states.values()) {
                result.add(state.clone());
            }
            return result;
        }
    }
    
    private static class ActionState {
        private ScriptAction action;
        private Set<String> nodes;
        
        public ActionState(ScriptAction action, Set<String> nodes) {
            this.action = action;
            this.nodes = nodes;
        }
        
        public void finish(String node) {
            nodes.remove(node);
        }
        
        public String toString() {
            if (nodes.isEmpty()) {
                return action.toString(); 
            } else {
                return action + " on " + nodes;
            }
        }
        
        public ActionState clone() {
            return new ActionState(action, new HashSet<String>(nodes));
        }
    }

    private static class LogBox implements Box {
        private Box delegate;
        private ScriptAction action;
        
        public LogBox(Box delegate, ScriptAction action) {
            this.delegate = delegate;
            this.action = action;
        }
        
        @Override
        public void success() {
            log.info("Execution of " + action + " finished successfully");
            delegate.success();
        }

        @Override
        public void failure(Throwable e) {
            log.error("Execution of " + action + " finished with exception - " + e);
            delegate.failure(e);
        }
    }
    
    private class StateBox implements Box {
        private Box delegate;
        private ScriptAction action;
        
        public StateBox(Box delegate, ScriptAction action) {
            this.delegate = delegate;
            this.action = action;
        }

        @Override
        public void success() {
            state.finish(action.getId());
            delegate.success();
        }

        @Override
        public void failure(Throwable e) {
            state.finish(action.getId());
            delegate.failure(e);
        }
    }
    
    private class LogDemon implements Runnable {
        @Override
        public void run() {   
            Collection<ActionState> states = state.snapshot();

            if (states.isEmpty()) {
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            
            sb.append("Currently in-progress actions:\n\n");
            for (ActionState state : states) {
                sb.append("\t" + state + "\n");
            }
            sb.append("\n");
            
            log.info(sb.toString());
        }
    }
}
