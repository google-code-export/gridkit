package org.gridkit.lab.mcube;

public interface Aggregation {
	
	public boolean isAtomic();
	
	public Value rebuild(Aggregateable dataSet);
	
	public AdditiveReducer getReducer();

}
