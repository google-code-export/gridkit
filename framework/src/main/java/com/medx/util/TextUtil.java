package com.medx.util;

public class TextUtil {
	public static String getCamelPrefix(String str) {
		for (int i = 0; i < str.length(); ++i)
			if (Character.isUpperCase(str.charAt(i)))
				return str.substring(0, i);
		return str;
	}
	
	public static String getRawType(String str) {
		int index = str.indexOf('<');
		
		if (index == -1)
			return str;
		else
			return str.substring(0, index);
	}
}
