package org.gridkit.lab.orchestration.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ScriptBuilder {
    public static final String START  = "start";
    public static final String FINISH = "finish";
  
    private Map<String, Checkpoint> checkpoints = new HashMap<String, Checkpoint>();
    private Map<Object, Creation> creations = new HashMap<Object, Creation>();
    
    private List<Script.Edge> edges = new ArrayList<Script.Edge>();
        
    private Checkpoint checkpoint = getCheckpoint(START);
    private List<ScriptAction> section = new ArrayList<ScriptAction>();

    public Script build() {
        joinFinish();
        Script result = new Script(edges);
        edges = null;
        return result;
    }
    
    public Checkpoint from(String name) {
        if (!section.isEmpty()) {
            throw new IllegalStateException("not empty section, call join first");
        }
        checkpoint = getCheckpoint(name);
        return checkpoint;
    }
    
    public Checkpoint fromStart() {
        return from(START);
    }
    
    public Checkpoint join(String name) {
        Checkpoint joinCheckpoint = getCheckpoint(name);
        
        Iterator<ScriptAction> sectionIter = section.iterator();
        while (sectionIter.hasNext()) {
            edge(sectionIter.next(), joinCheckpoint);
            sectionIter.remove();
        }
        
        from(name);
        return joinCheckpoint;
    }
    
    public Checkpoint joinFinish() {
        return join(FINISH);
    }

    public void create(ScriptBean bean, List<Object> refs) {
        Creation creation = new Creation();
        creation.setBean(bean);
        creations.put(bean.getRef(), creation);
        
        for (Object ref : refs) {
            reference(creation, ref);
        }
        
        edge(checkpoint, creation);
        action(creation);
    }
    
    private void action(ScriptAction action) {
        section.add(action);
    }
    
    private void reference(ScriptAction action, Object ref) {
        Creation creation = creations.get(ref);
        if (creation != null) {                    
            edge(creation, action);
        }
    }
    
    private void edge(ScriptAction from, ScriptAction to) {
        Script.Edge edge = new Script.Edge();
        edge.setFrom(from);
        edge.setTo(to);
        edges.add(edge);
    }
    
    protected Checkpoint getCheckpoint(String name) {
        if (!checkpoints.containsKey(name)) {
            Checkpoint checkpoint = new Checkpoint();
            checkpoint.setName(name);
            checkpoints.put(name, checkpoint);
        }
        return checkpoints.get(name);
    }
}
