package com.medx.framework.util;

public class CastUtil {
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object arg) {
		return (T)arg;
	}
}
