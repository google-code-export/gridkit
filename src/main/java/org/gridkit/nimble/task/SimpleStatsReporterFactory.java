package org.gridkit.nimble.task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.gridkit.nimble.statistics.FlushableStatsReporter;
import org.gridkit.nimble.statistics.simple.QueuedSimpleStatsAggregator;
import org.gridkit.nimble.statistics.simple.SimpleStatsAggregator;
import org.gridkit.nimble.statistics.simple.SimpleStatsProducer;

@SuppressWarnings("serial")
public class SimpleStatsReporterFactory implements TaskScenario.StatsReporterFactory, Serializable {
    private SimpleStatsAggregator globalAggregator;
    private transient SimpleStatsAggregator taskAggregator;
    
    public SimpleStatsReporterFactory(SimpleStatsAggregator globalAggregator) {
        this.globalAggregator = globalAggregator;
        this.taskAggregator = new QueuedSimpleStatsAggregator();
    }
    
    @Override
    public FlushableStatsReporter newTaskReporter() {
        return SimpleStatsProducer.newInstance(taskAggregator);
    }

    @Override
    public void flush() {
        globalAggregator.add(taskAggregator.calculate());
    }
        
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.taskAggregator = new QueuedSimpleStatsAggregator();
    }
}
