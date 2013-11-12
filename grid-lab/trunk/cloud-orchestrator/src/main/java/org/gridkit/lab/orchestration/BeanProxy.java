package org.gridkit.lab.orchestration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BeanProxy implements InvocationHandler {
    
    public interface Handler {
        Object invoke(Method method, List<Argument> args); 
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
        List<Argument> actionArgs;
        
        if (args != null) {
            actionArgs = new ArrayList<Argument>(args.length);
            for (Object arg : args) {
                actionArgs.add(translate(arg));
            }
        } else {
            actionArgs = Collections.emptyList();
        }

        return handler.invoke(method, actionArgs);
    }
    
    private Argument translate(Object object) {
        if (isBeanProxy(object)) {
            Handler handler = getHandler(object);
            
            if (handler instanceof RemoteBean) {
                return Argument.newRemote((RemoteBean)handler);
            } else if (handler instanceof LocalBean) {
                return Argument.newLocal((LocalBean)handler);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            return Argument.newValue(object);
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
    
    public static class Argument {
        private RemoteBean remote;
        private LocalBean local;
        private Object value;
        
        private Argument(RemoteBean remote, LocalBean local, Object value) {
            this.remote = remote;
            this.local = local;
            this.value = value;
        }
       
        private static Argument newRemote(RemoteBean remote) {
            return new Argument(remote, null, null);
        }
        
        private static Argument newLocal(LocalBean local) {
            return new Argument(null, local, null);
        }
        
        private static Argument newValue(Object value) {
            return new Argument(null, null, value);
        }
        
        public boolean isRemote() {
            return remote != null;
        }
        
        public boolean isLocal() {
            return local != null;
        }
        
        public boolean isValue() {
            return remote == null && local == null;
        }
        
        public RemoteBean getRemote() {
            return remote;
        }
        
        public LocalBean getLocal() {
            return local;
        }
        
        public Object getValue() {
            return value;
        }
    }
}
