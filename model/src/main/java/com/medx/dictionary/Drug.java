package com.medx.dictionary;

import org.apteka.insurance.attribute.AttrKey;

public final class Drug {
	public static final AttrKey<String> name = new AttrKey<String>(1, String.class, "");
	public static final AttrKey<Double> price = new AttrKey<Double>(2, Double.class, "");
	public static final class Id {
		public static final int name = 1;
		public static final int price = 2;
	}
	public static final class Text {
		public static final String name = "Drug.name";
		public static final String price = "Drug.price";
	}
}
