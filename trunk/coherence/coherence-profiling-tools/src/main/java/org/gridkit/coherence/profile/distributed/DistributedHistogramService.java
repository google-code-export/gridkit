/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.profile.distributed;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.gridkit.coherence.profile.Histogram;
import org.gridkit.coherence.profile.Sampler;
import org.gridkit.coherence.profile.StatValue;
import org.gridkit.coherence.profile.runtime.RuntimeStats;


/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class DistributedHistogramService implements HistogramService {

    private static final String PROP_PREFIX = "histo.";
    private ClusterInfoService service;
    private Thread updater;
    private Map<String, Histogram> localCounters = new HashMap<String, Histogram>();
    private ConcurrentMap<String, AsyncSampler> samplers = new ConcurrentHashMap<String, AsyncSampler>();
    
    public DistributedHistogramService(ClusterInfoService service) {
        this.service = service;
        updater = new Thread(){
            @Override
            public void run() {
                while(true) {
                    if (RuntimeStats.ENABLED) {
                        updateStats();
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            }
        };
        updater.setName("DistributedHistogramService:Daemon");
        updater.setDaemon(true);
        updater.start();
    }
    
    void updateStats() {
        for(Map.Entry<String, AsyncSampler> entry: samplers.entrySet()) {
            String key = entry.getKey();
            AsyncSampler sampler = entry.getValue();
            if (!sampler.isEmpty()) {
                Histogram hist;
                synchronized(this) {
                    hist = localCounters.get(key);
                }
                while(true) {
                    Long sample = sampler.poll();
                    if (sample != null) {
                        hist.addSample(sample);
                    } 
                    else {
                        break;
                    }
                }
                Histogram published = (Histogram) service.getLocalProperty(PROP_PREFIX + key);
                if (published == null) {
                    published = hist;
                }
                else {
                    published.addHistogram(hist);
                }
                service.putProperty(PROP_PREFIX + key, published);
                hist.reset();
            }
        }        
    }

    @Override
    public synchronized Sampler defineSampler(String name, long scale, long min, long max, int size) {
        Histogram hist = new Histogram(scale, min, max, size);
        Histogram existing = localCounters.get(name);
        if (existing != null) {
            hist.addHistogram(existing);
        }
        localCounters.put(name, hist);
        
        AsyncSampler sampler = samplers.get(name);
        if (sampler == null) {
        	sampler = new AsyncSampler();
        	samplers.put(name, sampler);
        }
        
        return sampler;
    }

    @Override
    public StatValue getValue(String key) {
        return aggregateHistogram(key);
    }
    
    @Override
    public void resetValue(String name) {
//        synchronized(this) {
//            Histogram hist = localCounters.get(name);
//            if (hist != null) {
//                hist = hist.clone();
//                hist.reset();
//                localCounters.put(name, hist);
//                AsyncSampler sampler = samplers.get(name);
//                if (sampler != null) {
//                    sampler.clear();
//                }
//            }
//        }
        service.eraseProperty(PROP_PREFIX + name);        
    }

    @Override
    public Map<String, StatValue> getAll(String pattern) {
        Set<String> keys = service.listProperties(PROP_PREFIX + pattern);
        Map<String, StatValue> result = new HashMap<String, StatValue>();
        for(String key: keys) {
            key = key.substring(PROP_PREFIX.length());
            result.put(key, getValue(key));
        }
        return result;
    }
    
    @Override
    public void resetAll(String pattern) {
        service.eraseAllProperties(PROP_PREFIX + pattern);
//        Set<String> keys = service.listProperties(PROP_PREFIX + pattern);
//        for(String key: keys) {
//            resetValue(key);
//        }
    }

    private StatValue aggregateHistogram(String key) {
        Histogram total = null;
        for(Object value: service.getProperty(PROP_PREFIX + key, false).values()) {
            Histogram hist = (Histogram) value;
            if (total == null) {
                total = hist;
            }
            else {
                total.addHistogram(hist);
            }
        }
        
        if (total != null) {
            total.updateStats();
        }
        
        return total;
    }
    
    private static class AsyncSampler extends ConcurrentLinkedQueue<Long> implements Sampler {

        private static final long serialVersionUID = 20090717L;

        public AsyncSampler() {
        }

        @Override
        public void addSample(long value) {
            if (RuntimeStats.ENABLED) {
                add(value);
            }
        }
    }


}
