package org.gridkit.lab.orchestration.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph.CycleFoundException;
import org.jgrapht.graph.DefaultEdge;

public class ScriptGraph implements Serializable {
    private static final long serialVersionUID = -4542896668675094215L;
    
    private Map<Object, ScriptAction> actions = new HashMap<Object, ScriptAction>();
    private DirectedAcyclicGraph<Object, Edge> dag = new DirectedAcyclicGraph<Object, Edge>(Edge.class);

    public ScriptAction getAction(Object actionId) {
        return actions.get(actionId);
    }
    
    public Set<Object> getActionIds() {
        return new HashSet<Object>(actions.keySet());
    }
    
    public Set<Object> getDependencies(Object actionId) {
        Set<Object> result = new HashSet<Object>();
        
        for (Edge edge : dag.incomingEdgesOf(actionId)) {
            result.add(edge.getFrom());
        }
        
        return result;
    }
    
    public void add(ScriptAction from, ScriptAction to){
        add(from);
        add(from.getId(), to);
    }
        
    public void add(Object from, ScriptAction to)  {
        add(to);
        
        if (actions.containsKey(from)) {
            try {
                dag.addDagEdge(from, to.getId());
            } catch (CycleFoundException e) {
                findCycle(from, to);
            }
        }
    }
    
    public void add(ScriptAction action) {
        if (!actions.containsKey(action.getId())) {
            actions.put(action.getId(), action);
            dag.addVertex(action.getId());
        }
    }
        
    private void findCycle(Object from, ScriptAction to) {
        List<Edge> path = DijkstraShortestPath.findPathBetween(dag, to.getId(), from);
        
        List<ScriptAction> cycle = new ArrayList<ScriptAction>();
        for (Edge edge : path) {
            cycle.add(actions.get(edge.getFrom()));
        }
        cycle.add(actions.get(from));
        cycle.add(to);
        
        throw new CycleDetectedException(cycle);
    }
    
    public static class Edge extends DefaultEdge {
        private static final long serialVersionUID = 3247486556306863355L;

        public Object getFrom() {
            return super.getSource();
        }
        
        public Object getTo() {
            return super.getTarget();
        }
        
        @Override
        public String toString() {
            return super.toString();
        }
    }
}
