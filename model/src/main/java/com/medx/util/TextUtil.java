package com.medx.util;

public class TextUtil {
	public static String getCamelPrefix(String str) {
		for (int i = 0; i < str.length(); ++i)
			if (Character.isUpperCase(str.charAt(i)))
				return str.substring(0, i);
		return str;
	}
	
	public static String getCamelPostfix(String str) {
		return null;
	}
}
