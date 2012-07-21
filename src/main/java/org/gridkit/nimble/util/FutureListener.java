package org.gridkit.nimble.util;

public interface FutureListener<V> {
    void onSuccess(V result);

    void onFailure(Throwable t, boolean afterSuccess, boolean afterCancel);
    
    void onCancel();
}
