package org.gridkit.coherence.util.classloader;

import java.util.Arrays;

public class ExceptionWeaver {
	
	public static void replaceStackTop(Throwable receiver, StackTraceElement receiverTop, Throwable donnor, StackTraceElement donnorBottom, StackTraceElement boundary) {
		StackTraceElement[] rtrace = receiver.getStackTrace();
		StackTraceElement[] dtrace = donnor.getStackTrace();
		
		StackTraceElement[] result = new StackTraceElement[rtrace.length + dtrace.length + 1];
		
		int dr = findLowestMatch(donnorBottom, dtrace);
		int rr =findHighestMatch(receiverTop, rtrace);

		int n = 0;
		
		for(int i = 0; i < rr; ++i) {
			result[n++] = rtrace[i];
		}
		
		if (boundary != null) {
			result[n++] = boundary;
		}

		for(int i = 0; i != dtrace.length; ++i) {
			if (i > dr) {
				result[n++] = dtrace[i];
			}
		}

		result = Arrays.copyOf(result, n);
		
		try {
			receiver.setStackTrace(result);
		} catch (Exception e) {
			// ignore
		}
	}
	
	private static int findLowestMatch(StackTraceElement pattern, StackTraceElement[] trace) {
		for(int i = trace.length; i != 0;) {
			--i;
			if (match(pattern, trace[i])) {
				return i;
			}
		}
		return -1;
	}

	private static int findHighestMatch(StackTraceElement pattern, StackTraceElement[] trace) {
		for(int i = 0; i != trace.length; ++i) {
			if (match(pattern, trace[i])) {
				return i;
			}
		}
		return trace.length;
	}

	public static boolean match(StackTraceElement pattern, StackTraceElement sample) {
		if (pattern.getClassName().length() > 0) {
			if (!sample.getClassName().startsWith(pattern.getClassName())) {
				return false;
			}
			else if (!sample.getClassName().equals(pattern.getClassName())) {
				if (!sample.getClassName().startsWith(pattern.getClassName() +".") 
						&& !sample.getClassName().startsWith(pattern.getClassName() +"$")) {
					return false;
				}
			}
		}
		if (pattern.getMethodName().length() > 0) {
			if (!pattern.getMethodName().equals(sample.getMethodName())) {
				return false;
			}			
		}
		if (pattern.getFileName() != null) {
			if (!pattern.getFileName().equals(sample.getFileName())) {
				return false;
			}
		}
		if (pattern.getLineNumber() != -1) {
			if (pattern.getLineNumber() != sample.getLineNumber()) {
				return false;
			}
		}
		return true;
	}
	
}
