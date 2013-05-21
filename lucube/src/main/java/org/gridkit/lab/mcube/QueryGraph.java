package org.gridkit.lab.mcube;

import java.util.Collection;

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
		
	}
	
	interface FilterNode extends RowSource {
	
		Criterion getCriterion();
		
		RowSource getSource();
		
	}
	
	interface VirtualRowSource extends RowSource {
		
		public Collection<Attribute> getAttributes();
		
	}
	
	interface Mapper extends VirtualRowSource {
		
		public RowSource getSource();
		
		public Collection<Attribute> getGroupAttributes();
		
		public RowSource getGroupSourcePrototype();
		
	}
	
	interface AggregationElement extends Attribute {

		public RowSource getAggregationSource();
		
		public Reducer getReducer();
		
	}
	
	interface Reducer {
		
	}
	
	interface AttributeCriterion {
		
		public Attribute getAttribute();
		
		public Transform getValuePredicate();
		
	}

	interface SampleTypeCriterion {
		
		public 
		
	}
	
	interface SampleAttribute {
		
		public String getName();
		
		public Class<?> getType();
		
	}
	
	interface Transform {
		
		public Object transfrom(Object value);
		
	}
}
