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

package org.gridkit.coherence.utils.pof;

import org.gridkit.coherence.utils.pof.ReflectionPofExtractor;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.filter.EqualsFilter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class PofExtractorCacheTest {

    private static final int SCALE = 4;
    
    private static NamedCache cache;
    
    @BeforeClass
    public static void initCache() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    	
    	CacheFactory.getCluster().shutdown();

    	System.setProperty("tangosol.coherence.wka", "localhost");
    	System.setProperty("tangosol.coherence.localhost", "localhost");
//        System.setProperty("tangosol.pof.enabled", "true");
//        System.setProperty("tangosol.pof.config", "test-pof-config.xml");
//        System.setProperty("tangosol.coherence.cacheconfig", "test-pof-cache-config.xml");
        System.setProperty("tangosol.coherence.distributed.localstorage", "true");

        CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("test-pof-cache-config.xml"));
        
//        // Hack around, to swetch default pof config after class is loaded
//        Field f = ConfigurablePofContext.class.getField("DEFAULT_RESOURCE");
//        f.setAccessible(true);
//        f.set(null, System.getProperty("tangosol.pof.config"));
        
        cache = CacheFactory.getCache("objects");
        
        int objects = SCALE * SCALE * SCALE * SCALE;
        for(int i = 0; i != objects; ++i) {
            cache.put(i, generate(i, SCALE));
        }
    }
    
    @AfterClass
    public static void shutdown() {
        CacheFactory.getCluster().shutdown();
    }
    
    @Test
    public void testFilter_1() {
        ReflectionPofExtractor extracter = new ReflectionPofExtractor("a");
        
        int size = cache.keySet(new EqualsFilter(extracter, "0")).size();
        
        Assert.assertEquals(SCALE * SCALE * SCALE, size);
    }    

    @Test
    public void testFilter_2() {
        ReflectionPofExtractor extracter = new ReflectionPofExtractor("tuple.a");
        
        int size = cache.keySet(new EqualsFilter(extracter, '0')).size();
        
        Assert.assertEquals(SCALE * SCALE * SCALE, size);
    }    

    @Test
    public void testIndex_1() {
        ReflectionPofExtractor extracter = new ReflectionPofExtractor("a");
        
        cache.addIndex(extracter, false, null);

        int size = cache.keySet(new EqualsFilter(extracter, "0")).size();
        
        Assert.assertEquals(SCALE * SCALE * SCALE, size);
    }    

    @Test
    public void testIndex_2() {
        ReflectionPofExtractor extracter = new ReflectionPofExtractor("tuple.a");
        
        cache.addIndex(extracter, false, null);
        
        int size = cache.keySet(new EqualsFilter(extracter, '0')).size();
        
        Assert.assertEquals(SCALE * SCALE * SCALE, size);
    }    

    private static TupleA generate(int ordinal, int radix) {
        int x1 = ordinal % radix;
        ordinal /= radix;
        int x2 = ordinal % radix;
        ordinal /= radix;
        int x3 = ordinal % radix;
        ordinal /= radix;
        int x4 = ordinal % radix;
        
        return new TupleA(String.valueOf(x4), new TupleB((char)('0' + x3), (char)('0' + x1), (char)('0' + x2)));        
    }
    
    public static class TupleA {
        
        public String a;
        public TupleB tuple;
        
        public TupleA() { };
        
        public TupleA(String a, TupleB tuple) {
            this.a = a;
            this.tuple = tuple;
        }
    }
    
    public static class TupleB {
        
        public char a;
        public char b;
        public char c;
        
        public TupleB() {};
        
        public TupleB(char a, char b, char c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
}
