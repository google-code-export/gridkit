package org.gridkit.coherence.search.comparation;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;
import java.lang.management.ManagementFactory;

/**
 * @author Alexander Solovyov
 */

public abstract class ComparationIndexTestBase {

    protected static final int N = 10;
    private static final int RECORD_NUMBER = 100000;
    private static final int STEP = N * 10000;

    public ReflectionExtractor[] stringFieldExtractors;
    public ReflectionExtractor[] intFieldExtractors;

    protected NamedCache cache;

    @BeforeClass
    public static void configure() {
        CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("lucene-cache-config.xml"));
    }

    @Before
    public void init() {
        cache = CacheFactory.getCache("local-cache");

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

        for (int i = 0; i < RECORD_NUMBER; i++) {
            cache.put(i, new MockIndexedObject(i, N, STEP));
        }

        System.out.println("ADD DATA (ms): " + (System.currentTimeMillis() - t) );
    }

    @Test
    public void test() {
        addData();

        System.gc();
        System.out.println("Mem. usage " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());

        // warm up
        int resultSize = entrySet().size();

        Assert.assertTrue(resultSize >= 1);
        Assert.assertTrue(resultSize <= RECORD_NUMBER / STEP);

        System.out.println("Result size: " + resultSize);

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
