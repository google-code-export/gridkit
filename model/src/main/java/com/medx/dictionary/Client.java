package com.medx.dictionary;

import org.apteka.insurance.attribute.AttrKey;

public final class Client {
	public static final AttrKey<String> name = new AttrKey<String>(7, String.class, "");
	public static final class Id {
		public static final int name = 7;
	}
	public static final class Text {
		public static final String name = "Client.name";
	}
}
