package org.gridkit.lab.orchestration;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanRegistry {
    private static BeanRegistry INSTANCE = new BeanRegistry();
    
    public static BeanRegistry getInstance() {
        return INSTANCE;
    }
    
    private Map<BeanRef, Object> beanMap = new HashMap<BeanRef, Object>();

    public void invoke(BeanRef targetRef, MethodRef method, List<Argument> args, BeanRef resultRef) throws InvocationTargetException {
        Object targetBean = bean(targetRef);
        Object[] targetArgs = resolve(args);
        
        Object resultBean;
        try {
            resultBean = method.invoke(targetBean, targetArgs);
        } catch (InvocationTargetException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        }
        
        register(resultRef, resultBean);
    }
    
    public void deploy(BeanRef reg, Object bean) {
        register(reg, bean);
    }
        
    public synchronized void register(BeanRef ref, Object bean) {
        if (beanMap.containsKey(ref)) {
            throw new IllegalStateException();
        }
        beanMap.put(ref, bean);
    }
    
    private synchronized Object[] resolve(List<Argument> args) {
        Object[] result = new Object[args.size()];
        
        for (int i = 0; i < args.size(); ++i) {
            Argument arg = args.get(i);
            
            if (arg.isRef()) {
                result[i] = bean(arg.getRef());
            } else {
                result[i] = arg.getVal();
            }
        }
        
        return result;
    }
    
    private synchronized Object bean(BeanRef ref) {
        if (beanMap.containsKey(ref)) {
            return beanMap.get(ref);
        } else {
            throw new IllegalArgumentException("bean not found");
        }
    }
    
    public static class Argument implements Serializable {
        private static final long serialVersionUID = -2268168066319319324L;
        
        private final BeanRef ref;
        private final Object val;
        
        private Argument(BeanRef ref, Object val) {
            this.ref = ref;
            this.val = val;
        }
        
        public static Argument newRef(BeanRef ref) {
            return new Argument(ref, null);
        }
        
        public static Argument newVal(Object val) {
            return new Argument(null, val);
        }
        
        public boolean isRef() {
            return ref != null;
        }
        
        public boolean isVal() {
            return ref == null;
        }
        
        public BeanRef getRef() {
            return ref;
        }
        
        public Object getVal() {
            return val;
        }
    }
    
    public static class BeanRef implements Serializable {
        private static final long serialVersionUID = 8842370997348559951L;
        
        private final String Id;

        public BeanRef(String id) {
            Id = id;
        }
        
        public String getId() {
            return Id;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((Id == null) ? 0 : Id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            BeanRef other = (BeanRef) obj;
            if (Id == null) {
                if (other.Id != null)
                    return false;
            } else if (!Id.equals(other.Id))
                return false;
            return true;
        }
        
        @Override
        public String toString() {
            return "BeanRef[" + Id + "]";
        }
    }
}
