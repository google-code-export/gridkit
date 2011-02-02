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
    public void resetMetricByName(String name) {
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

		private double percentile95;
		private double percentile99;
		private double percentile999;

        public MetricStats() {
        }

        @ConstructorProperties({"count", "avg", "total", "stdDev", "max", "percentile95", "percentile99", "percentile999"})
        public MetricStats(double count, double avg, double total, double stdDev, double max,
        		double percentile95, double percentile99, double percentile999)
        {
            this.count = count;
            this.avg = avg;
            this.total = total;
            this.stdDev = stdDev;
            this.max = max;
            this.percentile95 = percentile95;
            this.percentile99 = percentile99;
            this.percentile999 = percentile999;
        }

        @Override
        public double getCount() {
            return count;
        }

        @Override
        public double getAvg() {
            return avg;
        }

        @Override
        public double getTotal() {
            return total;
        }

        @Override
        public double getStdDev() {
            return stdDev;
        }
        
        @Override
        public double getMax() {
        	return max;
        }
        
        @Override
        public double getApproximatePercentile95() {
        	return percentile95;
        }
        
        @Override
        public double getApproximatePercentile99() {
        	return percentile99;
        }
        
        @Override
        public double getApproximatePercentile999() {
        	return percentile999;
        }
    }
}
