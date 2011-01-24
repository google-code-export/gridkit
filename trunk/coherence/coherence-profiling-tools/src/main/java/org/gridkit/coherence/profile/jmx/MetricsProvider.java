package org.gridkit.coherence.profile.jmx;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Map;

import org.gridkit.coherence.profile.StatValue;
import org.gridkit.coherence.profile.distributed.ClusterInfoService;
import org.gridkit.coherence.profile.distributed.HistogramService;
import org.gridkit.coherence.profile.runtime.RuntimeStats;
import org.gridkit.coherence.profile.utils.JvmId;

import com.tangosol.net.AbstractInvocable;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;

/**
 * @author Dmitri Babaev
 */
public class MetricsProvider implements MetricsMxBean {

	@Override
    public Map<String, StatValue> getMetricStats() {
    	HistogramService ss = ClusterInfoService.getInstance().getHistogramService();
        Map <String, StatValue> map = ss.getAll("*");
        return map;
    }
    
    @Override
    public void resetByRegExPattern(String regexPattern) {
    	ClusterInfoService.getInstance().getHistogramService().resetAll(regexPattern);
    }
    
    @Override
    public void resetMetric(String name) {
    	ClusterInfoService.getInstance().getHistogramService().resetValue(name);
    }
    
    @Override
    public void disableProfiling() {
    	InvocationService service = (InvocationService) CacheFactory.getService("ClusterInvocationService");
        service.query(new StatsOn(false), service.getCluster().getMemberSet());
    }
    
    @Override
    public void enableProfiling() {
    	InvocationService service = (InvocationService) CacheFactory.getService("ClusterInvocationService");
        service.query(new StatsOn(true), service.getCluster().getMemberSet());
    }
    
    public static class StatsOn extends AbstractInvocable {

        private static final long serialVersionUID = 20090724L;
        
        private boolean enable;
        
        protected StatsOn() {
            // for serialization
        }

        public StatsOn(boolean enable) {
            this.enable = enable;
        }
        
        @Override
        public void run() {
            RuntimeStats.ENABLED = enable;
            setResult(JvmId.JVM_ID);
        }
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
