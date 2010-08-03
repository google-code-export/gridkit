package org.gridkit.coherence.search.comparation;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import java.util.Set;

/**
 * @author Alexander Solovyov
 */

public abstract class ComparationIndexTestBase {

    protected static final int N = 10;
    private static final int RECORD_NUMBER = 1000000;

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
        for (int i = 0; i < RECORD_NUMBER; i++) {
            cache.put(i, new MockIndexedObject(i % N, N));
        }
    }

    @Test
    public void test() {
        addData();

        long t = System.currentTimeMillis();

        int resultSize = entrySet().size();

        System.out.println("TIME: " + (System.currentTimeMillis() - t));

        Assert.assertTrue(resultSize > 1);
        Assert.assertTrue(resultSize == RECORD_NUMBER / N);
    }

    protected abstract Set entrySet();
}
