package com.griddynamics.gridkit.coherence.benchmark.capacity;

import com.griddynamics.gridkit.coherence.index.lucene.LuceneExtractor;
import com.griddynamics.gridkit.coherence.index.lucene.WildcardFilter;
import com.griddynamics.gridkit.coherence.index.ngram.ContainsSubstringFilter;
import com.griddynamics.gridkit.coherence.index.ngram.NGramExtractor;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Alexander Solovyov
 */

public class LuceneIndexTest {

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
    public void testNGram() {
        final NamedCache cache = CacheFactory.getCache("objects");

        NGramExtractor extractor = new NGramExtractor(3);
        cache.addIndex(extractor, false, null);

        for (int i = 0; i < 1000; i++) {
            cache.put("ABC" + i, "ABC" + i);
        }

        cache.keySet(new ContainsSubstringFilter("AB")).size();
    }

    @Test
    public void testLucene() {
        final NamedCache cache = CacheFactory.getCache("objects");

        LuceneExtractor extractor = new LuceneExtractor(null);
        cache.addIndex(extractor, false, null);

        cache.put("ABCff", "dd");
        cache.put("ABCsddds", "dfsf");
        cache.put("ABCsdfsd", "sgfsd");

        long insert = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            cache.put("ABC" + i, "ABC" + i);
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
        int keyCount = cache.keySet(new WildcardFilter("AB*")).size();
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

        Assert.assertEquals(1000000, keyCount);
    }
}
