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

import java.util.ArrayList;

import junit.framework.Assert;

import org.gridkit.coherence.util.classloader.Isolate;
import org.gridkit.coherence.util.classloader.IsolateTestRunner;
import org.gridkit.coherence.util.classloader.NodeActions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

@RunWith(IsolateTestRunner.class)
public class AutoPofContext_DistributedTest extends AutoPofContext_FunctionalTest {

	private static Isolate isolate;
	private static NamedCache cache;

	@BeforeClass
	public static void init_storage_node() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
    	System.setProperty("tangosol.coherence.wka", "localhost");
    	System.setProperty("tangosol.coherence.localhost", "localhost");
		
		isolate = new Isolate("Remote", "org.gridkit", "com.tangosol");
		isolate.start();
		isolate.submit(NodeActions.Start.class, "auto-pof-cache-config-server.xml");
		isolate.submit(NodeActions.GetCache.class, "objects");
		
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
	
	@Test
	public void testUnsoliticed() {

		cache.remove("ok");
		cache.put("123", new Chars("123"));
		cache.put("456", new Chars("456"));
		cache.put("789", new Chars("789"));
		cache.put("111-222-333", new Chars[]{new Chars("111"), new Chars("111"), new Chars("111")});
		
		Isolate node = new Isolate("Remote-2", "org.gridkit", "com.tangosol");
		node.start();
		node.submit(NodeActions.Start.class, "auto-pof-cache-config-server.xml");
		node.submit(GetAll.class);
		node.submit(NodeActions.Stop.class);
		node.stop();		
		
		Assert.assertEquals("ok", cache.get("ok"));
	}
	
	public static class GetAll implements Runnable {

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void run() {
			System.out.println(new ArrayList(CacheFactory.getCache("objects").entrySet()).toString());
			CacheFactory.getCache("objects").put("ok", "ok");
		}
	}
}
