package org.gridkit.lab.orchestration;

import java.io.Serializable;

public class Argument<T> implements Serializable {
    private static final long serialVersionUID = -2268168066319319324L;
    
    private final T ref;
    private final Object val;
    
    private Argument(T ref, Object val) {
        this.ref = ref;
        this.val = val;
    }
    
    public static <T> Argument<T> newRef(T ref) {
        return new Argument<T>(ref, null);
    }
    
    public static <T> Argument<T> newVal(Object val) {
        return new Argument<T>(null, val);
    }
    
    public boolean isRef() {
        return ref != null;
    }
    
    public boolean isVal() {
        return ref == null;
    }
    
    public T getRef() {
        return ref;
    }
    
    public Object getVal() {
        return val;
    }
}