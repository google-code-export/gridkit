package com.medx.framework.util;

public class TextUtil {
	public static String getCamelPrefix(String str) {
		for (int i = 0; i < str.length(); ++i)
			if (Character.isUpperCase(str.charAt(i)))
				return str.substring(0, i);
		return str;
	}
	
	public static String getCamelPostfix(String str) {
		for (int i = 0; i < str.length(); ++i)
			if (Character.isUpperCase(str.charAt(i)))
				return Character.toLowerCase(str.charAt(i)) + str.substring(i + 1);
		return "";
	}
	
	public static String repeat(char ch, int count) {
		String result = "";
		
		while (count-- > 0)
			result += ch;
		
		return result;
	}
}
