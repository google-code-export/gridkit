package org.gridkit.lab.orchestration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gridkit.lab.orchestration.BeanProxy.Handler;
import org.gridkit.lab.orchestration.Platform.ScriptConstructor;
import org.gridkit.lab.orchestration.script.ScriptAction;
import org.gridkit.lab.orchestration.script.ScriptExecutor.Box;
import org.gridkit.lab.orchestration.util.ClassOps;
import org.gridkit.lab.orchestration.util.NamedThreadFactory;

public class LocalBean {    
    private static ExecutorService EXECUTOR = Executors.newCachedThreadPool(
        new NamedThreadFactory("Local-Bean-Executor", true)
    );
    
    private SourceRef ref;
    private Cell cell;
    private Class<?> clazz;
    
    public LocalBean(SourceRef ref, Cell cell, Class<?> clazz) {
        this.ref = ref;
        this.cell = cell;
        this.clazz = clazz;
    }

    public LocalBean(Object prototype, StackTraceElement location) {
        this.ref = new SourceRef("local()", location);
        this.cell = new Cell(prototype);
        this.clazz = prototype.getClass();
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Platform platform) {
        return (T)BeanProxy.newInstance(clazz, new ProxyHandler(this, platform));
    }
    
    private static class Invoke implements ScriptAction, ExecutableAction {
        private LocalBean target;
        private Method method;
        private List<Argument<LocalBean>> args;
        private LocalBean result;
        
        public Invoke(LocalBean target, Method method, List<Argument<LocalBean>> args, LocalBean result) {
            this.target = target;
            this.method = method;
            this.args = args;
            this.result = result;
        }

        public void execute(Box box) {
            EXECUTOR.submit(new InvokeExecutor(this, box));
        }
        
        public void execute() throws Throwable {
            Object[] args = getArgs();
            
            Object value;
            try {
                value = method.invoke(target.cell.getValue(), args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
            
            result.cell.setValue(value);
        }
        
        private Object[] getArgs() {
            Object[] result = new Object[args.size()];
            
            for (int i = 0; i < args.size(); ++i) {
                Argument<LocalBean> arg = args.get(i);
                
                if (arg.isRef()) {
                    result[i] = arg.getRef().cell.getValue();
                } else {
                    result[i] = arg.getVal();
                }
            }
            
            return result;
        }
        
        public String getId() {
            return result.ref.getId();
        }
        
        public String toString() {
            return result.ref.toString();
        }
    }
    
    private static class InvokeExecutor implements Runnable {
        private LocalBean.Invoke invoke;
        private Box box;
        
        public InvokeExecutor(Invoke invoke, Box box) {
            this.invoke = invoke;
            this.box = box;
        }

        public void run() {
            try {
                invoke.execute();
            } catch (Throwable e) {
                box.failure(e);
                return;
            }
            box.success();
        }
    }
    
    private static class ProxyHandler implements Handler {
        private Platform platform;
        private LocalBean bean;

        public ProxyHandler(LocalBean bean, Platform platform) {
            this.platform = platform;
            this.bean = bean;
        }

        @Override
        public Object invoke(Method method, List<Argument<Handler>> rawArgs) throws Throwable {
            validate(rawArgs);
            
            SourceRef ref = new SourceRef(ClassOps.toString(method), ClassOps.location(3));
            
            LocalBean result = new LocalBean(ref, new Cell(), method.getReturnType());
            
            List<Argument<LocalBean>> args = getArgs(rawArgs);
            
            LocalBean.Invoke invoke = new LocalBean.Invoke(bean, method, args, result);
            
            ScriptConstructor constructor = platform.getScriptConstructor();
            
            if (constructor == null) {
                invoke.execute();
            } else {
                constructor.getScriptBuilder().action(invoke, getRefs(args));
            }
            
            return result.getProxy(platform);
        }
        
        private static List<String> getRefs(List<Argument<LocalBean>> args) {
            List<String> refs = new ArrayList<String>();
            
            for (Argument<LocalBean> arg : args) {
                if (arg.isRef()) {
                    refs.add(arg.getRef().ref.getId());
                }
            }
            
            return refs;
        }
        
        private static List<Argument<LocalBean>> getArgs(List<Argument<Handler>> args) {
            List<Argument<LocalBean>> result = new ArrayList<Argument<LocalBean>>(args.size());
            
            for (Argument<Handler> arg : args) {
                if (arg.isRef()) {
                    LocalBean bean = ((ProxyHandler)arg.getRef()).bean;
                    result.add(Argument.newRef(bean));
                } else {
                    result.add(Argument.<LocalBean>newVal(arg.getVal()));
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
            }
        }
    }
    
    private static class Cell {
        private Object value;
        private boolean empty;
        
        public Cell(Object value) {
            this.value = value;
            this.empty = false;
        }

        public Cell() {
            this.value = null;
            this.empty = true;
        }

        public synchronized void setValue(Object value) {
            if (!empty) {
                throw new IllegalStateException("cell is not empty");
            }
            this.value = value;
            this.empty = false;
        }
        
        public synchronized Object getValue() {
            if (empty) {
                throw new IllegalStateException("cell is empty");
            }
            return value;
        }
    }
}
