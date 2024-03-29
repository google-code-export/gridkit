/**
 * Copyright 2008-2010 Grid Dynamics Consulting Services, Inc.
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
package com.griddynamics.gridkit.coherence.patterns.benchmark;

import static com.griddynamics.gridkit.coherence.patterns.benchmark.GeneralHelper.setCoherenceConfig;

import com.tangosol.net.DefaultCacheServer;

public class DumbBackend
{
	public static void main(String[] args)
	{
		setCoherenceConfig(false);
		System.setProperty("tangosol.coherence.cacheconfig", "benchmark-pof-cache-empty-config.xml");
		DefaultCacheServer.main(args);
	}
}
