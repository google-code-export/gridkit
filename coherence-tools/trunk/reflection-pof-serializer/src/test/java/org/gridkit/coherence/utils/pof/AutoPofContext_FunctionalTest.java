/**
 * Copyright Alexey Ragozin 2011
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import com.tangosol.util.ValueExtractor;
import com.tangosol.util.aggregator.AbstractAggregator;
import com.tangosol.util.aggregator.DistinctValues;

public abstract class AutoPofContext_FunctionalTest {

	public abstract Object serDeser(Object value);

	@Test
	public void testString() {
		String s1 = "Quick brown fox";
		Assert.assertEquals(s1, serDeser(s1));
	}

	@Test
	public void testStringArray() {
		String[] ss = {"Quick brown fox","has jumped","over lazy dog"};
		Object ss2 = serDeser(ss);
		Assert.assertSame(String[].class, ss2.getClass());
		Assert.assertTrue(Arrays.equals(ss, (Object[]) ss2));
	}

	@Test
	public void testArrayList() {
		String[] ss = {"Quick brown fox","has jumped","over lazy dog"};
		List<String> l1 = new ArrayList<String>(Arrays.asList(ss));
		Object l2 = serDeser(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}

	@Test
	public void testLargeArrayList() {
		ArrayList<String> list = new ArrayList<String>();
		for(int i = 0; i != 100; ++i) {
			list.add(String.valueOf(i));
		}
		List<String> l1 = list;
		Object l2 = serDeser(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}	
	
	@Test
	public void testObjectArray() {
		Chars[] ss = {new Chars("Quick brown fox"), null, new Chars("over lazy dog")};
		Object ss2 = serDeser(ss);
		Assert.assertSame(ss.getClass(), ss2.getClass());
		Assert.assertEquals(Arrays.toString(ss), Arrays.toString(ss));
	}

	@Test
	public void testObjectList() {
		Chars[] ss = {new Chars("Quick brown fox"), null, new Chars("over lazy dog")};
		List<Chars> l1 = new ArrayList<Chars>(Arrays.asList(ss));
		Object l2 = serDeser(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}

	@Test
	public void testUnmodificableObjectList() {
		Chars[] ss = {new Chars("Quick brown fox"), null, new Chars("over lazy dog")};
		List<Chars> l1 = new ArrayList<Chars>(Arrays.asList(ss));
		l1 = Collections.unmodifiableList(l1);
		Object l2 = serDeser(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}

	@Test
	public void testEmptyList() {
		List<String> l1 = Collections.emptyList();
		Object l2 = serDeser(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}

	@Test
	public void testEmptySet() {
		Set<String> l1 = Collections.emptySet();
		Object l2 = serDeser(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}
	
	@Test
	public void testSingletonList() {
		Chars ss = new Chars("Quick brown fox");
		List<Chars> l1 = Collections.singletonList(ss);
		Object l2 = serDeser(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}

	@Test
	public void testSingletonSet() {
		Chars ss = new Chars("Quick brown fox");
		Set<Chars> l1 = Collections.singleton(ss);
		Object l2 = serDeser(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}

	@Test
	public void testObjectSet() {
		Chars[] ss = {new Chars("Quick brown fox"), null, new Chars("over lazy dog")};
		HashSet<Chars> l1 = new HashSet<Chars>(Arrays.asList(ss));
		Object l2 = serDeser(l1);
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
		Object map2 = serDeser(map1);
		Assert.assertSame(map1.getClass(), map2.getClass());
		Assert.assertEquals(map1, map2);
	}

	@Test
	public void testSingletonMap() {
		Map<String, Integer> map1 = Collections.singletonMap("A", 1);
		Object map2 = serDeser(map1);
		Assert.assertSame(map1.getClass(), map2.getClass());
		Assert.assertEquals(map1, map2);
	}

	@Test
	public void testEmptyMap() {
		Map<String, Integer> map1 = Collections.emptyMap();
		Object map2 = serDeser(map1);
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
		Object map2 = serDeser(map1);
		Assert.assertSame(map1.getClass(), map2.getClass());
		Assert.assertEquals(map1, map2);
	}

	@Test
	public void testUnmodificableObjectTreeMap() {
		Map<String, Integer> map1 = new TreeMap<String, Integer>();
		map1.put("A", 1);
		map1.put("B", 2);
		map1.put("C", 3);
		map1.put("D", 5);
		map1.put("E", 10);
		map1 = Collections.unmodifiableMap(map1);
		Object map2 = serDeser(map1);
		Assert.assertSame(map1.getClass(), map2.getClass());
		Assert.assertEquals(map1, map2);
	}

	@Test
	public void testByteArrayTreeMap() {
		TreeMap<byte[], String> map1 = new TreeMap<byte[], String>(new ByteArrayComparator());
		map1.put("abc".getBytes(), "abc");
		map1.put("ABC".getBytes(), "ABC");
		map1.put("123".getBytes(), "123");
		map1.put("CDE".getBytes(), "CDE");
		map1.put("dec".getBytes(), "dec");
		Object map2 = serDeser(map1);
		Assert.assertSame(map1.getClass(), map2.getClass());
		Assert.assertEquals(map1.values().toString(), ((Map<?,?>)map2).values().toString());
	}

	@Test
	public void testArraysAsList() {
		List<String> list1 = Arrays.asList(new String[]{"Quick", "bronw", "fox","has","jumped","over","lazy","dog"});
		Object list2 = serDeser(list1);
		Assert.assertSame(list1.getClass(), list2.getClass());
		Assert.assertEquals(list1.toString(), list2.toString());
	}
	
	@Test
	public void testLinkedHashMap() {
		Map<String, String> map1 = new LinkedHashMap<String, String>();
		map1.put("A", "aaa");
		map1.put("C", "ccc");
		map1.put("B", "bbb");
		map1.put("3", "333");
		map1.put("2", "222");
		map1.put("1", "111");
		
		Object map2 = serDeser(map1);
		Assert.assertSame(map1.getClass(), map2.getClass());
		Assert.assertEquals(map1.toString(), map2.toString());		
	}

	@Test
	public void testEnum() {
		State s = State.STATE1;
		Object s1 = serDeser(s);
		Assert.assertSame(s, s1);
	}	

	@Test
	public void testEnumCollection() {
		List<State> l1 = new ArrayList<State>();
		l1.add(State.STATE1);
		l1.add(State.STATE1);
		l1.add(State.STATE2);
		l1.add(State.STATE3);
		
		Object l2 = serDeser(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}		

	@Test @Ignore("No support for RegEx")
	public void testRegex() {
		Pattern l1 = Pattern.compile("[0-9]+");
		
		Object l2 = serDeser(l1);
		Assert.assertSame(l1.getClass(), l2.getClass());
		Assert.assertEquals(l1.toString(), l2.toString());
	}

	@Test
	public void testPofNonPofComposite() {

		DistinctValues a1 = new DistinctValues(new CustomExtractor());
		DistinctValues a2 = (DistinctValues) serDeser(a1);
		Assert.assertSame(a1.getClass(), a2.getClass());
		Assert.assertEquals(a1.getValueExtractor(), a2.getValueExtractor());
	}

	@Test
	public void testInheritance1() {
		
		CustomAggregator1 a1 = new CustomAggregator1();
		CustomAggregator1 a2 = (CustomAggregator1) serDeser(a1);
		Assert.assertSame(a1.getClass(), a2.getClass());
		Assert.assertEquals(a1, a2);
	}

	@Test
	public void testInheritance2() {
		
		CustomAggregator2 a1 = new CustomAggregator2("test");
		CustomAggregator2 a2 = (CustomAggregator2) serDeser(a1);
		Assert.assertSame(a1.getClass(), a2.getClass());
		Assert.assertEquals(a1, a2);
	}
	
	public static enum State {
		STATE1,
		STATE2,
		STATE3
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

	public static class ByteArrayComparator implements Comparator<byte[]> {

		@Override
		public int compare(byte[] o1, byte[] o2) {
			int l = Math.min(o1.length, o2.length);
			for (int i = 0; i != l; ++i) {
				int b1 = 0xFF & o1[i];
				int b2 = 0xFF & o2[i];
				if (b1 == b2) {
					continue;
				}
				else {
					return b1 < b2 ? -1 : 1;
				}
			}
			return o1.length == o2.length ? 0 : o1.length < o2.length ? -1 : 1;
		}		
	}
	
	@SuppressWarnings("serial")
	public static class CustomAggregator1 extends AbstractAggregator {
		
		public CustomAggregator1() {
			// for deserialization
		}
		
		@Override
		public int hashCode() {
			return getClass().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			return true;
		}

		@Override
		protected Object finalizeResult(boolean arg0) {
			return null;
		}

		@Override
		protected void init(boolean arg0) {
		}

		@Override
		protected void process(Object arg0, boolean arg1) {
		}
	}
	
	@SuppressWarnings("serial")
	public static class CustomAggregator2 extends AbstractAggregator {
		
		private String value;

		@SuppressWarnings("unused")
		private CustomAggregator2() {
			// for deserialization
		}
		
		public CustomAggregator2(String value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			CustomAggregator2 other = (CustomAggregator2) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		protected Object finalizeResult(boolean arg0) {
			return value;
		}

		@Override
		protected void init(boolean arg0) {
		}

		@Override
		protected void process(Object arg0, boolean arg1) {
		}
	}
	
	public static class CustomExtractor implements ValueExtractor {
	
		private String prefix;
		
		private CustomExtractor() {
			// for deserialization
		}
		
		public CustomExtractor(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((prefix == null) ? 0 : prefix.hashCode());
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
			CustomExtractor other = (CustomExtractor) obj;
			if (prefix == null) {
				if (other.prefix != null)
					return false;
			} else if (!prefix.equals(other.prefix))
				return false;
			return true;
		}

		@Override
		public Object extract(Object arg) {
			return prefix + String.valueOf(arg);
		}
	}
}
