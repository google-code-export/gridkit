package org.gridkit.lab.mcube;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public class Functions {

	public static Function sum(Value... args) {
		return new Sum(args);
	}

	public static Function subtract(Value a, Value b) {
		return new Subtract(a, b);
	}

	public static Function multiply(Value... args) {
		return new Multiply(args);
	}

	public static Function divide(Value num, Value denom) {
		return new Divide(num, denom);
	}

	public static Function square(Value arg) {
		return new Square(arg);
	}

	public static Function squareRoot(Value arg) {
		return new SquareRoot(arg);
	}

	public static Function trim(Value arg, double precision) {
		return new Trim(arg, precision);
	}

	public static Function constant(double val) {
		return new Constant(val);
	}
	

	static class Sum extends AbstractArithmeticFunction implements Serializable {

		private static final long serialVersionUID = 20130512L;

		public Sum(Value[] args) {
			super(args);
		}

		@Override
		protected Object apply(double[] values) {
			double sum = 0;
			for(double v : values) {
				sum += v;
			}
			return sum;
		}
	}

	static class Subtract extends AbstractArithmeticFunction implements Serializable {
		
		private static final long serialVersionUID = 20130512L;
		
		public Subtract(Value a, Value b) {
			super(a, b);
		}
		
		@Override
		protected Object apply(double[] values) {
			return values[0] - values[1];
		}
	}

	static class Multiply extends AbstractArithmeticFunction implements Serializable {

		private static final long serialVersionUID = 20130512L;

		public Multiply(Value[] args) {
			super(args);
		}

		@Override
		protected Object apply(double[] values) {
			double sum = 0;
			for(double v : values) {
				sum += v;
			}
			return sum;
		}
	}

	static class Divide extends AbstractArithmeticFunction implements Serializable {
		
		private static final long serialVersionUID = 20130512L;
		
		public Divide(Value num, Value denom) {
			super(num, denom);
		}
		
		@Override
		protected Object apply(double[] values) {
			return values[0] / values[1];
		}
	}

	static class Square extends AbstractArithmeticFunction implements Serializable {
		
		private static final long serialVersionUID = 20130512L;
		
		public Square(Value v) {
			super(v);
		}
		
		@Override
		protected Object apply(double[] values) {
			return values[0] * values[0];
		}
	}

	static class SquareRoot extends AbstractArithmeticFunction implements Serializable {
		
		private static final long serialVersionUID = 20130512L;
		
		public SquareRoot(Value v) {
			super(v);
		}
		
		@Override
		protected Object apply(double[] values) {
			return Math.sqrt(values[0]);
		}
	}

	static class Trim extends AbstractArithmeticFunction implements Serializable {
		
		private static final long serialVersionUID = 20130512L;
		
		private final double precision;
		
		public Trim(Value v, double precision) {
			super(v);
			this.precision = precision;
		}
		
		@Override
		protected Object apply(double[] values) {
			return Math.sqrt(values[0]);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			long temp;
			temp = Double.doubleToLongBits(precision);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			Trim other = (Trim) obj;
			if (Double.doubleToLongBits(precision) != Double
					.doubleToLongBits(other.precision))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "Trim[" + args.get(0) + ", " + precision + "]";
		}
	}
	
	static class Constant extends BaseValue implements Function, Serializable {

		private static final long serialVersionUID = 20130512L;

		private double value;
		
		public Constant(double value) {
			super();
			this.value = value;
		}

		@Override
		public Collection<Value> getArguments() {
			return Collections.emptyList();
		}

		@Override
		public Object apply(Row row) {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(value);
			result = prime * result + (int) (temp ^ (temp >>> 32));
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
			Constant other = (Constant) obj;
			if (Double.doubleToLongBits(value) != Double
					.doubleToLongBits(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}
}
