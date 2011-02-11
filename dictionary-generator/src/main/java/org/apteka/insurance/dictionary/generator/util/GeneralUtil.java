package org.apteka.insurance.dictionary.generator.util;

public class GeneralUtil {
	public static boolean hasText(String str) {
		if (str.length() == 0)
			return false;
		
		int strLen = str.length();
		for (int i = 0; i < strLen; i++)
			if (!Character.isWhitespace(str.charAt(i)))
				return true;
		
		return false;
	}
}
