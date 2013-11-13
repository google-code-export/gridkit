package org.gridkit.lab.orchestration;

import java.io.Serializable;
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
    
    public Method newMethod() throws Exception {
        return clazz.getDeclaredMethod(name, parameterTypes);
    }
    
    public Object invoke(Object obj, Object... args) throws Exception {
        return newMethod().invoke(obj, args);
    }
}
