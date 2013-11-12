package org.gridkit.lab.orchestration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ClassOps {
    public static Class<?>[] interfaces(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return new Class<?>[0];
        } else if (clazz.isInterface()) {
            return new Class<?>[] {clazz}; 
        } else {
            Set<Class<?>> interfaces = new HashSet<Class<?>>();
            interfaces(clazz, interfaces);
            return interfaces.toArray(new Class<?>[interfaces.size()]);
        }
    }

    private static void interfaces(Class<?> clazz, Set<Class<?>> result) {
        if (Object.class.equals(clazz)) {
            return;
        } else {
            result.addAll(Arrays.asList(clazz.getInterfaces()));
            interfaces(clazz.getSuperclass(), result);
        }
    }
    
    public static StackTraceElement stackTraceElement(int depth) {
        return new Exception().getStackTrace()[depth+1];
    }
    
    public static String location(StackTraceElement trace) {
        if (trace.isNativeMethod()) {
            return "(Native Method)";
        } else if (trace.getFileName() != null && trace.getLineNumber() >=0) {
            return String.format("(%s:%d)", trace.getFileName(), trace.getLineNumber());
        } else if (trace.getFileName() != null) {
            return String.format("(%s)", trace.getFileName());
        } else {
            return "(Unknown Source)";
        }
    }
}
