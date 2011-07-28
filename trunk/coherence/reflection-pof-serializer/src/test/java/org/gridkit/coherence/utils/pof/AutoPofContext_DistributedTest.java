/**
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

package org.gridkit.coherence.utils.pof;

import org.gridkit.coherence.util.classloader.Isolate;
import org.gridkit.coherence.util.classloader.NodeActions;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

public class AutoPofContext_DistributedTest extends AutoPofContext_FunctionalTest {

	private static Isolate isolate;
	private static NamedCache cache;

	@BeforeClass
	public static void init_storage_node() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
		isolate = new Isolate("Remote", "org.gridkit", "com.tangosol");
		isolate.start();
		isolate.submit(NodeActions.Start.class, "auto-pof-cache-config-server.xml");
		isolate.submit(NodeActions.InitCache.class, "objects");
		
		initCache();
	}
	
	@AfterClass
	public static void shutdown_stoarge_node() {
		isolate.submit(NodeActions.Stop.class);
		isolate.stop();
		CacheFactory.getCluster().shutdown();
	}

    public static void initCache() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    	
    	CacheFactory.getCluster().shutdown();

    	System.setProperty("tangosol.coherence.wka", "localhost");
    	System.setProperty("tangosol.coherence.localhost", "localhost");

        CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("auto-pof-cache-config-client.xml"));
        cache = CacheFactory.getCache("objects");        
    }

	
	public Object serDeser(Object value) {
		cache.put("123", value);
		return cache.get("123");
	}	
}
