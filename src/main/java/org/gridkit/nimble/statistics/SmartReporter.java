package org.gridkit.nimble.statistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.gridkit.nimble.platform.TimeService;

@SuppressWarnings("serial")
public class SmartReporter extends DelegatingStatsReporter {    
    public static final String START_MS_MARK  = "start_ms";
    public static final String FINISH_MS_MARK = "finish_ms";
    public static final String TIME_NS_MARK   = "time_ns";
    public static final String OPS_MARK       = "ops";

    private static final String START_NS_MARK  = "start_ns";
    
    private final TimeService timeService;

    private final Map<String, Map<String, Object>> attrsMap;
    
    public SmartReporter(StatsReporter delegate, TimeService timeService) {
        super(delegate);
        this.timeService = timeService;
        this.attrsMap = new HashMap<String, Map<String, Object>>();
    }

    public void start(String statistica) {
        Map<String, Object> attrs = getAttrs(statistica);
        
        attrs.put(START_MS_MARK, timeService.currentTimeMillis());
        attrs.put(START_NS_MARK, timeService.currentTimeNanos());
    }
    
    public void describe(String statistica, String attr, Object value) {
        getAttrs(statistica).put(attr, value);
    }
    
    public void describe(String statistica, Map<String, Object> attrs) {
        getAttrs(statistica).putAll(attrs);
    }
    
    public void operations(String statistica, int count) {
        getAttrs(statistica).put(OPS_MARK, count);
    }
    
	public void finish(String statistica) {
        try {
            long finishNs = timeService.currentTimeNanos();
            long finishMs = timeService.currentTimeMillis();

            Map<String, Object> attrs = getAttrs(statistica);
            
            Long startMs = (Long)attrs.get(START_MS_MARK);
            Long startNs = (Long)attrs.get(START_NS_MARK);

            if (startNs != null && startMs != null) {
                if (!attrs.containsKey(OPS_MARK)) {
                    attrs.put(OPS_MARK, 1);
                }
                
                attrs.put(FINISH_MS_MARK, finishMs);
                attrs.put(TIME_NS_MARK, finishNs - startNs);
                attrs.remove(START_NS_MARK);
                
                report(statistica, attrs);
            } else if (startNs != null || startMs != null) {
                throw new IllegalStateException("startTimeNanos and startTimeMillis are unsync");
            }
        } finally {
            removeAttrs(statistica);
        }
    }
	
	public void report(String statistica, Map<String, Object> attrs) {
	    Map<String, Object> report = new HashMap<String, Object>();
	    
	    for (Map.Entry<String, Object> entry : attrs.entrySet()) {
	        report.put(StatsOps.mark(statistica, entry.getKey()), entry.getValue());
	    }
	    
	    getDelegate().report(report);
	}
	
	public void latency(String statistica, double latency, TimeUnit unit, Map<String, Object> attrs) {
	    Map<String, Object> report = new HashMap<String, Object>(attrs);
	    
	    report.put(StatsOps.mark(statistica, TIME_NS_MARK), StatsOps.convert(latency, unit, TimeUnit.NANOSECONDS));
	    
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
