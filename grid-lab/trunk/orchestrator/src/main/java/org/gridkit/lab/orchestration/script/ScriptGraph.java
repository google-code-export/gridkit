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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptGraph implements Serializable {
    private static final long serialVersionUID = -4542896668675094215L;
    
    private static final Logger log = LoggerFactory.getLogger(ScriptGraph.class);
    
    private Map<String, ScriptAction> actions = new HashMap<String, ScriptAction>();
    private DirectedAcyclicGraph<String, Edge> dag = new DirectedAcyclicGraph<String, Edge>(Edge.class);

    public ScriptAction getAction(String id) {
        return actions.get(id);
    }
    
    public boolean hasAction(String id) {
        return actions.containsKey(id);
    }
    
    public Set<String> getIds() {
        return new HashSet<String>(actions.keySet());
    }
    
    public Set<String> getDeps(String id) {
        Set<String> result = new HashSet<String>();
        
        for (Edge edge : dag.incomingEdgesOf(id)) {
            result.add(edge.getFrom());
        }
        
        return result;
    }
    
    public void action(ScriptAction action) {
        if (!actions.containsKey(action.getId())) {
            actions.put(action.getId(), action);
            dag.addVertex(action.getId());
        }
    }
    
    public void edge(ScriptAction from, ScriptAction to){
        action(from);
        action(to);
        edge(from.getId(), to.getId());
    }
        
    private void edge(String from, String to)  {
        try {
            dag.addDagEdge(from, to);
        } catch (CycleFoundException e) {
            findCycle(from, to);
        }
    }
        
    private void findCycle(String from, String to) {
        List<Edge> path = DijkstraShortestPath.findPathBetween(dag, to, from);
        
        List<ScriptAction> cycle = new ArrayList<ScriptAction>();
        for (Edge edge : path) {
            cycle.add(actions.get(edge.getFrom()));
        }
        cycle.add(actions.get(from));
        cycle.add(actions.get(to));
        
        throwCycle(cycle);
    }
    
    private static void throwCycle(List<ScriptAction> cycle) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Cycle detected during script construction:\n\n");
        for (ScriptAction action : cycle) {
            sb.append("\t" + action + "\n");
        }
        sb.append("\n");
        
        log.error(sb.toString());
        
        throw new CycleDetectedException(cycle);
    }
    
    public static class Edge extends DefaultEdge {
        private static final long serialVersionUID = 3247486556306863355L;

        public String getFrom() {
            return (String)super.getSource();
        }
        
        public String getTo() {
            return (String)super.getTarget();
        }
    }
}
