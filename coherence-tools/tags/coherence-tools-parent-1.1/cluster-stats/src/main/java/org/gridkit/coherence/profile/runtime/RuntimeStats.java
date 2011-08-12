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
package org.gridkit.coherence.profile.runtime;

import java.util.concurrent.TimeUnit;

import org.gridkit.coherence.profile.Sampler;
import org.gridkit.coherence.profile.distributed.ClusterInfoService;


/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public class RuntimeStats {

    public static boolean ENABLED = Boolean.valueOf(System.getProperty("org.gridkit.coherence.profile.enabled", "false"));
 
    public static long nanoTime() {
        return ENABLED ? System.nanoTime() : 0;
    }

    public static long delay(long from) {
        return from == 0 ? -1 : System.nanoTime() - from;
    }
    
    public static Sampler getLatencyTimer(String name, int maxMs) {
        return ClusterInfoService.getInstance().getHistogramService().defineSampler(name, TimeUnit.MILLISECONDS.toNanos(1), 0, maxMs, 50);
    }

    public static Sampler getLatencyTimer(String name, int maxMs, int width) {
        return ClusterInfoService.getInstance().getHistogramService().defineSampler(name, TimeUnit.MILLISECONDS.toNanos(1), 0, maxMs, width);
    }

    public static Sampler getSizeSampler(String name, int maxSize) {
        return ClusterInfoService.getInstance().getHistogramService().defineSampler(name, 1, 0, maxSize, maxSize > 10 ? 10 : maxSize);
    }

    public static Sampler getBooleanSampler(String name) {
        return ClusterInfoService.getInstance().getHistogramService().defineSampler(name, 1, 0, 1, 1);
    }
}
