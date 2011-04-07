package sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.tangosol.net.cache.CacheLoader;

public class SimpleCacheLoader implements CacheLoader {

    public SimpleCacheLoader() {
        new String();
    }
    
    @Override
    public Object load(Object obj) {
        System.out.println("load(" + obj + ") -- " + Thread.currentThread().getName());
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
        return obj;
    }

    @Override
    public Map loadAll(Collection list) {
        System.out.println("loadAll(" + new ArrayList(list) + ")");
        Map m = new HashMap();
        for(Object obj: list) {
            m.put(obj, obj);
        }
        return m;
    }

}
