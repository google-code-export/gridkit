package org.gridkit.lab.mcube;

public class CommonCube implements Cube {

	@Override
	public Cube unify(Unification unification) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Cube filter(Filter filter) {
		return null;
	}

	@Override
	public Aggregateable groupBy(Value... attributues) {
		return null;
	}

	@Override
	public Value aggregate(Aggregation agg) {
		return null;
	}
}
