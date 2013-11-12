package org.gridkit.lab.orchestration;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodRef implements Serializable {
    private static final long serialVersionUID = -2873786652381886609L;
    
    private final Class<?> clazz;
    private final String name;
    private final Class<?>[] parameterTypes;
    
    public MethodRef(Method method) {
        clazz = method.getDeclaringClass();
        name = method.getName();
        parameterTypes = method.getParameterTypes();
    }
    
    public Method newMethod() {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Object invoke(Object obj, Object... args) throws InvocationTargetException {
        try {
            return newMethod().invoke(obj, args);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
