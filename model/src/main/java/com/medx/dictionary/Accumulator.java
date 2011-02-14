package com.medx.dictionary;

import org.apteka.insurance.attribute.AttrKey;

public final class Accumulator {
	public static final AttrKey<Double> discretionValue = new AttrKey<Double>(3, Double.class, "");
	public static final AttrKey<java.util.Map<String, Integer>> drugLimits = new AttrKey<java.util.Map<String, Integer>>(4, java.util.Map.class, "hello");
	public static final class Id {
		public static final int discretionValue = 3;
		public static final int drugLimits = 4;
	}
	public static final class Text {
		public static final String discretionValue = "Accumulator.discretionValue";
		public static final String drugLimits = "Accumulator.drugLimits";
	}
}
