package org.gridkit.nimble.sensor;

import java.util.Map;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.print.LinePrinter.Contetx;
import org.gridkit.nimble.statistics.simple.AbstractSimpleStatsLinePrinter;

public class ProcCpuPrinter extends AbstractSimpleStatsLinePrinter {
    @Override
    protected String getSamplerName() {
        return ProcCpuReporter.SAMPLER_NAME;
    }

    @Override
    protected void print(Map<String, StatisticalSummary> aggregates, Contetx context) {
        StatisticalSummary usr = aggregates.get(ProcCpuReporter.USR);
        StatisticalSummary sys = aggregates.get(ProcCpuReporter.SYS);
        StatisticalSummary tot = aggregates.get(ProcCpuReporter.TOT);
        
        StatisticalSummary time = aggregates.get(ProcCpuReporter.MS);
        
        StatisticalSummary cnt = aggregates.get(ProcCpuReporter.CNT);
        
        if (time != null) {
            context.cell("Usr", usr.getSum() / time.getSum());
            context.cell("Sys", sys.getSum() / time.getSum());
            context.cell("Tot", tot.getSum() / time.getSum());
        }
        
        if (cnt != null) {
            context.cell("# of Proc", cnt.getMean());
        }
        
        if (time != null) {
            context.cell("# of Mesures", time.getN());
        }
        
        context.newline();
    }
}
