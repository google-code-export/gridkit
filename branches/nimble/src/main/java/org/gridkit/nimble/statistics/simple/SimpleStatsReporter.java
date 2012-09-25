package org.gridkit.nimble.statistics.simple;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.gridkit.nimble.platform.TimeService;
import org.gridkit.nimble.statistics.DelegatingStatsReporter;
import org.gridkit.nimble.statistics.StatsOps;
import org.gridkit.nimble.statistics.StatsReporter;

public class SimpleStatsReporter extends DelegatingStatsReporter {
    private final TimeService timeService;
    
    private static final String START_NS_MARK = "start_ns";
    
    private final Map<String, Map<String, Object>> attrsMap;
    
    public SimpleStatsReporter(StatsReporter delegate, TimeService timeService) {
        super(delegate);
        this.timeService = timeService;
        this.attrsMap = new HashMap<String, Map<String, Object>>();
    }

    public void start(String statistica) {
        Map<String, Object> attrs = getAttrs(statistica);
        
        attrs.put(SimpleStats.START_MS_MARK, timeService.currentTimeMillis());
        attrs.put(START_NS_MARK, timeService.currentTimeNanos());
    }
    
    public void describe(String statistica, String attr, Object value) {
        getAttrs(statistica).put(attr, value);
    }
    
    public void describe(String statistica, Map<String, Object> attrs) {
        getAttrs(statistica).putAll(attrs);
    }
    
    public void operations(String statistica, int count) {
        getAttrs(statistica).put(SimpleStats.OPS_MARK, count);
    }
    
	public void finish(String statistica) {
        try {
            long finishNs = timeService.currentTimeNanos();
            long finishMs = timeService.currentTimeMillis();

            Map<String, Object> attrs = getAttrs(statistica);
            
            Long startMs = (Long)attrs.get(SimpleStats.START_MS_MARK);
            Long startNs = (Long)attrs.get(START_NS_MARK);

            if (startNs != null && startMs != null) {
                if (!attrs.containsKey(SimpleStats.OPS_MARK)) {
                    attrs.put(SimpleStats.OPS_MARK, 1);
                }
                
                attrs.put(SimpleStatsProducer.STATS_NAME_ATTR, statistica);
                attrs.put(SimpleStats.FINISH_MS_MARK, finishMs);
                attrs.put(SimpleStats.TIME_NS_MARK, finishNs - startNs);

                attrs.remove(START_NS_MARK);
                
                getDelegate().report(attrs);
            } else if (startNs != null || startMs != null) {
                throw new IllegalStateException("startTimeNanos and startTimeMillis are unsync");
            }
        } finally {
            removeAttrs(statistica);
        }
    }
	
	public void latency(String statistica, double latency, TimeUnit unit, Map<String, Object> attrs) {
	    Map<String, Object> report = new HashMap<String, Object>(attrs);
	    
	    report.put(SimpleStatsProducer.STATS_NAME_ATTR, statistica);
	    report.put(SimpleStats.TIME_NS_MARK, StatsOps.convert(latency, unit, TimeUnit.NANOSECONDS));
	    
	    getDelegate().report(report);
	}
	
	public void latency(String statistica, double latency, TimeUnit unit) {
	    latency(statistica, latency, unit, Collections.<String, Object>emptyMap());
	}
	
	public void report(String attr, Object value) {
	    getDelegate().report(Collections.singletonMap(attr, value));
	}
	
	private Map<String, Object> getAttrs(String statistica) {
        Map<String, Object> statAttrs = attrsMap.get(statistica);
        
        if (statAttrs == null) {
            statAttrs = new HashMap<String, Object>();
            attrsMap.put(statistica, statAttrs);
        }
        
        return statAttrs;
	}
	
	private void removeAttrs(String statistica) {
	    attrsMap.remove(statistica);
	}
}
