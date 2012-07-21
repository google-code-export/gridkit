package org.gridkit.nimble.task;

import org.slf4j.Logger;

import org.gridkit.nimble.platform.AttributeContext;
import org.gridkit.nimble.platform.TimeService;
import org.gridkit.nimble.statistics.StatsReporter;

public interface Task {
    String getName();
    
    void excute(Context context) throws Exception;
    
    public interface Context extends TimeService, AttributeContext {
        StatsReporter getStatReporter();
        
        Logger getLogger();
        
        void setFailure();
    }
}
