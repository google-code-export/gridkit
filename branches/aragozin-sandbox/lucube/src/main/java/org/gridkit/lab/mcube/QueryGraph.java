package org.gridkit.lab.mcube;

import java.util.Collection;
import java.util.List;
import java.util.Set;

class QueryGraph {
	
	/**
	 * Reference to stored or calculated attribute associated with row.
	 * May be composite.
	 */
	interface Attribute {
		
		public RowSource getSource();
		
	}
	
	interface RowSource {
		
	}
	
	interface Criterion {
		
		public Attribute getAttribute();
		
		public Function getFunction();
		
	}
	
	interface FilterNode extends RowSource {
	
		Criterion getCriterion();
		
		RowSource getSource();
		
	}
	
	interface VirtualRowSource extends RowSource {
		
		public Collection<Attribute> getAttributes();
		
	}
	
	interface MapperSource extends VirtualRowSource {
		
		public RowSource getSource();
		
		public List<Attribute> getRoutingKey();
		
		public VirtualRowSource getGroupSourcePrototype();
		
	}
	
	interface AggregationElement extends Attribute {

		public RowSource getAggregationSource();
		
		public Reducer getReducer();
		
	}
	
	interface Reducer {
		
	}
	
	interface AttributeCriterion {
		
		public Attribute getAttribute();
		
		public Function getValuePredicate();
		
	}

	interface StaticMarkersSource extends RowSource {
		
	}
	
	interface SampleField extends Attribute {
		
		public Set<String> getNamespaces();
		
		public String getName();
		
		public Class<?> getType();
		
	}
	
	interface 
	
	interface Function {
		
		public Object apply(Object value);
		
	}
}
