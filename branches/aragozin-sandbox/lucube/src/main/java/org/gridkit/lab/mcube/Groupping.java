package org.gridkit.lab.mcube;

import java.util.Collection;
import java.util.Map;

public interface Groupping {

	public Collection<Group> allGroups();
	
	public Group group(Object... tuple);
	
	public Group group(Map<Value, Object> tuple);
	
	public Value calculate(Aggregation aggregation);
	
	public interface Group {
		
		public Map<Value, Object> getTuple();
		
		public Object[] getTupleAsArray();
		
		public Filter getFilter();
		
	}	
}
