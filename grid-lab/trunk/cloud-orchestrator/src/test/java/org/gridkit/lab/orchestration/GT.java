package org.gridkit.lab.orchestration;

import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;

public class GT {
    public static void main(String[] args) {
        DirectedAcyclicGraph<String, DefaultEdge> dag = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge.class);
        
        dag.addVertex("a");
        dag.addVertex("b");
        dag.addVertex("c");
        
        dag.addEdge("a", "b");
        dag.addEdge("b", "c");
        dag.addEdge("a", "c");
                
        TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(dag);
                
        System.err.println(dag);
    }
}
