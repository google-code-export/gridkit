package org.gridkit.nimble.statistics.simple;

import java.util.HashMap;
import java.util.Map;

import org.gridkit.nimble.platform.TimeService;
import org.gridkit.nimble.statistics.DelegatingStatsReporter;
import org.gridkit.nimble.statistics.StatsReporter;

public class SimpleStatsReporter extends DelegatingStatsReporter {
    private final TimeService timeService;
    
    private final Map<String, Long> startNanos;
    
    public SimpleStatsReporter(StatsReporter delegate, TimeService timeService) {
        super(delegate);
        this.timeService = timeService;
        this.startNanos = new HashMap<String, Long>();
    }

    public void start(String statistica) {
        report(statistica, timeService.currentTimeMillis());
        startNanos.put(statistica, timeService.currentTimeNanos());
    }
    
    public void finish(String statistica) {
        long finishTime = timeService.currentTimeNanos();
        
        Long startTime = startNanos.get(statistica);
        
        if (statistica != null) {
            report(statistica, startTime, finishTime - startTime);
            startNanos.remove(statistica);
        }
    }
}
