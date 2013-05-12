package org.gridkit.lab.mcube;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractArithmeticFunction extends BaseValue implements Function, Serializable {

	private static final long serialVersionUID = 20130512L;
	
	protected final List<Value> args;
	
	public AbstractArithmeticFunction(Value... args) {
		this.args = Collections.unmodifiableList(Arrays.asList(args));
	}
	
	@Override
	public Collection<Value> getArguments() {
		return args;
	}

	@Override
	public Object apply(Row row) {
		double[] tuple = new double[args.size()];
		for(int i = 0; i != tuple.length; ++i) {
			Object v = row.get(args.get(i));
			if (v instanceof Number) {
				tuple[i] = ((Number) v).doubleValue();
			}
		}
		return apply(tuple);
	}

	protected abstract Object apply(double[] values);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((args == null) ? 0 : args.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractArithmeticFunction other = (AbstractArithmeticFunction) obj;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		return true;
	}
	
	@Override
	public String toString() {		
		return getClass().getSimpleName() + args;
	}
}
