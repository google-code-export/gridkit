package org.gridkit.nimble.statistics;

import java.util.HashMap;
import java.util.Map;

public class StopWatchReporter {
    private final StatsReporter reporter;

    private final Map<String, Long> startedEvents;
    
    public StopWatchReporter(StatsReporter reporter) {
        this.reporter = reporter;
        this.startedEvents = new HashMap<String, Long>();
    }
    
    public void start(String event) {
        startedEvents.put(event, currentTimeMillis());
    }
    
    public void finish(String event) {
        long finishTime = currentTimeMillis();
        
        Long startTime = startedEvents.get(event);
        
        if (startTime != null) {
            reporter.report(event, startTime, finishTime - startTime);
            startedEvents.remove(event);
        }
    }
    
    public void report(String event) {
        reporter.report(event, currentTimeMillis());
    }
    
    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
