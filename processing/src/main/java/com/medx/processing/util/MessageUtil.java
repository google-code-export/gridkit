package com.medx.processing.util;

public class MessageUtil {
	public static String createMessage(String message) {
		return "[ " + message + " ]";
	}
	
	public static String createMessage(Throwable throwable){
		if (throwable == null)
			return "";
		else if (throwable.getCause() == null)
			return createMessage(throwable.getMessage());
		else
			return createMessage(throwable.getMessage()) + " : " + createMessage(throwable.getCause());
	}
	
	public static String createMessage(String message, Throwable throwable) {
		if (throwable == null)
			return createMessage(message);
		else
			return createMessage(message) + " : " + createMessage(throwable);
	}
}
