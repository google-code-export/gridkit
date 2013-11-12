package org.gridkit.lab.orchestration;

import static org.gridkit.lab.orchestration.StringOps.F;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.List;

import org.gridkit.lab.orchestration.BeanRegistry.BeanRef;
import org.gridkit.lab.orchestration.script.ScriptBean;
import org.gridkit.util.concurrent.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RemoteBean implements BeanProxy.Handler, Platform.ScriptBeanProvider {    
    private static final Logger log = LoggerFactory.getLogger(RemoteBean.class);
    
    protected BeanRef ref;
    protected Scope scope;
    protected Platform platform;
    protected Class<?> clazz;
    protected Object proxy;
    protected StackTraceElement createPoint;
    
    public RemoteBean(Class<?> clazz, BeanRef ref, Scope scope, Platform platform, StackTraceElement createPoint) {
        this.ref = ref;
        this.scope = scope;
        this.platform = platform;
        this.clazz = clazz;
        this.createPoint = createPoint;
    }
    
    @Override
    public Object invoke(Method method, List<BeanProxy.Argument> args) {
        validate(args);
        
        RemoteBean.Invoke result = platform.newRemoteBean(
            scope, this, method, args, ClassOps.stackTraceElement(3)
        );
        
        platform.invoke(result, getRefs(args));

        return result.getProxy();
    }
       
    public static class Deploy extends RemoteBean implements ScriptBean {
        protected Object prototype;
        
        public Deploy(Object prototype, BeanRef ref, Scope scope, Platform platform, StackTraceElement createPoint) {
            super(prototype.getClass(), ref, scope, platform, createPoint);
            this.prototype = prototype;
        }

        @Override
        public ScriptBean getHookBean() {
            return new HookDeploy(ref, prototype);
        }
        
        @Override
        public ScriptBean getScenarioBean() {
            return this;
        }
        
        @Override
        public void create(Box<Void> box) {
            String[] nodes = platform.vinode(scope);
            
            String logTarget = F("'%s' at %s", prototype, ClassOps.location(createPoint));
            
            if (nodes.length == 0) {
                log.info(F("No nodes found to deploy %s", logTarget));
                box.setData(null);
            } else {
                String aboutMsg   = F("About to deploy %s" , prototype, ClassOps.location(createPoint));
                String successMsg = F("Deploy of %s finished successfully", prototype, ClassOps.location(createPoint));
                String failureMsg = F("Deploy of %s finished with failure", prototype, ClassOps.location(createPoint));
                
                log.info(aboutMsg);
                RemoteBox remoteBox = new LoggingRemoteBox(box, nodes.length, successMsg, failureMsg);
                ScenarioDeploy executor = new ScenarioDeploy(ref, prototype, remoteBox);
                platform.cloud().nodes(nodes).submit(executor);
            }
        }
        
        @Override
        public Object getRef() {
            return ref;
        }
    }
    
    public static class Invoke extends RemoteBean implements ScriptBean {
        protected RemoteBean target;
        protected Method method;
        protected List<BeanProxy.Argument> args;
        
        public Invoke(BeanRef ref, Scope scope, Platform platform,
                      RemoteBean target, Method method, 
                      List<BeanProxy.Argument> args,
                      StackTraceElement createPoint) {
            super(method.getReturnType(), ref, scope, platform, createPoint);
            this.target = target;
            this.method = method;
            this.args = args;
        }

        @Override
        public ScriptBean getHookBean() {
            return new HookInvoke(
                target.ref, new MethodRef(method), getRegistryArgs(), ref
            );
        }
        
        @Override
        public ScriptBean getScenarioBean() {
            return this;
        }
        
        @Override
        public void create(Box<Void> box) {
            String[] nodes = platform.vinode(scope);
            
            String logTarget = F("%s.%s() at %s", method.getDeclaringClass().getSimpleName(), method.getName(), ClassOps.location(createPoint));
            
            if (nodes.length == 0) {
                log.info(F("No nodes found for invocation of %s", logTarget));
                box.setData(null);
            } else {
                String aboutMsg   = F("About to invoke %s" , logTarget);
                String successMsg = F("Invocation of %s finished successfully", logTarget);
                String failureMsg = F("Invocation of %s finished with error", logTarget);

                log.info(aboutMsg);
                RemoteBox remoteBox = new LoggingRemoteBox(box, nodes.length, successMsg, failureMsg);
                ScenarioInvoke executor = new ScenarioInvoke(
                    target.ref, new MethodRef(method), getRegistryArgs(), ref, remoteBox
                );
                platform.cloud().nodes(nodes).submit(executor);
            }
        }
        
        private List<BeanRegistry.Argument> getRegistryArgs() {
            List<BeanRegistry.Argument> result = new ArrayList<BeanRegistry.Argument>(args.size());
            
            for (BeanProxy.Argument arg : args) {
                if (arg.isRemote()) {
                    result.add(BeanRegistry.Argument.newRef(arg.getRemote().ref));
                } else {
                    result.add(BeanRegistry.Argument.newVal(arg.getValue()));
                }
            }
            
            return result;
        }
        
        @Override
        public Object getRef() {
            return ref;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy() {
        if (proxy == null) {
            proxy = BeanProxy.newInstance(clazz, this);
        }
        return (T) proxy;
    }

    private void validate(List<BeanProxy.Argument> args) {
        for (BeanProxy.Argument arg : args) {
            validate(arg);
        }
    }
    
    private static void validate(BeanProxy.Argument arg) {
        if (arg.isLocal()) {
            throw new IllegalArgumentException();
        } else if (arg.isValue()) {
            boolean valid = arg.getValue() instanceof Serializable || arg.getValue() instanceof Remote;
            if (!valid){
                throw new IllegalArgumentException();
            }
        }
    }

    private List<Object> getRefs(List<BeanProxy.Argument> args) {
        List<Object> result = new ArrayList<Object>();
        
        for (BeanProxy.Argument arg : args) {
            if (arg.isRemote()) {
                result.add(arg.getRemote().ref);
            }
        }
        result.add(ref);
        
        return result;
    }
    
    private static class ScenarioDeploy implements Runnable, Serializable {
        private static final long serialVersionUID = -1013487079759748972L;
        
        private BeanRef ref;
        private Object bean;
        private RemoteBox box;

        public ScenarioDeploy(BeanRef ref, Object bean, RemoteBox box) {
            this.ref = ref;
            this.bean = bean;
            this.box = box;
        }

        @Override
        public void run() {
            try {
                BeanRegistry.getInstance().deploy(ref, bean);
            } catch (Exception e) {
                box.failure(e);
                return;
            }
            box.success();
        }
    }
    
    private static class HookDeploy implements ScriptBean, Serializable {
        private static final long serialVersionUID = 9161261099583795231L;
        
        private BeanRef ref;
        private Object bean;
        
        public HookDeploy(BeanRef ref, Object bean) {
            this.ref = ref;
            this.bean = bean;
        }

        @Override
        public void create(Box<Void> box) {
            try {
                BeanRegistry.getInstance().deploy(ref, bean);
            } catch (Exception e) {
                box.setError(e);
                return;
            }
            box.setData(null);
        }
        
        @Override
        public Object getRef() {
            return ref;
        }
    }

    public static class ScenarioInvoke implements Runnable, Serializable {
        private static final long serialVersionUID = 3020445566627386839L;
        
        private BeanRef target;
        private MethodRef method;
        private List<BeanRegistry.Argument> args;
        private BeanRef result;
        private RemoteBox box;

        public ScenarioInvoke(BeanRef target, MethodRef method,
                              List<BeanRegistry.Argument> args,
                              BeanRef result, RemoteBox box) {
            this.target = target;
            this.method = method;
            this.args = args;
            this.result = result;
            this.box = box;
        }

        @Override
        public void run() {
            try {
                BeanRegistry.getInstance().invoke(target, method, args, result);
            } catch (InvocationTargetException e) {
                box.failure(e.getCause());
                return;
            } catch (Exception e) {
                box.failure(e);
                return;
            }
            box.success();
        }
    }
    
    public static class HookInvoke implements ScriptBean, Serializable { 
        private static final long serialVersionUID = 5414782354115965368L;
        
        private BeanRef target;
        private MethodRef method;
        private List<BeanRegistry.Argument> args;
        private BeanRef result;
        
        public HookInvoke(BeanRef target, MethodRef method,
                          List<BeanRegistry.Argument> args,
                          BeanRef result) {
            this.target = target;
            this.method = method;
            this.args = args;
            this.result = result;
        }

        @Override
        public void create(Box<Void> box) {
            try {
                BeanRegistry.getInstance().invoke(target, method, args, result);
            } catch (InvocationTargetException e) {
                box.setError(e.getCause());
                return;
            } catch (Exception e) {
                box.setError(e);
                return;
            }
            box.setData(null);
        }
        
        @Override
        public Object getRef() {
            return result;
        }
    }
    
    private interface RemoteBox extends Remote {
        void success();
        
        void failure(Throwable e);
    }
    
    private static class LoggingRemoteBox implements RemoteBox {
        private Box<Void> delegate;
        private int counter;
        private String successMsg;
        private String failureMsg;

        public LoggingRemoteBox(Box<Void> delegate, int counter,
                                String successMsg, String failureMsg) {
            this.delegate = delegate;
            this.counter = counter;
            this.successMsg = successMsg;
            this.failureMsg = failureMsg;
        }

        @Override
        public synchronized void success() {
            counter -= 1;
            if (counter == 0 && delegate != null) {
                delegate.setData(null);
                delegate = null;
                log.info(successMsg);
            }
        }

        @Override
        public synchronized void failure(Throwable e) {
            if (delegate != null) {
                delegate.setError(e);
                delegate = null;
                log.info(failureMsg);
            }
        }
    }
    
    protected static String getNodeName() {
        return System.getProperty("vinode.name");
    }
}
