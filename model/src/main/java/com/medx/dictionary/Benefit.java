package com.medx.dictionary;

import org.apteka.insurance.attribute.AttrKey;

public final class Benefit {
	public static final AttrKey<Double> discretionValue = new AttrKey<Double>(5, Double.class, "");
	public static final AttrKey<java.util.Map<String, Integer>> drugLimits = new AttrKey<java.util.Map<String, Integer>>(6, java.util.Map.class, "");
	public static final class Id {
		public static final int discretionValue = 5;
		public static final int drugLimits = 6;
	}
	public static final class Text {
		public static final String discretionValue = "Benefit.discretionValue";
		public static final String drugLimits = "Benefit.drugLimits";
	}
}
