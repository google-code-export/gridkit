package com.medx.dictionary;

import org.apteka.insurance.attribute.AttrKey;

public final class Patient {
	public static final AttrKey<String> clientName = new AttrKey<String>(8, String.class, "");
	public static final AttrKey<String> name = new AttrKey<String>(9, String.class, "");
	public static final class Id {
		public static final int clientName = 8;
		public static final int name = 9;
	}
	public static final class Text {
		public static final String clientName = "Patient.clientName";
		public static final String name = "Patient.name";
	}
}
