package org.gridkit.lab.orchestration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanRegistry {
    private static BeanRegistry INSTANCE = new BeanRegistry();
    
    public static BeanRegistry getInstance() {
        return INSTANCE;
    }
    
    private Map<SourceRef, Object> beanMap = new HashMap<SourceRef, Object>();

    public void invoke(SourceRef targetRef, MethodRef method, List<Argument<SourceRef>> args, SourceRef resultRef) throws Exception {
        Object targetBean = bean(targetRef);
        Object[] targetArgs = resolve(args);
        Object resultBean = method.invoke(targetBean, targetArgs);
        register(resultRef, resultBean);
    }
    
    public void deploy(SourceRef reg, Object bean) {
        register(reg, bean);
    }
        
    public synchronized void register(SourceRef ref, Object bean) {
        if (beanMap.containsKey(ref)) {
            throw new IllegalStateException("bean '" + ref + "' already exists");
        }
        beanMap.put(ref, bean);
    }
    
    private synchronized Object[] resolve(List<Argument<SourceRef>> args) {
        Object[] result = new Object[args.size()];
        
        for (int i = 0; i < args.size(); ++i) {
            Argument<SourceRef> arg = args.get(i);
            
            if (arg.isRef()) {
                result[i] = bean(arg.getRef());
            } else {
                result[i] = arg.getVal();
            }
        }
        
        return result;
    }
    
    private synchronized Object bean(Object ref) {
        if (beanMap.containsKey(ref)) {
            return beanMap.get(ref);
        } else {
            throw new IllegalArgumentException("bean '" + ref + "' not found");
        }
    }
}
