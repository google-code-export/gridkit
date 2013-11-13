package org.gridkit.lab.orchestration.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

// TODO to do start and end added incorrectly (the should always be first and last actions)
public class ScriptBuilder {
    public static final String START  = "start";
    public static final String FINISH = "finish";
      
    private ScriptGraph graph = new ScriptGraph(); {
        graph.add(newCheckpoint(START), newCheckpoint(FINISH));
    }
    
    private Checkpoint checkpoint = newCheckpoint(START);
    private List<ScriptAction> section = new ArrayList<ScriptAction>();

    public Script build() {
        joinFinish();
        Script result = new Script(graph);
        graph = null;
        return result;
    }
    
    public Checkpoint from(String name) {
        if (!section.isEmpty()) {
            throw new IllegalStateException("not empty section, call join first");
        }
        checkpoint = newCheckpoint(name);
        return checkpoint;
    }
    
    public Checkpoint fromStart() {
        return from(START);
    }
    
    public Checkpoint join(String name) {
        Checkpoint joinCheckpoint = newCheckpoint(name);
        
        Iterator<ScriptAction> sectionIter = section.iterator();
        while (sectionIter.hasNext()) {
            graph.add(sectionIter.next(), joinCheckpoint);
            sectionIter.remove();
        }
        
        from(name);
        return joinCheckpoint;
    }
    
    public Checkpoint joinFinish() {
        return join(FINISH);
    }

    public void action(ScriptAction action, List<Object> refs) {        
        for (Object ref : refs) {
            graph.add(ref, action);
        }
        graph.add(checkpoint, action);
        section.add(action);
    }
    
    public void action(ScriptAction action) {
        action(action, Collections.emptyList());
    }
    
    private static Checkpoint newCheckpoint(String name) {
        return new Checkpoint(name);
    }
}
