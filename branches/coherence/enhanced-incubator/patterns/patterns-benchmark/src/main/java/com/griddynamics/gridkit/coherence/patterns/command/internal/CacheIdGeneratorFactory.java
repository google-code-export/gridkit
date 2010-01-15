package com.griddynamics.gridkit.coherence.patterns.command.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.oracle.coherence.patterns.command.internal.GenericThreadFactory;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Binary;
import com.tangosol.util.InvocableMap.Entry;
import com.tangosol.util.InvocableMap.EntryProcessor;
import com.tangosol.util.processor.AbstractProcessor;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class CacheIdGeneratorFactory {

    private final static Executor EXECUTOR = Executors.newFixedThreadPool(1, new GenericThreadFactory("CacheIdGenerator:Daemon", true));     
    public final static String DEFAULT_ID_CACHE = "default-id-generator";  
    public final static Object COUNTER_KEY = new Binary("COUNTER_KEY".getBytes());  
    public final static int DEFAULT_PREFEATCH = 256;  
    
    public static IdGenerator<Integer> createIntGenerator(NamedCache counterCache, NamedCache checkCache) {
        return new IntegerIdGenerator(counterCache, COUNTER_KEY, DEFAULT_PREFEATCH, checkCache);
    }

    public static IdGenerator<Integer> createIntGenerator(Object counterKey, NamedCache checkCache) {
        return new IntegerIdGenerator(CacheFactory.getCache(DEFAULT_ID_CACHE), counterKey, DEFAULT_PREFEATCH, checkCache);
    }
    
    public static IdGenerator<Integer> createIntGenerator(NamedCache counterCache, Object counterKey, int prefetchSize, NamedCache checkCache) {
        return new IntegerIdGenerator(counterCache, counterKey, prefetchSize, checkCache);
    }

    public static IdGenerator<Long> createLongGenerator(NamedCache counterCache, NamedCache checkCache) {
        return new LongIdGenerator(counterCache, COUNTER_KEY, DEFAULT_PREFEATCH, checkCache);
    }

    public static IdGenerator<Long> createLongGenerator(NamedCache counterCache, Object counterKey, int prefetchSize, NamedCache checkCache) {
        return new LongIdGenerator(counterCache, counterKey, prefetchSize, checkCache);
    }

    public static IdGenerator<Integer> createIntSequence(Object counterKey) {
        return new IntegerIdSequence(CacheFactory.getCache(DEFAULT_ID_CACHE), counterKey);
    }

    public static IdGenerator<Integer> createIntSequence(NamedCache counterCache, Object counterKey) {
        return new IntegerIdSequence(counterCache, counterKey);
    }

    public static IdGenerator<Integer> createLongSequence(Object counterKey) {
        return new IntegerIdSequence(CacheFactory.getCache(DEFAULT_ID_CACHE), counterKey);
    }

    public static IdGenerator<Integer> createLongSequence(NamedCache counterCache, Object counterKey) {
        return new IntegerIdSequence(counterCache, counterKey);
    }
    
    
    private static abstract class AbstractGenerator<T> implements IdGenerator<T> {
        
        private final NamedCache counterCache;
        private final Object counterKey;
        private final int prefetch;
        private final NamedCache checkCache;
        private final BlockingQueue<T> freeList;
        
        public AbstractGenerator(NamedCache counterCache, Object counterKey, int prefetch, NamedCache checkCache) {
            this.counterCache = counterCache;
            this.counterKey = counterKey;
            this.prefetch = prefetch;
            this.checkCache = checkCache;
            this.freeList = new ArrayBlockingQueue<T>(2 * prefetch);            
            prefetch();            
        }

        private void prefetch() {
            if (freeList.size() < prefetch >> 1) {
                EXECUTOR.execute(new Runnable() {            
                    @Override
                    public void run() {
                        allocate();
                
                    }
                });
            }
        }

        @Override
        public T nextId() {
            prefetch();            
            try {
                return freeList.take();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted", e);
            }
        }

        private synchronized void allocate() {
            while (freeList.size() < prefetch >> 1) {
                EntryProcessor proc = createAllocateProc(prefetch);
                Object[] range = (Object[]) counterCache.invoke(counterKey, proc);
                List<T> ids = rangeToList(range);
                
                if (checkCache != null) {
                    // availability check
                    Map<T, Object> existing = AnyType.cast(checkCache.getAll(ids));
                    existing.values().removeAll(Collections.singleton(null));
                    ids.removeAll(existing.keySet());
                }
                
                try {
                    freeList.addAll(ids);
                }
                catch(IllegalStateException e) {
                    // queue is full
                }                
            }            
        }
        

        protected abstract EntryProcessor createAllocateProc(int range);
        
        protected abstract List<T> rangeToList(Object[] range);
    }

    private static abstract class AbstractSequence<T> implements IdGenerator<T> {
        
        private final NamedCache counterCache;
        private final Object counterKey;
        
        public AbstractSequence(NamedCache counterCache, Object counterKey) {
            this.counterCache = counterCache;
            this.counterKey = counterKey;
        }
        
        @Override
        public T nextId() {
            EntryProcessor proc = createAllocateProc(1);
            Object[] range = (Object[]) counterCache.invoke(counterKey, proc);
            return AnyType.<T>cast(range[0]);            
        }
        
        protected abstract EntryProcessor createAllocateProc(int range);
    }
    
    private static class LongIdGenerator extends AbstractGenerator<Long> {

        public LongIdGenerator(NamedCache counterCache, Object counterKey, int prefetch, NamedCache checkCache) {
            super(counterCache, counterKey, prefetch, checkCache);
        }

        @Override
        protected EntryProcessor createAllocateProc(int range) {
            return new LongIdAllocator(range);
        }

        @Override
        protected List<Long> rangeToList(Object[] range) {
            long l = (Long) range[0];
            long h = (Long) range[1];
            List<Long> list = new ArrayList<Long>((int)(h - l));
            for(long i = l; i < h; ++i) {
                list.add(Long.valueOf(i));
            }
            return list;
        }
    }

    private static class IntegerIdGenerator extends AbstractGenerator<Integer> {
        
        public IntegerIdGenerator(NamedCache counterCache, Object counterKey, int prefetch, NamedCache checkCache) {
            super(counterCache, counterKey, prefetch, checkCache);
        }
        
        @Override
        protected EntryProcessor createAllocateProc(int range) {
            return new IntegerIdAllocator(range);
        }
        
        @Override
        protected List<Integer> rangeToList(Object[] range) {
            int l = (Integer) range[0];
            int h = (Integer) range[1];
            List<Integer> list = new ArrayList<Integer>((int)(h - l));
            for(int i = l; i < h; ++i) {
                list.add(Integer.valueOf(i));
            }
            return list;
        }
    }
    
    private static class LongIdSequence extends AbstractSequence<Long> {
        
        public LongIdSequence(NamedCache counterCache, Object counterKey) {
            super(counterCache, counterKey);
        }
        
        @Override
        protected EntryProcessor createAllocateProc(int range) {
            return new LongIdAllocator(range);
        }
    }
    
    private static class IntegerIdSequence extends AbstractSequence<Integer> {
        
        public IntegerIdSequence(NamedCache counterCache, Object counterKey) {
            super(counterCache, counterKey);
        }
        
        @Override
        protected EntryProcessor createAllocateProc(int range) {
            return new IntegerIdAllocator(range);
        }        
    }
    
    public static class LongIdAllocator extends AbstractProcessor {

        private static final long serialVersionUID = 20090721L;
        
        int range;
        
        LongIdAllocator() {
            // for serializar
        }
        
        public LongIdAllocator(int range) {
            this.range = range;
        }

        @Override
        public Object process(Entry entry) {
            if (entry.isPresent()) {
                Long current = (Long) entry.getValue();
                current = current == null ? 0 : current; 
                Long newVal = current + range;
                entry.setValue(newVal, false);
                return new Long[]{current, newVal};
            }
            else {
                entry.setValue(Long.valueOf(range), false);
                return new Long[]{Long.valueOf(0), Long.valueOf(range)};
            }
        }        
    }

    public static class IntegerIdAllocator extends AbstractProcessor {
        
        private static final long serialVersionUID = 20090721L;
        
        int range;
        
        IntegerIdAllocator() {
            // for serializar
        }
        
        public IntegerIdAllocator(int range) {
            this.range = range;
        }
        
        @Override
        public Object process(Entry entry) {
            if (entry.isPresent()) {
                Integer current = (Integer) entry.getValue();
                current = current == null ? 0 : current; 
                Integer newVal = current + range;
                entry.setValue(newVal, false);
                return new Integer[]{current, newVal};
            }
            else {
                entry.setValue(Integer.valueOf(range), false);
                return new Integer[]{Integer.valueOf(0), Integer.valueOf(range)};
            }
        }        
    }    
}
