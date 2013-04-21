/**
 * Copyright 2010 Grid Dynamics Consulting Services, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gridkit.coherence.search.lucene;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DefaultConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;

public abstract class BaseLuceneIndexTest {

    private static final String[] WORD_SET = {
    	"public", "private", "protected", "final", "abstract", 
    	"class", "interface", "pub", "pro", "abs", "static", "void"
    };

	protected NamedCache cache;
	protected LuceneDocumentExtractor extractor = new LuceneDocumentExtractor("text", new ReflectionExtractor("toString"));
	protected LuceneSearchFactory factory = new LuceneSearchFactory(extractor);
	{
//		factory.getEngineConfig().setIndexUpdateQueueSizeLimit(10);
	};
	
	@BeforeClass
	public static void configure() {
		CacheFactory.setConfigurableCacheFactory(new DefaultConfigurableCacheFactory("lucene-cache-config.xml"));
	}
	
	@Before
	public void init() {
		cache = getCache();
		factory.createIndex(cache);		
	}
	
	public void init(int objectCount) {

		cache.clear();
//		long objectCount = 1024;
//		long objectCount = 32;
		
        long rangeStart = 0;
        long rangeFinish = rangeStart + objectCount;
		
        int putSize = 10;
//        long blockTs = System.nanoTime();
//        long blockStart = rangeStart;
        for(long i = rangeStart;  i < rangeFinish; i += putSize) {
            if (i % 1000 == 0) {
//                String stats = "";
//                if (i > blockStart) {
//                    long blockSize = i - blockStart;
//                    long blockTime = System.nanoTime() - blockTs;
//                    double avg = (((double)blockSize) / blockTime) * TimeUnit.SECONDS.toNanos(1);
//                    stats = " block " + blockSize + " in " + TimeUnit.NANOSECONDS.toMillis(blockTime) + "ms, avg " + avg + " put/sec, batchSize " + putSize;
//                }
//                blockTs = System.nanoTime();
//                blockStart = i;
            }
            long j = Math.min(rangeFinish, i + putSize);
            cache.putAll(generateEncodedNumber(i, j, WORD_SET));
        }
	}
	
    static Map<String, String> generateEncodedNumber(long from, long to, String[] wordset) {
        Map<String, String> result = new HashMap<String, String>();
        for (long i = from; i != to; ++i) {
            String text = encodeBitPositions(i, wordset);
            result.put(text, text);
        }
        return result;
    }    
    
    static String encodeBitPositions(long value, String[] charset) {
        StringBuilder builder = new StringBuilder();
        for(int i =0 ; i != charset.length; ++i) {
            if ((value & (1 << i)) != 0) {
            	if (builder.length() > 0) {
            		builder.append(" ");
            	}
                builder.append(charset[i]);
            }
        }
        return builder.toString();
    }
	
	protected abstract NamedCache getCache();
	
	@After
	public void destroyCache() {
		cache.destroy();
	}
	
	@AfterClass
	public static void shutdown() {
		CacheFactory.getCluster().shutdown();
	}
	
	@Test
	public void testQuery32() {
		init(32);
		Assert.assertEquals(32, cache.size());
		Assert.assertEquals(32, query(new MatchAllDocsQuery()).length);
		Assert.assertEquals(2, query("public", "private", "protected", "final").length);
		Assert.assertEquals(4, query("public", "private", "protected").length);
		Assert.assertEquals(8, query("public", "private").length);
		Assert.assertEquals(16, query("private").length);
	}

	@Test
	public void testQuery128() {
		init(128);
		Assert.assertEquals(128, cache.size());
		Assert.assertEquals(128, query(new MatchAllDocsQuery()).length);
//		Assert.assertEquals(2, query("public", "private", "protected", "final").length);
//		Assert.assertEquals(4, query("public", "private", "protected").length);
//		Assert.assertEquals(8, query("public", "private").length);
//		Assert.assertEquals(16, query("private").length);
	}

	@Test
	public void testQuery512() {
		init(512);
		Assert.assertEquals(512, cache.size());
		Assert.assertEquals(512, query(new MatchAllDocsQuery()).length);
//		Assert.assertEquals(2, query("public", "private", "protected", "final").length);
//		Assert.assertEquals(4, query("public", "private", "protected").length);
//		Assert.assertEquals(8, query("public", "private").length);
//		Assert.assertEquals(16, query("private").length);
	}

	@Test
	public void testQuery1024() {
		init(1024);
		Assert.assertEquals(1024, cache.size());
		Assert.assertEquals(1024, query(new MatchAllDocsQuery()).length);
//		Assert.assertEquals(2, query("public", "private", "protected", "final").length);
//		Assert.assertEquals(4, query("public", "private", "protected").length);
//		Assert.assertEquals(8, query("public", "private").length);
//		Assert.assertEquals(16, query("private").length);
	}

	@Test
	public void testQuery2048() {
		init(2048);
		Assert.assertEquals(2048, cache.size());
		Assert.assertEquals(2048, query(new MatchAllDocsQuery()).length);
//		Assert.assertEquals(2, query("public", "private", "protected", "final").length);
//		Assert.assertEquals(4, query("public", "private", "protected").length);
//		Assert.assertEquals(8, query("public", "private").length);
//		Assert.assertEquals(16, query("private").length);
	}

	@Test
	public void testQuery4096() {
		init(4096);
		Assert.assertEquals(4096, cache.size());
		Assert.assertEquals(4096, query(new MatchAllDocsQuery()).length);
//		Assert.assertEquals(2, query("public", "private", "protected", "final").length);
//		Assert.assertEquals(4, query("public", "private", "protected").length);
//		Assert.assertEquals(8, query("public", "private").length);
//		Assert.assertEquals(16, query("private").length);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String[] query(Query query) {
		Set entries = cache.entrySet(factory.createFilter(query));
		List<String> result = new ArrayList<String>();
		for(Map.Entry entry: (Set<Map.Entry>)entries) {
			result.add((String) entry.getValue());
		}
		
		return result.toArray(new String[0]);		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String[] query(String... terms) {
		BooleanQuery query = new BooleanQuery();
		for(String term: terms) {
			TermQuery tq = new TermQuery(new Term("text", term));
			query.add(tq, Occur.MUST);
		}
		Set entries = cache.entrySet(factory.createFilter(query));
		List<String> result = new ArrayList<String>();
		for(Map.Entry entry: (Set<Map.Entry>)entries) {
			result.add((String) entry.getValue());
		}
		
		return result.toArray(new String[0]);
	}
}
