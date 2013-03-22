package org.gridkit.coherence.utils.pof;

import java.util.Collections;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.aggregator.ReducerAggregator;

public class PofMultiExtractorTest {

	private static NamedCache CACHE;
	
	@BeforeClass
	public static void initCache() {
		CACHE = new DefaultConfigurableCacheFactory("test-pof-cache-config.xml").ensureCache("objects-test", null);
	}

	@AfterClass
	public static void destroyCache() {
		CACHE.destroy();
	}
	
	@Test
	public void test_extractor_with_array() {
		
		Object[] values = {"A", "B", "C", "D", "E"};
		CACHE.put("A", values);
		Object proj = CACHE.aggregate(Collections.singleton("A"), new ReducerAggregator(new PofMultiExtractor(4, 2, 0)));
		Assert.assertEquals("{A=[E, C, A]}", proj.toString());		
	}
	
}
