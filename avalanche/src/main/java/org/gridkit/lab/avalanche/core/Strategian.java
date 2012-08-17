package org.gridkit.lab.avalanche.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Strategian {

	Graph graph;
	Knot target;
	List<Proposal> proposals = new ArrayList<Strategian.Proposal>();
	List<Conflict> conflicts = new ArrayList<Conflict>();
	
	public Strategian(Graph graph) {
		this.graph = graph;
	}
	
	public Collection<ActionItem> analyze(Graph graph, Knot target) {
		reset();		
		this.target = target;
		buildLiveDependencyGraph();
		List<ActionItem> result = new ArrayList<ActionItem>();
		for(Conflict c: conflicts) {
			result.add(reportConflict(c));
		}
		for(Proposal p: proposals) {
			result.add(reportProposal(p));
		}
		return result;		
	}	
	
	private void buildLiveDependencyGraph() {
		// TODO Auto-generated method stub
		
	}

	private ActionItem reportProposal(Proposal p) {
		return null;
	}

	private ActionItem reportConflict(Conflict c) {
		return null;
	}

	private void reset() {
		target = null;
		proposals.clear();
		conflicts.clear();
		
	}
	
	private static class Proposal {
		
		Edge transtion;
		
	}
	
	private static class Conflict {
		
		Knot goal1;
		Edge
		Knot goal2;
		
	}
	
	private static class Node {
		
		Knot knot;
		List<Edge> incomming = new ArrayList<Edge>();
		List<Edge> outgoing = new ArrayList<Edge>();
		
		public Node(Knot knot) {
			this.knot = knot;
		}
		
	}
	
	private static class Edge {
		
		Knot dependant;
		Knot dependency;
		
	}
}
