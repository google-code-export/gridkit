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
public class AutoPofContext_ExtendTest extends AutoPofContext_FunctionalTest {

	private static Isolate isolate;
	private static NamedCache cache;

	@BeforeClass
	public static void init_storage_node() throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
    	System.setProperty("gridkit.auto-pof.use-public-cache-config", "true");
		
		System.setProperty("tangosol.coherence.wka", "127.0.0.1");
    	System.setProperty("tangosol.coherence.localhost", "127.0.0.1");
		
		isolate = new Isolate("Remote", "org.gridkit", "com.tangosol");
		isolate.start();
		isolate.submit(NodeActions.Start.class, "auto-pof-cache-config-extend-server.xml");
		isolate.submit(NodeActions.GetService.class, "AUTO_POF_SERVICE");
		isolate.submit(NodeActions.GetService.class, "TcpProxyService");
//		isolate.submit(NodeActions.GetService.class, "TcpAutoPofProxyService");
		
		initCache();
	}
	
	@AfterClass
	public static void shutdown_stoarge_node() {
		isolate.submit(NodeActions.Stop.class);
		isolate.stop();
		CacheFactory.getCluster().shutdown();
		System.getProperties().remove("gridkit.auto-pof.use-public-cache-config");
	}

    public static void initCache() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    	
    	CacheFactory.getCluster().shutdown();

    	System.setProperty("tangosol.coherence.wka", "127.0.0.1");
    	System.setProperty("tangosol.coherence.localhost", "127.0.0.1");

        CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("auto-pof-cache-config-extend-client.xml"));
        cache = CacheFactory.getCache("AUTO_POF_MAPPING");
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
		node.submit(NodeActions.Start.class, "auto-pof-cache-config-extend-server.xml");
		node.submit(GetAll.class);
		node.submit(NodeActions.Stop.class);
		node.stop();		
		
		Assert.assertEquals("ok", cache.get("ok"));
	}

	@Test
	public void testBackPush() {
		
		cache.remove("dummy");
		
		Isolate node = new Isolate("Remote-2", "org.gridkit", "com.tangosol");
		node.start();
		node.submit(NodeActions.Start.class, "auto-pof-cache-config-extend-server.xml");
		node.submit(PushObject.class);
		node.submit(NodeActions.Stop.class);
		node.stop();		
		
		Dummy dm = (Dummy) cache.get("dummy");
		Assert.assertEquals("ok", dm.dummyOk);
	}
	
	public static class GetAll implements Runnable {

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void run() {
			System.out.println(new ArrayList(CacheFactory.getCache("objects").entrySet()).toString());
			CacheFactory.getCache("objects").put("ok", "ok");
		}
	}

	public static class PushObject implements Runnable {
		
		@Override
		public void run() {
			Dummy dm = new Dummy();
			dm.dummyOk = "ok";
			CacheFactory.getCache("objects").put("dummy", dm);
		}
	}
	
	public static class Dummy {
		public String dummyOk;
	}
}
