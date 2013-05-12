package org.gridkit.lab.mcube;

import java.io.Serializable;
import java.util.Iterator;

public abstract class AbstractArithmeticReducer implements AdditiveReducer, Serializable {

	private static final long serialVersionUID = 20130512L;
	
	private final Value value;
	
	public AbstractArithmeticReducer(Value value) {
		this.value = value;
	}

	@Override
	public Value getSource() {
		return value;
	}

	@Override
	public Object reduceSamples(Iterator<Object> values) {
		return reduceReductions(values);
	}

	@Override
	public Object reduceReductions(Iterator<Object> values) {
		double[] buf = new double[256];
		int n = 0;
		while(true) {
			Number v = (Number) values.next();
			buf[n] = v.doubleValue();
			++n;
			if (!values.hasNext()) {
				double r = reduce(buf, n);
				return r;
			}
			if (n == buf.length) {
				double r = reduce(buf, n);
				buf[0] = r;
				n = 1;
			}
		}
	}
	
	protected abstract double reduce(double[] values, int len);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		AbstractArithmeticReducer other = (AbstractArithmeticReducer) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + value + "]";
	}
}
