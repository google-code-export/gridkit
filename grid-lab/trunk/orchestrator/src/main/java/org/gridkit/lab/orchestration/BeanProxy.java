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
    
    private Handler handler;
    
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

    public Handler getHandler() {
        return handler;
    }

    private Argument<Handler> translate(Object object) {
        Handler handler = getHandler(object, Handler.class);
        
        if (handler != null) {
            return Argument.newRef(handler);
        } else {
            return Argument.newVal(object);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Handler> T getHandler(Object object, Class<T> clazz) {
        if (Proxy.isProxyClass(object.getClass())) {
            Object proxy = Proxy.getInvocationHandler(object);
            if (proxy instanceof BeanProxy) {
                BeanProxy bProxy = (BeanProxy)proxy;
                if (clazz.isInstance(bProxy.getHandler())) {
                    return (T)bProxy.getHandler();
                }
            }
        }
        return null;
    }
}
