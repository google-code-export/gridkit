/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
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

package org.gridkit.coherence.integration;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.tangosol.io.Serializer;
import com.tangosol.io.ReadBuffer.BufferInput;
import com.tangosol.io.WriteBuffer.BufferOutput;
import com.tangosol.net.InvocationService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.AbstractEvictionPolicy;
import com.tangosol.net.cache.CacheLoader;
import com.tangosol.net.cache.NearCache;
import com.tangosol.net.cache.ConfigurableCacheMap.Entry;
import com.tangosol.util.ExternalizableHelper;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public abstract class BaseSimpleContextTest {

	protected static ApplicationContext context;
		
	@Test
	public void testCacheA() {
		NamedCache cache = (NamedCache) context.getBean("cache.A");
		cache.put("a", "b");
		Assert.assertEquals("b", cache.get("a"));
	}
	
	@Test
	public void testCacheB() {
		NamedCache cache = (NamedCache) context.getBean("cache.B");
		Assert.assertEquals("A", cache.get("a"));		
		Assert.assertEquals("B", cache.get("b"));		
	}

	public static class TestCacheLoader implements CacheLoader {

		public TestCacheLoader() {
			new String();
		}
		
		@Override
		public Object load(Object key) {
			return key.toString().toUpperCase();
		}

		@Override
		@SuppressWarnings("unchecked")
		public Map loadAll(Collection key) {
			throw new UnsupportedOperationException();
		}
	}
	
	@Test
	public void testCacheC() {
		NamedCache cache = (NamedCache) context.getBean("cache.C");
		cache.put("a", "b");
		Assert.assertEquals("b", cache.get("a"));
	}

	@Test
	public void testCacheD() {
		NamedCache cache = (NamedCache) context.getBean("cache.D");
		cache.put("a", "b");
		cache.put("a", "b");
		Assert.assertEquals("b", cache.get("a"));
		LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1000));
		// entry should expire
		Assert.assertEquals(null, cache.get("a"));
	}
	
	@Test
	public void testCacheE_Serializer() {
		NamedCache cache = (NamedCache) context.getBean("cache.E");
		cache.put("a", new CustomObject("b"));
		Assert.assertEquals(new CustomObject("b"), cache.get("a"));
	}
	
	public static class CustomObject {
		String value;

		public CustomObject(String value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CustomObject other = (CustomObject) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static class CustomSerializer implements Serializer {

		@Override
		public Object deserialize(BufferInput buf)	throws IOException {
			int type = buf.readByte();
			if (type == 0) {
				byte[] data = new byte[buf.available()];
				buf.readFully(data);
				return ExternalizableHelper.fromByteArray(data);
			}
			else {
				return new CustomObject(buf.readUTF());
			}
		}

		@Override
		public void serialize(BufferOutput buf, Object object) throws IOException {
			if (object instanceof CustomObject) {
				buf.writeByte(1);
				buf.writeUTF(((CustomObject) object).value);
			}
			else {
				buf.writeByte(0);
				buf.write(ExternalizableHelper.toByteArray(object));
			}
		}
	}
	
	@Test
	public void testCacheF_Evictor() {
		NamedCache cache = (NamedCache) context.getBean("cache.F");
		cache.put("a", "A");
		cache.put("b", "B");
		cache.put("c", "C");
//		LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(200));
		Assert.assertEquals("A", cache.get("a"));
		Assert.assertEquals(null, cache.get("b"));
		Assert.assertEquals("C", cache.get("c"));
	}
	
	public static class CustomEvictor extends AbstractEvictionPolicy {

		private AtomicInteger counter = new AtomicInteger();

		
		
//		@Override
//		public void entryTouched(Entry paramEntry) {
//			int n = counter.getAndIncrement();
//			if (n % 2 == 1) {
//				paramEntry.
//				paramEntry.setExpiryMillis(1);
//			}
//			else {
//				paramEntry.setExpiryMillis(0);
//			}			
//		}

		@Override
		public void entryUpdated(Entry entry) {
			int n = counter.getAndIncrement();
			if (n % 2 == 1) {
				getCache().remove(entry.getKey());
			}
			else {
			}			
		}

		@Override
		public void entryTouched(Entry paramEntry) {
			// do nothing
		}

		@Override
		public String getName() {
			return "Even-Evictor";
		}

		@Override
		public void requestEviction(int paramInt) {
			// do nothing
		}
	}

	@Test
	public void testCacheG_Replicated() {
		NamedCache cache = (NamedCache) context.getBean("cache.G");
		cache.put("a", "A");
		cache.put("b", "B");
		cache.put("c", "C");
		Assert.assertEquals("A", cache.get("a"));
		Assert.assertEquals("B", cache.get("b"));
		Assert.assertEquals("C", cache.get("c"));
	}

	@Test
	public void testCacheH_Optimistic() {
		NamedCache cache = (NamedCache) context.getBean("cache.H");
		cache.put("a", "A");
		cache.put("b", "B");
		cache.put("c", "C");
		Assert.assertEquals("A", cache.get("a"));
		Assert.assertEquals("B", cache.get("b"));
		Assert.assertEquals("C", cache.get("c"));
	}

	@Test
	public void testCacheI_Near() {
		NearCache cache = (NearCache) context.getBean("cache.I");
		cache.put("a", "A");
		cache.put("b", "B");
		cache.put("c", "C");
		Assert.assertEquals("A", cache.get("a"));
		Assert.assertEquals("B", cache.get("b"));
		Assert.assertEquals("C", cache.get("c"));
	}

	@Test
	public void testCacheJ_Near() {
		NearCache cacheI = (NearCache) context.getBean("cache.I");
		NearCache cacheJ = (NearCache) context.getBean("cache.J");
		cacheI.put("a", "A");
		cacheI.put("b", "B");
		cacheI.put("c", "C");
		cacheJ.put("a", "X");
		cacheJ.put("b", "Y");
		cacheJ.put("c", "Z");
		Assert.assertEquals("A", cacheI.get("a"));
		Assert.assertEquals("B", cacheI.get("b"));
		Assert.assertEquals("C", cacheI.get("c"));
		Assert.assertEquals("X", cacheJ.get("a"));
		Assert.assertEquals("Y", cacheJ.get("b"));
		Assert.assertEquals("Z", cacheJ.get("c"));
		Assert.assertEquals("A", cacheI.getFrontMap().get("a"));
		Assert.assertEquals("B", cacheI.getFrontMap().get("b"));
		Assert.assertEquals("C", cacheI.getFrontMap().get("c"));
		Assert.assertEquals("X", cacheJ.getFrontMap().get("a"));
		Assert.assertEquals("Y", cacheJ.getFrontMap().get("b"));
		Assert.assertEquals("Z", cacheJ.getFrontMap().get("c"));
	}
	
	@Test
	public void testService_Invocation() {
		InvocationService service = (InvocationService) context.getBean("exec-service");
		service.getInfo().getServiceMembers();
	}
	
//	@Test
	@Ignore
	public void testCacheK_RemoteCache() {
		NearCache cache = (NearCache) context.getBean("cache.K");
		cache.put("a", "A");
		cache.put("b", "B");
		cache.put("c", "C");
		Assert.assertEquals("A", cache.get("a"));
		Assert.assertEquals("B", cache.get("b"));
		Assert.assertEquals("C", cache.get("c"));
	}
	
}
