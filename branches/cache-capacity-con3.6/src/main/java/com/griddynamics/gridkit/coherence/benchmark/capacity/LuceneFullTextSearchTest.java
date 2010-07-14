package com.griddynamics.gridkit.coherence.benchmark.capacity;

import com.griddynamics.gridkit.coherence.index.lucene.LuceneExtractor;
import com.griddynamics.gridkit.coherence.index.lucene.TermFilter;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Alexander Solovyov
 */

public class LuceneFullTextSearchTest {

    @Before
    public void setUp() {
        System.setProperty("tangosol.pof.enabled", "true");
        System.setProperty("tangosol.pof.config", "capacity-benchmark-pof-config.xml");
        System.setProperty("tangosol.coherence.cacheconfig", "capacity-benchmark-cache-config.xml");

//      System.setProperty("benchmark-default-scheme", "local-scheme");
//      System.setProperty("benchmark-default-scheme", "local-hashmap-scheme");
//      System.setProperty("benchmark-default-scheme", "local-juc-hashmap-scheme");
      System.setProperty("benchmark-default-scheme", "simple-distributed-scheme");
//      System.setProperty("benchmark-default-scheme", "external-distributed-scheme");
//      System.setProperty("benchmark-default-scheme", "partitioned-true-external-distributed-scheme");
//      System.setProperty("benchmark-default-scheme", "partitioned-false-external-distributed-scheme");
//      System.setProperty("benchmark-default-scheme", "simple-replicated-scheme");
//      System.setProperty("benchmark-default-scheme", "transactional-scheme");
    }

    @Test
    public void test() {
        final NamedCache cache = CacheFactory.getCache("objects");

        LuceneExtractor extractor = new LuceneExtractor(null);
        cache.addIndex(extractor, false, null);

        long insert = System.currentTimeMillis();

        StringBuilder s = new StringBuilder();

        for (int i = 1; i < 3*1000*1000 + 1; i++) {
            s.append(' ').append(i % 1000);
            if (i % 3 == 0) {
                cache.put(i, s.toString());
                s = new StringBuilder();
            }
        }

        System.out.println("INSERT " + (System.currentTimeMillis() - insert));

/*
        long delete = System.currentTimeMillis();

        for (int i = 0; i < 500000; i++) {
            cache.remove("ABC" + i);
        }

        System.out.println("DELETE " + (System.currentTimeMillis() - delete));
*/

        long search = System.currentTimeMillis();
        int keyCount = cache.keySet(new TermFilter("999")).size();
/*
        Collection result = new ArrayList();
        for (Object o : cache.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String value = (String) entry.getValue();
            if (value.indexOf("AB") != -1) {
                result.add(entry.getKey());
            }
        }
*/

        System.out.println("SEARCH " + (System.currentTimeMillis() - search));

        Assert.assertEquals(3000, keyCount);
    }
}