/**
 * Copyright 2011 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.coherence.profile.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.gridkit.coherence.profile.StatValue;
import org.gridkit.coherence.profile.distributed.ClusterInfoService;
import org.gridkit.coherence.profile.distributed.HistogramService;
import org.gridkit.coherence.profile.runtime.RuntimeStats;
import org.gridkit.coherence.profile.utils.JvmId;

import com.tangosol.net.AbstractInvocable;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.InvocationService;

/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class ShowClusterStats {

    private String[] args;
    
    public ShowClusterStats(String[] args) {
        this.args = args;
    }
    
    public static void main(String[] args) {
        System.setProperty("tangosol.coherence.distributed.localstorage", "false");
        new ShowClusterStats(args).start();
    }

    private void start() {
        Map<String, StatValue> values = new LinkedHashMap<String, StatValue>();
        HistogramService ss = ClusterInfoService.getInstance().getHistogramService();
        
        for(String prop: args) {
            if (prop.startsWith("reset-histo:")) {
                prop = prop.substring("reset-histo:".length());
                
                ClusterInfoService.getInstance().getHistogramService().resetAll(prop);
//                InvocationService service = (InvocationService) CacheFactory.getService("ClusterInvocationService");
//                Map<?,?> result = service.query(new StatsReset(prop), service.getCluster().getMemberSet());
//                System.out.println("Statistic reset on " + result.values());

                continue;
            }
            if (prop.startsWith("enable:")) {
            
                boolean value = Boolean.parseBoolean(prop.substring("enable:".length()));
                
                InvocationService service = (InvocationService) CacheFactory.getService("ClusterInvocationService");
                Map<?,?> result = service.query(new StatsOn(value), service.getCluster().getMemberSet());
                System.out.println((value ? "Statistics enabled" : "Statistics disabled") + " on " + result.values());
                
                continue;
            }
            if (prop.equals("all")) {
                prop = "*";
            }
            if (prop.startsWith("'")) {
                prop = prop.substring(1);
            }
            if (prop.endsWith("'")) {
                prop = prop.substring(0, prop.length() - 1);
            }
            if (prop.startsWith("histo:")) {
                prop = prop.substring("histo:".length());
            }
            if (prop.indexOf('?') >= 0 || prop.indexOf('*') >= 0) {
                values.putAll(new TreeMap<String, StatValue>(ss.getAll(prop)));
            }
            else {
                values.put(prop, ss.getValue(prop));
            }
        }
        
        // remove nulls
        values.values().removeAll(Collections.singleton(null));
        
        System.out.println(StatsTableFormater.formatStatsTable(values));
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
}
