package org.gridkit.nimble.statistics.simple;

import java.util.HashMap;
import java.util.Map;

import org.gridkit.nimble.platform.TimeService;
import org.gridkit.nimble.statistics.DelegatingStatsReporter;
import org.gridkit.nimble.statistics.StatsReporter;

public class SimpleStatsReporter extends DelegatingStatsReporter {
    private final TimeService timeService;
    
    private final Map<String, Long> startNanos;
    private final Map<String, Long> startMillis; 
    
    public SimpleStatsReporter(StatsReporter delegate, TimeService timeService) {
        super(delegate);
        this.timeService = timeService;
        this.startNanos = new HashMap<String, Long>();
        this.startMillis = new HashMap<String, Long>();
    }

    public void start(String statistica) {
        startNanos.put(statistica, timeService.currentTimeNanos());
        startMillis.put(statistica, timeService.currentTimeMillis());
    }
    
    public void finish(String statistica) {
        long finishTimeNanos = timeService.currentTimeNanos();
        
        Long startTimeNanos = startNanos.get(statistica);
        Long startTimeMillis = startMillis.get(statistica);
        
        if (startTimeNanos != null && startTimeMillis != null) {
            report(statistica, startTimeMillis, finishTimeNanos - startTimeNanos);
            startNanos.remove(statistica);
            startMillis.remove(statistica);
        }
    }
}
