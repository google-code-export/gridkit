package org.gridkit.lab.mcube;

public interface Cube {

	Cube unify(Unification unification);
	
	Cube filter(Filter filter);
	
	Aggregateable groupBy(Value... attributues);
	
	Value aggregate(Aggregation agg);
	
}
