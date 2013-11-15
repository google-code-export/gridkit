package org.gridkit.lab.orchestration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gridkit.lab.orchestration.util.ClassOps;

public class BeanProxy implements InvocationHandler {
    
    public interface Handler {
        Object invoke(Method method, List<Argument<Handler>> args); 
    }

    public static Object newInstance(Class<?> clazz, Handler handler) {
        Class<?>[] interfaces = ClassOps.interfaces(clazz);
        return Proxy.newProxyInstance(
            handler.getClass().getClassLoader(),
            interfaces, new BeanProxy(handler)
        );
    }
    
    private final Handler handler;
    
    private BeanProxy(Handler handler) {
        this.handler = handler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Argument<Handler>> actionArgs;
        
        if (args != null) {
            actionArgs = new ArrayList<Argument<Handler>>(args.length);
            for (Object arg : args) {
                actionArgs.add(translate(arg));
            }
        } else {
            actionArgs = Collections.emptyList();
        }

        return handler.invoke(method, actionArgs);
    }
    
    private Argument<Handler> translate(Object object) {
        if (isBeanProxy(object)) {
            Handler handler = getHandler(object);
            
            if (handler instanceof Handler) {
                return Argument.newRef((Handler)handler);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            return Argument.newVal(object);
        }
    }
    
    private static boolean isBeanProxy(Object object) {
        if (Proxy.isProxyClass(object.getClass())) {
            Object handler = Proxy.getInvocationHandler(object);
            if (handler instanceof BeanProxy) {
                return true;
            }
        }
        return false;
    }
    
    private static Handler getHandler(Object object) {
        return ((Handler)Proxy.getInvocationHandler(object));
    }
}
