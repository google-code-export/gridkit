package org.gridkit.coherence.search.comparation;

import java.util.Set;

import junit.framework.Assert;

import org.gridkit.coherence.search.lucene.TestDocument;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AlwaysFilter;

/**
 * @author Alexander Solovyov
 */

public abstract class IndexComparisonTestBase {

    protected static final int RECORD_NUMBER = Integer.getInteger("RECORD_NUMBER", 1 << Integer.getInteger("RECORD_BITS", 16));
    protected static final int N = ((32 - Integer.numberOfLeadingZeros(RECORD_NUMBER - 1) + TestDocument.BITS_PER_ATTRIB - 1) / TestDocument.BITS_PER_ATTRIB);

    public ReflectionExtractor[] stringFieldExtractors;
    public ReflectionExtractor[] intFieldExtractors;

    protected NamedCache cache;

    @BeforeClass
    public static void configure() {
    	if (System.getProperty("tangosol.coherence.wka") == null) {
    		System.setProperty("tangosol.coherence.wka", "localhost");
    	}
        System.setProperty("tangosol.coherence.cluster", "index-comparison-test");

        System.setProperty("tangosol.coherence.distributed.localstorage", "false");

	    System.setProperty("tangosol.pof.enabled", "true");
	    System.setProperty("tangosol.pof.config", "pof-config.xml");
        
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

        int page = 1 << 12;
        for(int i = 0; i < RECORD_NUMBER; i += page) {
        	int j = Math.min(RECORD_NUMBER, i + page);
        	cache.putAll(TestDocument.generate(i, j));
        	System.out.println("Done " + j);
        }

        System.out.println("ADD DATA (ms): " + (System.currentTimeMillis() - t) );
    }

    @Test
    public void test() {
        addData();

        String memusage = (String) cache.aggregate(AlwaysFilter.INSTANCE, new MemUsageAggregator());
        
        System.gc();
        System.out.println("Mem. usage: \n" + memusage);

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
        long deadline = System.currentTimeMillis() + 5000;

        int attemptCount = 1000;
        int i = 0;
        for (i = 0; i < attemptCount; i++) {
            entrySet().size();
            if (i % 10 == 0 && System.currentTimeMillis() > deadline) {
            	break;
            }
        }

        System.out.println((System.nanoTime() - t) / i );
    }

    @SuppressWarnings("unchecked")
	protected abstract Set entrySet();
}
