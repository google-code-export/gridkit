package sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.tangosol.net.cache.CacheLoader;

public class EmptyCacheLoader implements CacheLoader {

    public EmptyCacheLoader() {
        new String();
    }
    
    @Override
    public Object load(Object obj) {
        System.out.println("load(" + obj + ") -- " + Thread.currentThread().getName());
        return null;
    }

    @Override
    public Map loadAll(Collection list) {
        System.out.println("loadAll(" + new ArrayList(list) + ")");
        return Collections.EMPTY_MAP;
    }

}
