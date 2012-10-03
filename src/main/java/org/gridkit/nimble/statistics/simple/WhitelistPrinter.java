package org.gridkit.nimble.statistics.simple;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.print.LinePrinter.Contetx;

public class WhitelistPrinter implements SimpleStatsTablePrinter.SimpleStatsLinePrinter {
    @Override
    public void print(SimpleStats stats, Contetx context) {
        for (String valStats : stats.getValStatsNames()) {
            StatisticalSummary aggr = stats.getValStats(valStats);
            
            context.cell(AbstractSimpleStatsLinePrinter.VALUE, valStats);
            context.cell("N",    aggr.getN());
            context.cell("Mean", aggr.getMean());
            context.cell("Sd",   aggr.getStandardDeviation());
            context.cell("Var",  aggr.getVariance());
            context.cell("Min",  aggr.getMin());
            context.cell("Max",  aggr.getMax());
            
            context.newline();
        }
    }
}
