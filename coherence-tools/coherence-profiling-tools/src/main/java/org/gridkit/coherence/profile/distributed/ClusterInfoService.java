/**
 * Copyright 2009 Grid Dynamics Consulting Services, Inc.
 */
package org.gridkit.coherence.profile.distributed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gridkit.coherence.profile.utils.AnyType;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;
import com.tangosol.util.UID;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.EqualsFilter;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class ClusterInfoService {

    private static final ReflectionExtractor GET_PROP_NAME = new ReflectionExtractor("getPropName");
    
    private final static ClusterInfoService INSTANCE = new ClusterInfoService();
    
    public static final ClusterInfoService getInstance() {
        return INSTANCE;
    }
        
    private NamedCache cache;
    private HistogramService histogramService;
    
    private synchronized NamedCache getCache() {
    	if (cache == null) {
    		cache = CacheFactory.getCache("distributed.cluster-info");
            cache.addIndex(GET_PROP_NAME, false, null);
    	}
    	return cache;
    }
    
    private UID getUid() {
    	return CacheFactory.getCluster().getLocalMember().getUid();
    }
    
    protected ClusterInfoService() {
    }
    
    public synchronized HistogramService getHistogramService() {
        if (histogramService == null) {
            histogramService = new DistributedHistogramService(this);
        }
        return histogramService;
    }
    
    public Object getLocalProperty(String name) {
        ClusterPropertyKey key = new ClusterPropertyKey(getUid(), name);
        ClusterProperty prop = (ClusterProperty) getCache().get(key);
        return prop == null ? null : prop.getValue();
    }
    
    public Map<UID, Object> getProperty(String name, boolean liveOnly) {
        Set<Map.Entry<ClusterPropertyKey, ClusterProperty>> set = AnyType.cast(getCache().entrySet(new EqualsFilter(GET_PROP_NAME, name)));
        Map<UID, Object> result = new HashMap<UID, Object>();
        Set<UID> liveUid = null;
        if (liveOnly) {
            Set<Member> ms = AnyType.cast(CacheFactory.getCluster().getMemberSet());
            liveUid = new HashSet<UID>();
            for(Member member: ms) {
                liveUid.add(member.getUid());
            }
        }
        for(Map.Entry<ClusterPropertyKey, ClusterProperty> entry: set) {
            ClusterProperty prop = entry.getValue();
            if (!liveOnly || liveUid.contains(prop.getMemberId())) {
                result.put(prop.getMemberId(), prop.getValue());
            }
        }
        
        return result;
    }    

    public void putProperty(String name, Object value) {
        ClusterPropertyKey key = new ClusterPropertyKey(getUid(), name);
        ClusterProperty val = new ClusterProperty(key, value);
        getCache().put(key, val);
    }
    
    @SuppressWarnings("unchecked")
    public void eraseProperty(String name) {
        getCache().keySet().removeAll(getCache().keySet(new EqualsFilter(GET_PROP_NAME, name)));
    }
 
    public Set<String> listProperties(String pattern) {
        Pattern pat = toPattern(pattern);
        Set<String> result = new HashSet<String>();
        try {
            for(ClusterPropertyKey key: AnyType.<Set<ClusterPropertyKey>>cast(getCache().keySet())) {
                Matcher mc = pat.matcher(key.getPropName());
                if (mc.matches()) {
                    result.add(key.getPropName());
                }                
            }
        }
        catch(RuntimeException e) {
            if (e.getMessage().equals("Storage is not configured")) {
                // ignore;
            }
            else {
                throw e;
            }
        }
        
        return result;
    }

    public void eraseAllProperties(String pattern) {
        Pattern pat = toPattern(pattern);
        @SuppressWarnings("unused")
		Set<String> result = new HashSet<String>();
        for(ClusterPropertyKey key: AnyType.<Set<ClusterPropertyKey>>cast(getCache().keySet())) {
            Matcher mc = pat.matcher(key.getPropName());
            if (mc.matches()) {
                eraseProperty(key.getPropName());
            }
        }
    }

    private Pattern toPattern(String pattern) {
        @SuppressWarnings("unused")
		String re = pattern.replaceAll("[.]", "[.]").replaceAll("[*]", ".*").replaceAll("[?]", ".?");
        Pattern pat = Pattern.compile(pattern);
        return pat;
    }
}
