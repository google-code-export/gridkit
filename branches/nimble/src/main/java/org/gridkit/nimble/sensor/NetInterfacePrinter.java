package org.gridkit.nimble.sensor;

import java.util.Map;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.gridkit.nimble.print.LinePrinter.Contetx;
import org.gridkit.nimble.statistics.simple.AbstractSimpleStatsLinePrinter;

public class NetInterfacePrinter extends AbstractSimpleStatsLinePrinter {
    @Override
    protected String getSamplerName() {
        return null;
    }

    @Override
    protected void print(Map<String, StatisticalSummary> aggregates, Contetx context) {
        
    }
}
