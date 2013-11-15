package org.gridkit.lab.orchestration;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.gridkit.lab.orchestration.BeanProxy.Handler;
import org.gridkit.lab.orchestration.util.ClassOps;

public abstract class RemoteBean implements Serializable, ViNodeAction {        
    private static final long serialVersionUID = -6430323142684195091L;
    
    protected SourceRef ref;
    protected Scope scope;
    protected Class<?> clazz;
    
    public RemoteBean(Class<?> clazz, Scope scope, SourceRef ref) {
        this.ref = ref;
        this.scope = scope;
        this.clazz = clazz;
    }
    
    public static RemoteBean.Deploy newDeploy(Object prototype, Scope scope, StackTraceElement location) {
        return new RemoteBean.Deploy(prototype, scope, location);
    }
    
    public static class Deploy extends RemoteBean implements Callable<Void> {
        private static final long serialVersionUID = -488868113954566425L;
        
        protected Object prototype;
        
        public Deploy(Object prototype, Scope scope, StackTraceElement location) {
            super(prototype.getClass(), scope, new SourceRef(
                "Deploy[" + ClassOps.toString(prototype.getClass()) + "]", location
            ));
            this.prototype = prototype;
        }

        @Override
        public Void call() {
            BeanRegistry.getInstance().deploy(ref, prototype);
            return null;
        }
        
        @Override
        public Callable<Void> getExecutor() {
            return this;
        }
    }
    
    public static class Invoke extends RemoteBean implements Callable<Void> {
        private static final long serialVersionUID = 613178071091607041L;
        
        protected SourceRef targetRef;
        protected MethodRef method;
        protected List<Argument<SourceRef>> args;
        
        public Invoke(SourceRef targetRef, Method method, List<Argument<SourceRef>> args, Scope scope, StackTraceElement location) {
            super(method.getReturnType(), scope, new SourceRef(
                    ClassOps.toString(method), location
            ));
            this.targetRef = targetRef;
            this.method = new MethodRef(method);
            this.args = args;
        }

        @Override
        public Void call() throws Exception {
            try {
                BeanRegistry.getInstance().invoke(targetRef, method, args, ref);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Exception) {
                    throw (Exception)cause;
                } else {
                    throw e;
                }
            }
            return null;
        }
        
        @Override
        public Callable<Void> getExecutor() {
            return this;
        }
    }

    private static class ProxyHandler implements Handler {
        private RemoteBean bean;
        private Platform platform;
        
        public ProxyHandler(RemoteBean bean, Platform platform) {
            this.bean = bean;
            this.platform = platform;
        }

        @Override
        public Object invoke(Method method, List<Argument<Handler>> args) {
            validate(args);
            
            List<Argument<SourceRef>> registryRefs = getRegistryRefs(args);
            
            RemoteBean.Invoke result = new RemoteBean.Invoke(
                bean.ref, method, registryRefs, bean.scope, ClassOps.location(3)
            );
            
            platform.invoke(result, getScriptRefs(registryRefs));

            return result.getProxy(platform);
        }
        
        private List<String> getScriptRefs(List<Argument<SourceRef>> args) {
            List<String> result = new ArrayList<String>();
            
            for (Argument<SourceRef> arg : args) {
                if (arg.isRef()) {
                    result.add(arg.getRef().getId());
                }
            }
            result.add(bean.ref.getId());
            
            return result;
        }
        
        private static List<Argument<SourceRef>> getRegistryRefs(List<Argument<Handler>> args) {
            List<Argument<SourceRef>> result = new ArrayList<Argument<SourceRef>>(args.size());
            
            for (Argument<Handler> arg : args) {
                if (arg.isRef()) {
                    SourceRef ref = ((ProxyHandler)arg.getRef()).bean.ref;
                    result.add(Argument.newRef(ref));
                } else {
                    result.add(Argument.<SourceRef>newVal(arg.getVal()));
                }
            }
            
            return result;
        }

        
        private void validate(List<Argument<Handler>> args) {
            for (Argument<Handler> arg : args) {
                validate(arg);
            }
        }
        
        private void validate(Argument<Handler> arg) {
            if (arg.isRef()) {
                Handler handler = arg.getRef();
                if (!(handler instanceof ProxyHandler)) {
                    throw new IllegalArgumentException();
                }
                
                ProxyHandler proxyHandler = (ProxyHandler)handler;
                if (proxyHandler.platform != platform) {
                    throw new IllegalArgumentException();
                }
            } else if (arg.isVal()) {
                boolean valid = arg.getVal() instanceof Serializable || arg.getVal() instanceof Remote;
                if (!valid){
                    throw new IllegalArgumentException();
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Platform platform) {
        return (T)BeanProxy.newInstance(clazz, new ProxyHandler(this, platform));
    }
    
    public String getId() {
        return ref.getId();
    }
    
    public Scope getScope() {
        return scope;
    }
    
    @Override
    public String toString() {
        return ref.toString();
    }
}
