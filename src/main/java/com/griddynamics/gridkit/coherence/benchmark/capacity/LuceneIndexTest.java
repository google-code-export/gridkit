package com.griddynamics.gridkit.coherence.benchmark.capacity;

import com.griddynamics.gridkit.coherence.index.lucene.LuceneExtractor;
import com.griddynamics.gridkit.coherence.index.lucene.PrefixFilter;
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
        System.setProperty("benchmark-default-scheme", "simple-distributed-scheme");
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

//        cache.put("ABCdfd", "ABCsss");
//        cache.put("ABCdfd222", "ABCssseee");

        for (int i = 0; i < 1000; i++) {
            cache.put("ABC" + i, "ABC" + i);
        }

        cache.put("ABCff", "dd");
        cache.put("ABCsddds", "dfsf");
        cache.put("ABCsdfsd", "sgfsd");

        int keyCount = cache.keySet(new PrefixFilter("AB")).size();
        Assert.assertEquals(1000, keyCount);
    }
}
