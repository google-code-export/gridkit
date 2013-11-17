package org.gridkit.lab.orchestration;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.gridkit.lab.orchestration.BeanProxy.Handler;
import org.gridkit.lab.orchestration.Platform.ScriptConstructor;
import org.gridkit.lab.orchestration.util.ClassOps;

public class RemoteBean implements Serializable {        
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
        SourceRef ref = new SourceRef(
            "Deploy[" + ClassOps.toString(prototype.getClass()) + "]", location
        );
        
        RemoteBean bean = new RemoteBean(prototype.getClass(), scope, ref);
        
        return new RemoteBean.Deploy(bean, prototype);
    }
    
    private static abstract class BeanAction implements Callable<Void>, ViAction, Serializable {
        private static final long serialVersionUID = 4825392097027091793L;
        
        protected RemoteBean result;

        public BeanAction(RemoteBean result) {
            this.result = result;
        }

        public String getId() {
            return result.ref.getId();
        }

        public Scope getScope() {
            return result.scope;
        }

        public Callable<Void> getExecutor() {
            return this;
        }
        
        public String toString() {
            return result.ref.toString();
        }
        
        public <T> T getProxy(Platform platform) {
            return result.getProxy(platform);
        }
    }
    
    public static class Deploy extends BeanAction {
        private static final long serialVersionUID = -488868113954566425L;
        
        private Object prototype;
        
        public Deploy(RemoteBean bean, Object prototype) {
            super(bean);
            this.prototype = prototype;
        }

        public Void call() {
            BeanRegistry.getInstance().deploy(result.ref, prototype);
            return null;
        }
    }
    
    public static class Invoke extends BeanAction {
        private static final long serialVersionUID = 613178071091607041L;
        
        protected RemoteBean target;
        protected MethodRef method;
        protected List<Argument<RemoteBean>> args;
        
        public Invoke(RemoteBean target, Method method, List<Argument<RemoteBean>> args, RemoteBean result) {
            super(result);
            this.target = target;
            this.method = new MethodRef(method);
            this.args = args;
        }

        @Override
        public Void call() throws Exception {
            try {
                BeanRegistry.getInstance().invoke(target.ref, method, getArgs(), result.ref);
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
        
        private List<Argument<SourceRef>> getArgs() {
            List<Argument<SourceRef>> result = new ArrayList<Argument<SourceRef>>();
            
            for (Argument<RemoteBean> arg : args) {
                if (arg.isRef()) {
                    result.add(Argument.newRef(arg.getRef().ref));
                } else {
                    result.add(Argument.<SourceRef>newVal(arg.getVal()));
                }
            }
            
            return result;
        }
    }

    protected static class ProxyHandler implements Handler {
        private RemoteBean bean;
        private Platform platform;
        
        public ProxyHandler(RemoteBean bean, Platform platform) {
            this.bean = bean;
            this.platform = platform;
        }

        @Override
        public Object invoke(Method method, List<Argument<Handler>> rawArgs) {
            List<Argument<RemoteBean>> args = getArgs(rawArgs);
            
            SourceRef ref = new SourceRef(ClassOps.toString(method), ClassOps.location(3));
            
            ScriptConstructor constructor = platform.getScriptConstructor();
            
            Scope scope = bean.scope;
            if (constructor != null) {
                scope = Scopes.and(scope, constructor.getScope());
            }
            
            RemoteBean result = new RemoteBean(method.getReturnType(), scope, ref);
            
            RemoteBean.Invoke invoke = new RemoteBean.Invoke(bean, method, args, result);
            
            if (constructor != null) {
                constructor.getScriptBuilder().action(invoke, getRefs(args));
            } else {
                platform.execute(invoke);
            }

            return result.getProxy(platform);
        }
        
        private List<String> getRefs(List<Argument<RemoteBean>> args) {
            List<String> result = new ArrayList<String>();
            
            for (Argument<RemoteBean> arg : args) {
                if (arg.isRef()) {
                    result.add(arg.getRef().ref.getId());
                }
            }
            result.add(bean.ref.getId());
            
            return result;
        }
                
        private List<Argument<RemoteBean>> getArgs(List<Argument<Handler>> args) {
            List<Argument<RemoteBean>> result = new ArrayList<Argument<RemoteBean>>(args.size());
            
            for (Argument<Handler> arg : args) {
                result.add(validate(arg));
            }
            
            return result;
        }
        
        private Argument<RemoteBean> validate(Argument<Handler> arg) {
            if (arg.isRef()) {
                Handler handler = arg.getRef();
                if (!(handler instanceof ProxyHandler)) {
                    throw new IllegalArgumentException();
                }
                
                ProxyHandler proxyHandler = (ProxyHandler)handler;
                if (proxyHandler.platform != platform) {
                    throw new IllegalArgumentException();
                }
                
                return Argument.newRef(proxyHandler.bean);
            } else {
                boolean valid = arg.getVal() instanceof Serializable || arg.getVal() instanceof Remote;
                
                if (!valid){
                    throw new IllegalArgumentException();
                }
                
                return Argument.newVal(arg.getVal());
            }
        }
        
        public RemoteBean getBean() {
            return bean;
        }
    }
    
    public RemoteBean onScope(Scope scope) {
        return new RemoteBean(this.clazz, Scopes.and(this.scope, scope), this.ref);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Platform platform) {
        return (T)BeanProxy.newInstance(clazz, new ProxyHandler(this, platform));
    }
}
