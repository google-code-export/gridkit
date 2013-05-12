package org.gridkit.lab.mcube;

import static org.gridkit.lab.mcube.Functions.divide;
import static org.gridkit.lab.mcube.Functions.square;
import static org.gridkit.lab.mcube.Functions.squareRoot;
import static org.gridkit.lab.mcube.Functions.subtract;

import java.io.Serializable;
import java.util.Iterator;

import org.gridkit.lab.tentacle.Sample.SampleMeta;

public class Aggregates {

	public static Aggregation invariant(Value v) {
		return atomic(new Invariant(v));
	}
	
	public static Aggregation sum(Value v) {
		return atomic(new Sum(v));
	}

	public static Aggregation sumOfSquares(Value v) {
		return atomic(new SumOfSquares(v));
	}

	public static Aggregation count() {
		return atomic(new Count(SampleMeta.STAR));
	}

	public static Aggregation mean(Value v) {
		return new Mean(v);
	}	

	public static Aggregation stdDev(Value v) {
		return new StdDev(v);
	}

	public static Aggregation variance(Value v) {
		return new Variance(v);
	}

	public static Aggregation min(Value v) {
		return atomic(new Min(v));
	}

	public static Aggregation max(Value v) {
		return atomic(new Max(v));
	}
	
	static Aggregation atomic(AdditiveReducer reducer) {
		return new AtomicAggregation(reducer);
	}
	
	static class AtomicAggregation implements Aggregation, Serializable {

		private static final long serialVersionUID = 20130512L;
		
		private final AdditiveReducer reducer;
		
		public AtomicAggregation(AdditiveReducer reducer) {
			this.reducer = reducer;
		}

		@Override
		public boolean isAtomic() {
			return true;
		}

		@Override
		public Value rebuild(Aggregateable dataSet) {
			throw new UnsupportedOperationException();
		}

		@Override
		public AdditiveReducer getReducer() {
			return reducer;
		}

		@Override
		public String toString() {
			return String.valueOf(reducer);
		}
	}
	
	static class Invariant implements AdditiveReducer, Serializable {

		private static final long serialVersionUID = 20130512L;
		
		private final Value value;
		
		public Invariant(Value value) {
			this.value = value;
		}

		@Override
		public Value getSource() {
			return value;
		}

		@Override
		public Object reduceSamples(Iterator<Object> values) {
			Object val = values.next();
			while(values.hasNext()) {
				Object val2 = values.next();
				if ((val == null && val2 != null) || (val != null && !val.equals(val2))) {
					throw new IllegalArgumentException("Failed invariant");
				}
			}
			return val;
		}

		@Override
		public Object reduceReductions(Iterator<Object> values) {
			Object val = values.next();
			while(values.hasNext()) {
				Object val2 = values.next();
				if ((val == null && val2 != null) || (val != null && !val.equals(val2))) {
					throw new IllegalArgumentException("Failed invariant");
				}
			}
			return val;
		}

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
			Invariant other = (Invariant) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Invariant[" + value + "]";
		}
	}
	
	static class Sum extends AbstractArithmeticReducer implements Serializable {
		
		private static final long serialVersionUID = 20130512L;

		public Sum(Value value) {
			super(value);
		}

		@Override
		protected double reduce(double[] values, int len) {
			double r = 0;
			for(int i = 0; i != len; ++i) {
				r += values[i];
			}
			return r;
		}		
	}

	static class SumOfSquares extends AbstractArithmeticReducer implements Serializable {
		
		private static final long serialVersionUID = 20130512L;
		
		public SumOfSquares(Value value) {
			super(value);
		}
		
		@Override
		protected double reduce(double[] values, int len) {
			double r = 0;
			for(int i = 0; i != len; ++i) {
				r += values[i] * values[i];
			}
			return r;
		}		
	}

	static class Min extends AbstractArithmeticReducer implements Serializable {
		
		private static final long serialVersionUID = 20130512L;
		
		public Min(Value value) {
			super(value);
		}
		
		@Override
		protected double reduce(double[] values, int len) {
			double m = values[0];
			for(int i = 1; i != len; ++i) {
				if (values[i] < m) {
					m = values[i];
				}
			}
			return m;
		}		
	}

	static class Max extends AbstractArithmeticReducer implements Serializable {
		
		private static final long serialVersionUID = 20130512L;
		
		public Max(Value value) {
			super(value);
		}
		
		@Override
		protected double reduce(double[] values, int len) {
			double m = values[0];
			for(int i = 1; i != len; ++i) {
				if (values[i] < m) {
					m = values[i];
				}
			}
			return m;
		}		
	}
	
	static class Count implements AdditiveReducer, Serializable {

		private static final long serialVersionUID = 20130512L;

		private final Value value;
		
		public Count(Value value) {
			this.value = value;
		}

		@Override
		public Value getSource() {
			return value;
		}

		@Override
		public Object reduceSamples(Iterator<Object> values) {
			long n = 0;
			while(values.hasNext()) {
				++n;
				values.next();
			}
			return n;
		}

		@Override
		public Object reduceReductions(Iterator<Object> values) {
			long n = 0;
			while(values.hasNext()) {
				long l = ((Number)values.next()).longValue();
				n += l;
			}
			return n;
		}

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
			Count other = (Count) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}
	
	static class Mean implements Aggregation, Serializable {
		
		private static final long serialVersionUID = 20130512L;

		private final Value value;

		public Mean(Value value) {
			this.value = value;
		}

		@Override
		public boolean isAtomic() {
			return false;
		}

		@Override
		public Value rebuild(Aggregateable dataSet) {
			Value sum = dataSet.aggregate(sum(value));
			Value count = dataSet.aggregate(count());
			return Functions.divide(sum, count);
		}

		@Override
		public AdditiveReducer getReducer() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + value + "]";
		}
	}

	static class Variance implements Aggregation, Serializable {
		
		private static final long serialVersionUID = 20130512L;
		
		private final Value value;
		
		public Variance(Value value) {
			this.value = value;
		}
		
		@Override
		public boolean isAtomic() {
			return false;
		}
		
		@Override
		public Value rebuild(Aggregateable dataSet) {
			Value sqsum = dataSet.aggregate(sumOfSquares(value));
			Value sum = dataSet.aggregate(sum(value));
			Value count = dataSet.aggregate(count());
			return subtract(divide(sqsum, count), square(divide(sum, count)));
		}
		
		@Override
		public AdditiveReducer getReducer() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + value + "]";
		}
	}

	static class StdDev implements Aggregation, Serializable {
		
		private static final long serialVersionUID = 20130512L;
		
		private final Value value;
		
		public StdDev(Value value) {
			this.value = value;
		}
		
		@Override
		public boolean isAtomic() {
			return false;
		}
		
		@Override
		public Value rebuild(Aggregateable dataSet) {
			Value sqsum = dataSet.aggregate(sumOfSquares(value));
			Value sum = dataSet.aggregate(sum(value));
			Value count = dataSet.aggregate(count());
			return squareRoot(subtract(divide(sqsum, count), square(divide(sum, count))));
		}
		
		@Override
		public AdditiveReducer getReducer() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + value + "]";
		}
	}
}
