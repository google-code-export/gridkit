package org.gridkit.coherence.search.comparation;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import java.util.Set;

/**
 * @author Alexander Solovyov
 */

public abstract class ComparationIndexTestBase {
    protected NamedCache cache;

    @BeforeClass
    public static void configure() {
        CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("lucene-cache-config.xml"));
    }

    @Before
    public void init() {
        cache = CacheFactory.getCache("local-cache");
        setUp();
    }

    protected abstract void setUp();

    private void addData() {
        for (int i = 0; i < 1000000; i++) {
            cache.put(i, new MockIndexedObject(String.valueOf(i), i));
        }
    }

    @Test
    public void test() {
        addData();

        long t = System.currentTimeMillis();

        int resultSize = entrySet().size();

        System.out.println("TIME: " + (System.currentTimeMillis() - t));

        Assert.assertEquals(1, resultSize);
    }

    protected abstract Set entrySet();
}
