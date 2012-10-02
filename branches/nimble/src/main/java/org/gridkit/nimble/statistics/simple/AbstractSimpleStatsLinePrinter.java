package org.gridkit.nimble.statistics.simple;

import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.print.LinePrinter;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public abstract class AbstractSimpleStatsLinePrinter implements SimpleStatsTablePrinter.SimpleStatsLinePrinter {
    private Predicate<String> statsPredicate = Predicates.alwaysTrue();
    
    protected abstract Set<String> getMarks();
    
    protected abstract void print(Map<String, StatisticalSummary> stats, LinePrinter.Contetx context);
    
    @Override
    public void print(SimpleStats stats, LinePrinter.Contetx context) {
        
    }
}