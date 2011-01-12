package org.gridkit.coherence.profile.jmx;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Map;

import org.gridkit.coherence.profile.StatValue;
import org.gridkit.coherence.profile.distributed.ClusterInfoService;
import org.gridkit.coherence.profile.distributed.HistogramService;

/**
 * @author Dmitri Babaev
 */
public class MetricsProvider implements MetricsMxBean {

    public Map<String, StatValue> getMetricStats() {
    	HistogramService ss = ClusterInfoService.getInstance().getHistogramService();
        Map <String, StatValue> map = ss.getAll("*");
        return map;
    }

    public static class MetricStats implements StatValue, Serializable {
        private static final long serialVersionUID = 1;

        private double count;
        private double avg;
        private double total;
        private double stdDev;
        private double max;

        public MetricStats() {
        }

        @ConstructorProperties({"count", "avg", "total", "stdDev", "max"})
        public MetricStats(double count, double avg, double total, double stdDev, double max) {
            this.count = count;
            this.avg = avg;
            this.total = total;
            this.stdDev = stdDev;
            this.max = max;
        }

        public double getCount() {
            return count;
        }

        public double getAvg() {
            return avg;
        }

        public double getTotal() {
            return total;
        }

        public double getStdDev() {
            return stdDev;
        }
        
        public double getMax() {
        	return max;
        }
    }
}
