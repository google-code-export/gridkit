package org.apteka.insurance.dictionary.datetime;

import org.apteka.insurance.attribute.AttrKey;
import org.apteka.insurance.attribute.AttrKeyRegistry;

public final class Date {
	public static final AttrKey<java.util.Date> startDate = new AttrKey<java.util.Date>(0, java.util.Date.class, "startDate description");
	public static final AttrKey<java.util.Date> finishDate = new AttrKey<java.util.Date>(1, java.util.Date.class, "finishDate description");
	public static final class Id {
		public static final int startDate = 0;
		public static final int finishDate = 1;
	}
	public static final class Text {
		public static final String startDate = "startDate";
		public static final String finishDate = "finishDate";
	}
	static {
		AttrKeyRegistry.getInstance().registerAttrKey("startDate", startDate);
		AttrKeyRegistry.getInstance().registerAttrKey("finishDate", finishDate);
	}
}
