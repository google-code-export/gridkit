package org.gridkit.coherence.search.timeseries;

import java.util.Collection;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.tangosol.util.aggregator.DistinctValues;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AlwaysFilter;

public class BasicFunctional extends AbstractTimeseriesFunctionalTest {

	TimeSeriesHelper<String, SampleValue, Long> helper = new TimeSeriesHelper<String, SampleValue, Long>(
			new KeyExtractor("getSerieKey"), 
			new ReflectionExtractor("getTimestamp"));
	
	@Before
	public void cleanUp() {
//		helper.destroyIndex(testCache);
		testCache.clear();
		helper.createIndex(testCache);
	}
	
	private void fixture1() {
		testCache.put(key("A", 1), version("A-10", 10));
		testCache.put(key("A", 2), version("A-12", 12));
		testCache.put(key("A", 3), version("A-20", 20));
		testCache.put(key("B", 1), version("B-8", 8));
		testCache.put(key("B", 2), version("B-11", 11));
		testCache.put(key("B", 3), version("B-13", 13));
		testCache.put(key("B", 4), version("B-21", 21));
	}

	@Test
	public void floorSize() {
		fixture1();
		
		Assert.assertEquals(0, testCache.keySet(helper.floor(7l)).size());
		Assert.assertEquals(1, testCache.keySet(helper.floor(8l)).size());
		Assert.assertEquals(1, testCache.keySet(helper.floor(9l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(10l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(15l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(25l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.floor(null)).size());
	}

	@Test
	public void ceilingSize() {
		fixture1();
		
		Assert.assertEquals(0, testCache.keySet(helper.ceiling(25l)).size());
		Assert.assertEquals(1, testCache.keySet(helper.ceiling(21l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(20l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(15l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(13l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(7l)).size());
		Assert.assertEquals(2, testCache.keySet(helper.ceiling(null)).size());
	}

	@Test
	public void gettingElements() {
		fixture1();
		
		Assert.assertEquals(0, testCache.keySet(helper.ceiling("A", 21l)).size());
	}
	
	@Test
	public void aggregatorTest() {
		fixture1();
		
		Collection<Object> result = (Collection<Object>) testCache.aggregate(AlwaysFilter.INSTANCE, new SurafceAggregator(10));
		TreeSet<Object> sq = new TreeSet<Object>(result);
		
		Assert.assertEquals(sq.toString(), "[A-10, B-8]");
		
		result = (Collection<Object>) testCache.aggregate(helper.floor(10l), new DistinctValues("getValue"));
		sq = new TreeSet<Object>(result);

		Assert.assertEquals(sq.toString(), "[A-10, B-8]");
	}
}
