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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.gridkit.coherence.util.classloader.IsolateTestRunner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.filter.AllFilter;
import com.tangosol.util.filter.InFilter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@RunWith(IsolateTestRunner.class)
public class PofSerializerComplexObjectTest {

    private static NamedCache cache;
    
    @BeforeClass
    public static void initCache() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    	
    	CacheFactory.getCluster().shutdown();

    	System.setProperty("tangosol.coherence.wka", "127.0.0.1");
    	System.setProperty("tangosol.coherence.localhost", "127.0.0.1");
        System.setProperty("tangosol.coherence.distributed.localstorage", "true");

        CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("test-pof-cache-config.xml"));
                
        cache = CacheFactory.getCache("objects");        
    }
    
    @AfterClass
    public static void shutdown() {
        CacheFactory.getCluster().shutdown();
    }
    
    @Test
    public void testObjectA() {
    	ComplexObjectA o1 = new ComplexObjectA("A", "B", "Claudia");
    	cache.put(1, o1);
    	Object o2 = cache.get(1);
    	
    	Assert.assertEquals(o1.getClass(), o2.getClass());
    	Assert.assertEquals(o1.toString(), o2.toString());
    }    

    @Test
    public void testObjectB() {
    	ComplexObjectB o1 = new ComplexObjectB("A", "B", "Claudia");
    	cache.put(1, o1);
    	Object o2 = cache.get(1);
    	
    	Assert.assertEquals(o1.getClass(), o2.getClass());
    	Assert.assertEquals(o1.toString(), o2.toString());
    }    

    @Test
    public void testObjectC() {
    	ComplexObjectC o1 = new ComplexObjectC("A", "B", "Claudia");
    	cache.put(1, o1);
    	Object o2 = cache.get(1);
    	
    	Assert.assertEquals(o1.getClass(), o2.getClass());
    	Assert.assertEquals(o1.toString(), o2.toString());
    	o1.strings.add("Final");
    	((ComplexObjectC)o2).strings.add("Final");
    	Assert.assertEquals(o1.toString(), o2.toString());
    }    

    @Test
    public void testObjectD() {
    	ComplexObjectD o1 = new ComplexObjectD("A", "B", "Claudia");
    	cache.put(1, o1);
    	Object o2 = cache.get(1);
    	
    	Assert.assertEquals(o1.getClass(), o2.getClass());
    	Assert.assertEquals(o1.toString(), o2.toString());
    	o1.strings.add(new Triple("Final"));
    	((ComplexObjectD)o2).strings.add(new Triple("Final"));
    	Assert.assertEquals(o1.toString(), o2.toString());
    }    
    
    @Test
    public void testObjectE() {
    	ComplexObjectE o1 = new ComplexObjectE("A", "B", "Claudia");
    	cache.put(1, o1);
    	Object o2 = cache.get(1);
    	
    	Assert.assertEquals(o1.getClass(), o2.getClass());
    	Assert.assertEquals(o1.toString(), o2.toString());
    	o1.strings.put("Final", new Triple("Final"));
    	((ComplexObjectE)o2).strings.put("Final", new Triple("Final"));
    	Assert.assertEquals(o1.toString(), o2.toString());
    }    
    
    @Test
    public void testCoherenceFilter() throws IOException {
    	Set<String> set = new TreeSet<String>();
    	
    	cache.put("1", "A");
    	cache.put("2", "B");
    	cache.put("3", "C");
    	cache.put("4", "AA");
    	cache.put("5", "BB");
    	cache.put("6", "CC");
    	
    	set.add("A");
    	set.add("B");
    	set.add("AA");
    	set.add("CC");
    	
    	InFilter f = new InFilter(IdentityExtractor.INSTANCE, set);
    	AllFilter af = new AllFilter(new Filter[]{f});
    	
    	cache.put("---", af);
    	Filter af2 = (Filter) cache.get("---");
    	
    	ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
    	oos.writeObject(af2);
    	oos.close();
    	
    	Assert.assertEquals(4, cache.keySet(af2).size());    	
    }
    
	@Test
	public void testByteArrayTreeMap() {
		TreeMap<byte[], String> map1 = new TreeMap<byte[], String>(new ByteArrayComparator());
		map1.put("abc".getBytes(), "abc");
		map1.put("ABC".getBytes(), "ABC");
		map1.put("123".getBytes(), "123");
		map1.put("CDE".getBytes(), "CDE");
		map1.put("dec".getBytes(), "dec");

    	cache.put("---", map1);
    	Object map2 = cache.get("---");

		Assert.assertSame(map1.getClass(), map2.getClass());
		Assert.assertEquals(map1.values().toString(), ((Map<?,?>)map2).values().toString());
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
    
//    @Test
//    public void mapExtractor() {
//    	Map<String, String> map = new HashMap<String, String>();
//    	
//    	map.put("1", "A");
//    	map.put("2", "B");
//    	map.put("3", "C");
//    	
//    	ReflectionExtractor re = new ReflectionExtractor("get", new Object[]{"1"});
//    	Assert.assertEquals("A", re.extract(map));
//    }
    
    public static class ComplexObjectA {
        
        public String[] strings;
        
        public ComplexObjectA() { };
        
        public ComplexObjectA(String... strings) {
            this.strings = strings;
        }

		@Override
		public String toString() {
			return "ComplexObjectA" + Arrays.toString(strings);
		}
    }

    public static class ComplexObjectB {
    	
    	public Triple[] strings;
    	
    	public ComplexObjectB() { };
    	
    	public ComplexObjectB(String... strings) {
    		this.strings = new Triple[strings.length];
    		for(int i = 0; i != strings.length; ++i) {
    			this.strings[i] = new Triple(strings[i]);
    		}
    	}
    	
    	@Override
    	public String toString() {
    		return "ComplexObjectB" + Arrays.toString(strings);
    	}
    }

    public static class ComplexObjectC {
    	
    	public List<String> strings = new ArrayList<String>();
    	
    	public ComplexObjectC() { };
    	
    	public ComplexObjectC(String... strings) {
    		this.strings.addAll(Arrays.asList(strings));
    	}
    	
    	@Override
    	public String toString() {
    		return "ComplexObjectC" + strings.toString();
    	}
    }    

    public static class ComplexObjectD {
    	
    	public Deque<Triple> strings = new LinkedList<Triple>();
    	
    	public ComplexObjectD() { };
    	
    	public ComplexObjectD(String... strings) {
    		for(int i = 0; i != strings.length; ++i) {
    			this.strings.add(new Triple(strings[i]));
    		}
    	}
    	
    	@Override
    	public String toString() {
    		return "ComplexObjectD" + strings.toString();
    	}
    }    

    public static class ComplexObjectE {
    	
    	public SortedMap<String, Triple> strings = new TreeMap<String,Triple>();
    	
    	public ComplexObjectE() { };
    	
    	public ComplexObjectE(String... strings) {
    		for(int i = 0; i != strings.length; ++i) {
    			this.strings.put(strings[i], new Triple(strings[i]));
    		}
    	}
    	
    	@Override
    	public String toString() {
    		return "ComplexObjectE" + strings.toString();
    	}
    }    
    
    public static class Triple {
        
        public char a;
        public char b;
        public char c;
        public char[] whole;
        
        public Triple() {};
        
        public Triple(String txt) {
            this.a = txt.length() < 1 ? ' ' : txt.charAt(0);
            this.b = txt.length() < 2 ? ' ' : txt.charAt(1);
            this.c = txt.length() < 3 ? ' ' : txt.charAt(2);
            txt.getChars(0, txt.length(), whole = new char[txt.length()], 0);
        }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + a;
			result = prime * result + b;
			result = prime * result + c;
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
			Triple other = (Triple) obj;
			if (a != other.a)
				return false;
			if (b != other.b)
				return false;
			if (c != other.c)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "" + a + b + c + "/" + Arrays.toString(whole); 
		}
    }
}
