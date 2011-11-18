package org.gridkit.coherence.search.lucene;

import org.apache.lucene.search.Query;
import org.junit.Test;

import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.net.cache.LocalCache;
import com.tangosol.net.cache.WrapperNamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AlwaysFilter;

public class TestAnyFieldQuery {

	@Test
	public void test() {
		
		WrapperNamedCache mock = new WrapperNamedCache(new LocalCache(),"test");
		ContinuousQueryCache cache = new ContinuousQueryCache(mock, AlwaysFilter.INSTANCE);
		
		LuceneDocumentExtractor extractor = new LuceneDocumentExtractor();
		extractor.addText("id", new ReflectionExtractor("getInternalId"));
		extractor.addText("ticker", new ReflectionExtractor("getTicker"));
		extractor.addText("ric", new ReflectionExtractor("getRic"));
		extractor.addText("productName", new ReflectionExtractor("getProductName"));
		
		LuceneSearchFactory factory = new LuceneSearchFactory(extractor);
		factory.createIndex(cache);
		
		Trade trd;
		trd = new Trade();
		trd.internalId = "0000001";
		trd.ticker = "GOOG";
		trd.ric = "goog.sw";
		trd.productName = "Google Inc";
		
		cache.put(1, trd);

		trd = new Trade();
		trd.internalId = "0000002";
		trd.ticker = "IBM";
		trd.ric = "ibm";
		trd.productName = "Intelegent Business Machines (IBM)";

		cache.put(2, trd);
		
		Query query = new AnyFieldTermQuery("GOOG");		
		System.out.println("Looking for " + query + " -> " + cache.keySet(factory.createFilter(query)).size());

		query = new AnyFieldTermQuery("google");		
		System.out.println("Looking for " + query + " -> " + cache.keySet(factory.createFilter(query)).size());

		query = new AnyFieldTermQuery("Google");		
		System.out.println("Looking for " + query + " -> " + cache.keySet(factory.createFilter(query)).size());

		query = new AnyFieldTermQuery("IBM");		
		System.out.println("Looking for " + query + " -> " + cache.keySet(factory.createFilter(query)).size());
	}
	
}
