package org.gridkit.nimble;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class StatsTests {
    public static void main(String[] args) throws Exception {
        SummaryStatistics s1 = new SummaryStatistics();
        
        System.out.println(s1.getSecondMoment());
    }
}
