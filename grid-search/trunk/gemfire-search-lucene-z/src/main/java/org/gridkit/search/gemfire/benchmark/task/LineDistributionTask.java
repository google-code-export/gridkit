package org.gridkit.search.gemfire.benchmark.task;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gridkit.search.gemfire.benchmark.model.Commitment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

public abstract class LineDistributionTask extends BenchmarkTask {
    private static Logger log = LoggerFactory.getLogger(LineDistributionTask.class);
    
    protected Iterator<Map.Entry<String, Map<String, Integer>>> deps;
    protected Iterator<Map.Entry<String, Integer>> lines;
    
    protected String dep;
    protected String line;
    protected Integer count;
    
    private Stopwatch overallSw = new Stopwatch();
    
    @Override
    public void reset() {
        super.reset();
        
        deps = ftsData.getLineDistribution().entrySet().iterator();
        lines = Collections.<Map.Entry<String, Integer>>emptySet().iterator();
    }
    
    @Override
    public boolean execute() throws Exception {
        if (!deps.hasNext() && !lines.hasNext())
            return false;
        
        if (!lines.hasNext()) {
            Map.Entry<String, Map<String, Integer>> nextDep = deps.next();
            dep = nextDep.getKey();
            lines = nextDep.getValue().entrySet().iterator();
        }
        
        Map.Entry<String, Integer> nextLine = lines.next();
        line = nextLine.getKey();
        count = nextLine.getValue();
        
        overallSw.start();
        Collection<Commitment> commitments = getCommitments(dep, line);
        overallSw.stop();
        
        if (commitments.size() != count)
            log.warn(String.format("Query error for '%s' - '%s'", dep, line));
        
        return true;
    }
    
    @Override
    public void record() {
        super.record();
        
        DescriptiveStatistics overallSt = getStatistics("overall", count);
        overallSt.addValue(overallSw.elapsedTime(TimeUnit.MICROSECONDS));
        overallSw.reset();
    }
    
    protected abstract Collection<Commitment> getCommitments(String dep, String line) throws Exception;
}
