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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import junit.framework.Assert;

import org.junit.Test;

import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.LocalCache;
import com.tangosol.net.cache.WrapperNamedCache;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

public class AutoPofContext_LocalTest {

	private static NamedCache typeMap = new WrapperNamedCache(new LocalCache(), "");
	private static AutoPofContext ctx1 = new AutoPofContext("coherence-pof-config.xml", typeMap);
	private static AutoPofContext ctx2 = new AutoPofContext("coherence-pof-config.xml", typeMap);

	
	public Object serDeser12(Object value) {
		Binary bin = ExternalizableHelper.toBinary(value, ctx1);
		return ExternalizableHelper.fromBinary(bin, ctx2);
	}

	public Object serDeser21(Object value) {
		Binary bin = ExternalizableHelper.toBinary(value, ctx1);
		return ExternalizableHelper.fromBinary(bin, ctx2);
	}
	
	@Test
	public void testString() {
		String s1 = "Quick brown fox";
		Assert.assertEquals(s1, serDeser12(s1));
		Assert.assertEquals(s1, serDeser21(s1));
	}

	@Test
	public void testStringArray() {
		String[] ss = {"Quick brown fox","has jumped","over lazy dog"};
		Object ss2 = serDeser12(ss);
		Assert.assertSame(String[].class, ss2.getClass());
		Assert.assertTrue(Arrays.equals(ss, (Object[]) ss2));
	}

	@Test
	public void testArrayList() {
		String[] ss = {"Quick brown fox","has jumped","over lazy dog"};
		List<String> l1 = new ArrayList<String>(Arrays.asList(ss));
		Object l2 = serDeser12(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}

	@Test
	public void testObjectArray() {
		Chars[] ss = {new Chars("Quick brown fox"), null, new Chars("over lazy dog")};
		Object ss2 = serDeser12(ss);
		Assert.assertSame(ss.getClass(), ss2.getClass());
		Assert.assertEquals(Arrays.toString(ss), Arrays.toString(ss));
	}
	
	@Test
	public void testObjectList() {
		Chars[] ss = {new Chars("Quick brown fox"), null, new Chars("over lazy dog")};
		List<Chars> l1 = new ArrayList<Chars>(Arrays.asList(ss));
		Object l2 = serDeser12(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}	

	@Test
	public void testObjectSet() {
		Chars[] ss = {new Chars("Quick brown fox"), null, new Chars("over lazy dog")};
		HashSet<Chars> l1 = new HashSet<Chars>(Arrays.asList(ss));
		Object l2 = serDeser12(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1, l2);
	}	

	@Test
	public void testObjectMap() {
		HashMap<String, Integer> map1 = new HashMap<String, Integer>();
		map1.put("A", 1);
		map1.put("B", 2);
		map1.put("C", 3);
		map1.put("D", 5);
		map1.put("E", 10);
		Object map2 = serDeser12(map1);
		Assert.assertSame(map1.getClass(), map2.getClass());
		Assert.assertEquals(map1, map2);
	}	

	@Test
	public void testObjectTreeMap() {
		TreeMap<String, Integer> map1 = new TreeMap<String, Integer>();
		map1.put("A", 1);
		map1.put("B", 2);
		map1.put("C", 3);
		map1.put("D", 5);
		map1.put("E", 10);
		Object map2 = serDeser12(map1);
		Assert.assertSame(map1.getClass(), map2.getClass());
		Assert.assertEquals(map1, map2);
	}	
	
	public static class Chars {
		char[] chars;
		
		@SuppressWarnings("unused")
		private Chars() {
		}
		
		public Chars(String text) {
			chars = text.toCharArray();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(chars);
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
			Chars other = (Chars) obj;
			if (!Arrays.equals(chars, other.chars))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return new String(chars);
		}
	}
}
