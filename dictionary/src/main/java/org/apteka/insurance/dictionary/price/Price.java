package org.apteka.insurance.dictionary.price;

import org.apteka.insurance.attribute.AttrKey;
import org.apteka.insurance.attribute.AttrKeyRegistry;

public final class Price {
	public static final AttrKey<Double> initialPrice = new AttrKey<Double>(2, Double.class, "initialPrice description");
	public static final class Id {
		public static final int initialPrice = 2;
	}
	public static final class Text {
		public static final String initialPrice = "initialPrice";
	}
	static {
		AttrKeyRegistry.getInstance().registerAttrKey("initialPrice", initialPrice);
	}
}
