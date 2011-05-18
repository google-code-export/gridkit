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
package org.gridkit.coherence.profile.distributed;

import java.util.Map;

import org.gridkit.coherence.profile.Sampler;
import org.gridkit.coherence.profile.StatValue;


/**
 * @author Alexey Ragozin (aragozin@gridsynamics.com)
 */
public interface HistogramService {

    public Sampler defineSampler(String name, long scale, long min, long max, int size);
    
    public StatValue getValue(String name);
    
    public void resetValue(String name);

    public Map<String, StatValue> getAll(String pattern);
    
    public void resetAll(String pattern);
}
