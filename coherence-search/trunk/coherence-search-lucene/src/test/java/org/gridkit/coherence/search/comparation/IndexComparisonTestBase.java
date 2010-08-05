package org.gridkit.coherence.search.comparation;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import junit.framework.Assert;
import org.gridkit.coherence.search.lucene.TestDocument;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.util.Set;

/**
 * @author Alexander Solovyov
 */

public abstract class IndexComparisonTestBase {

    protected static final int N = 5;
    private static final int RECORD_NUMBER = 1 << 16;

    public ReflectionExtractor[] stringFieldExtractors;
    public ReflectionExtractor[] intFieldExtractors;

    protected NamedCache cache;

    @BeforeClass
    public static void configure() {
        System.setProperty("tangosol.coherence.wka", "localhost");
        System.setProperty("tangosol.coherence.cluster", "index-comparison-test");

         System.setProperty("tangosol.coherence.distributed.localstorage", "false");

        CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("lucene-cache-config.xml"));
    }

    @Before
    public void init() {
        cache = CacheFactory.getCache("distributed-cache");

        stringFieldExtractors = new ReflectionExtractor[N];
        intFieldExtractors = new ReflectionExtractor[N];

        for (int i = 0; i < N; i++) {
            stringFieldExtractors[i] = new ReflectionExtractor("getStringField", new Object[]{i});
            intFieldExtractors[i] = new ReflectionExtractor("getIntField", new Object[]{i});
        }

        setUp();
    }

    protected abstract void setUp();

    private void addData() {
        long t = System.currentTimeMillis();

        cache.putAll(TestDocument.generate(0, RECORD_NUMBER));

        System.out.println("ADD DATA (ms): " + (System.currentTimeMillis() - t) );
    }

    @Test
    public void test() {
        addData();

        System.gc();
        System.out.println("Mem. usage " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());

        // warm up
        int resultSize = entrySet().size();

        System.out.println("Result size: " + resultSize);

        Assert.assertEquals(1, resultSize);


        for (int i = 0; i < 50; i++) {
            assertEntrySetSize();
        }
    }

    private void assertEntrySetSize() {
        long t = System.nanoTime();

        int attemptCount = 1000;
        for (int i = 0; i < attemptCount; i++) {
            entrySet().size();
        }

        System.out.println("TIME: " + (System.nanoTime() - t) / attemptCount );
    }

    protected abstract Set entrySet();
}
