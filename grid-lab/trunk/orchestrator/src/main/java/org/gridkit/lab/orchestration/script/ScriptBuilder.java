package org.gridkit.lab.orchestration.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ScriptBuilder {
    public static final String START  = "start";
    public static final String FINISH = "finish";
    
    private static final Checkpoint START_POINT  = new Checkpoint(START);
    private static final Checkpoint FINISH_POINT = new Checkpoint(FINISH);
    
    private ScriptGraph graph = new ScriptGraph(); {
        graph.edge(START_POINT, FINISH_POINT);
    }
    
    private Checkpoint begin = checkpoint(START);
    private List<ScriptAction> section = new ArrayList<ScriptAction>();
    private boolean seq = false;
    
    public void from(String name) {
        if (!section.isEmpty()) {
            throw new IllegalStateException("not empty section, call join first");
        }
        if (FINISH.equals(name)) {
            throw new IllegalArgumentException("construction from finish isn't allowed");
        }
        begin = checkpoint(name);
    }
    
    public void seq() {
        if (!section.isEmpty()) {
            throw new IllegalStateException("not empty section, call join first");
        }
        seq = true;
    }
    
    public void par() {
        if (!section.isEmpty()) {
            throw new IllegalStateException("not empty section, call join first");
        }
        seq = false;
    }
    
    public void join(String name) {
        if (START.equals(name)) {
            throw new IllegalArgumentException("join to start isn't allowed");
        }
        
        Checkpoint end = checkpoint(name);

        Iterator<ScriptAction> iter = section.iterator();
        while (iter.hasNext()) {
            ScriptAction action = iter.next();
            graph.edge(begin, action);
            graph.edge(action, end);
            iter.remove();
        }
        
        graph.edge(begin, end);
        
        if (FINISH.equals(name)) {
            from(START);
        } else {
            from(name);
        }
    }

    public void action(ScriptAction action, List<String> refs) {        
        for (String ref : refs) {
            if (graph.hasAction(ref)) {
                graph.edge(graph.getAction(ref), action);
            }
        }
        
        if (seq && !section.isEmpty()) {
            graph.edge(section.get(section.size()-1), action);
        }
        
        section.add(action);
    }
    
    public void action(ScriptAction action) {
        action(action, Collections.<String>emptyList());
    }
    
    public Script getScript() {
        return new Script(graph);
    }
    
    public boolean isSectionEmpty() {
        return section.isEmpty();
    }
    
    private Checkpoint checkpoint(String name) {
        if (START.equals(name)) {
            return START_POINT;
        } else if (FINISH.equals(name)) {
            return FINISH_POINT;
        } else {
            Checkpoint checkpoint = new Checkpoint(name);
            graph.edge(START_POINT, checkpoint);
            graph.edge(checkpoint, FINISH_POINT);
            return checkpoint;
        }
    }
}
