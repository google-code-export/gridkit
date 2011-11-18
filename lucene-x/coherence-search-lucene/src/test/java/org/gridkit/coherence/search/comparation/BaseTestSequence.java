package org.gridkit.coherence.search.comparation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;

public abstract class BaseTestSequence {
	
	static void println() {
	    System.out.println();
	}
	
	static void println(String text) {
	    System.out.println(String.format("[%1$tH:%1$tM:%1$tS.%1$tL] ", new Date()) + text);	
	}
	
	static void sysProp(String propName, String value) {
		if (System.getProperty(propName) == null) {
			System.setProperty(propName, value);
		}
		System.out.println("sysProp: " + propName + ": " + System.getProperty(propName));
	}
	
	public abstract void createIndexes(NamedCache cache, List<String> fields);
	
	public abstract Filter createFilter(QueryCondition[] conditions);

	
	TestDocumentGenerator docGen = new TestDocumentGenerator();
	int docCount = 100000;
	int batchSize = 20;
	Map<String, String> testStats = new LinkedHashMap<String, String>();
	private NamedCache cache;
	
	public void start() {
		sysProp("tangosol.pof.enabled", "true");
		sysProp("tangosol.pof.config", "pof-config.xml");
		sysProp("tangosol.coherence.cacheconfig", "index-test-cache-config.xml");
		sysProp("tangosol.coherence.distributed.localstorage", "true");

		if (System.getProperty("large-dataset") == null) {		    
		    start100k();
		}
		else {
			docCount = 1000000;
			start100k();
		}
	}

	public void start100k() {
		

	    
		docGen.setDocCount(docCount);
		
		docGen.addField("Sparse1", 0.1);
		docGen.addField("Sparse2s", 0.1);
		
		docGen.addField("A1", 1);
		docGen.addField("A2s", 1);

		docGen.addField("B1", 10);
		docGen.addField("B2s", 10);

		docGen.addField("C1", 50);
		docGen.addField("C2s", 50);

		docGen.addField("D1", docCount / 100);
		docGen.addField("D2s", docCount / 100);

		docGen.addField("E1", docCount / 10);
		docGen.addField("E2s", docCount / 10);
		docGen.addField("E3", docCount / 10);
		docGen.addField("E4", docCount / 10);

		docGen.addField("H1", docCount / 2);
		docGen.addField("H2", docCount / 2);
		
		initCache();
		
		println("Generating " + docCount + " documents");
		
		Map<Integer, Object> batch = new HashMap<Integer, Object>();
		for(int i = 0; i != docCount; ++i) {
			if (i % batchSize ==0) {
				putToCache(batch);
				batch.clear();
			}
			Map<String, String> doc = docGen.getDoc(i);
			batch.put(i, doc);
			if ((i + 1) % 10000 == 0) {
				println("Loaded " + (i + 1));
			}
		}
		putToCache(batch);
		
		println("Creating indexes");
		
		createIndexes();
		
		println("Cache loaded");
		
		runSingleCriteriaQueries();
		runRangeCriteriaQueries();
		runComplexCriteriaQueries();
		runComplexCriteriaQueries2();

		testStats.clear();
		println("Rerun tests");

		for (int i = 0; i != 5; ++i) {
			runSingleCriteriaQueries();
			runRangeCriteriaQueries();
			runComplexCriteriaQueries();
			runComplexCriteriaQueries2();
		}
		
		println("Done");
		
		for(Map.Entry<String, String> entry: testStats.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
	}

	protected void createIndexes() {
		createIndexes(cache, docGen.getFieldList());
	}

	protected void putToCache(Map<Integer, Object> batch) {
		cache.putAll(batch);
	}

	protected void initCache() {
		cache = CacheFactory.getCache("objects");
	}

//	private void testSingleCriteria(Random rnd, NamedCache cache, String attrib) {
//		List<Filter> filters;
//
//		double c = docGen.getSelectivity(attrib);
//		
//		println("Query test. Single criterion " + attrib + "(C = " + c + ")");
//		filters = new ArrayList<Filter>();
//		int n = (int) (10000 / Math.sqrt(c));
//		if (n > 1000) {
//			n = 1000;
//		}
//		if (n < 50) {
//			n = 50;
//		}
//		for(int i = 0; i != n; ++i) {
//			String field = attrib;
//			String term = docGen.getRandomTerm(rnd, field);
//			Filter f = createFilter(new QueryCondition[]{new QueryCondition(field, term)});
//			filters.add(f);
//		}
//		
//		testFilters(cache, filters);
//	}

//	private void testComplexCriteria(Random rnd, NamedCache cache, String attrib1, String attrib2) {
//		List<Filter> filters;
//
//		double c1 = docGen.getSelectivity(attrib1);
//		double c2 = docGen.getSelectivity(attrib2);
//		
//		println("Query test. Complex criterion " + attrib1 + "(C = " + c1 + "), " + attrib2 + "(C = " + c2 + ")");
//		filters = new ArrayList<Filter>();
//		int n = 100;
//		for(int i = 0; i != n; ++i) {
//			QueryCondition q1 = new QueryCondition(attrib1, docGen.getRandomTerm(rnd, attrib1));
//			QueryCondition q2 = new QueryCondition(attrib2, docGen.getRandomTerm(rnd, attrib2));
//			Filter f = createFilter(new QueryCondition[]{q1, q2});
//			filters.add(f);
//		}
//		
//		testFilters(cache, filters);
//	}
	
//	private void testRangeCriteria(Random rnd, NamedCache cache, String attrib, int termRange) {
//		List<Filter> filters;
//		
//		double c = docGen.getSelectivity(attrib);
//		
//		println("Query test. Range criterion " + attrib + "(C = " + c + "), term range " + termRange);
//		filters = new ArrayList<Filter>();
//		int n = (int) (10000 / Math.sqrt(c));
//		if (n > 1000) {
//			n = 1000;
//		}
//		if (n < 50) {
//			n = 50;
//		}
//		
//		for(int i = 0; i != n; ++i) {
//			String field = attrib;
//			String[] terms = docGen.getRandomRange(rnd, field, termRange);
//			Filter f = createFilter(new QueryCondition[]{new QueryCondition(field, terms[0], terms[1])});
//			filters.add(f);
//		}
//		
//		testFilters(cache, filters);
//	}
	
	private void runSingleCriteriaQueries() {
		
		Random rnd = new Random(0);

		new QueryBuilder().single("Sparse1").test();
		new QueryBuilder().single("Sparse2s").test();
		new QueryBuilder().single("A1").test();
		new QueryBuilder().single("A2s").test();
		new QueryBuilder().single("B1").test();
		new QueryBuilder().single("B2s").test();
		new QueryBuilder().single("C1").test();
		new QueryBuilder().single("C2s").test();
//		new QueryBuilder().single("D1").test(cache);
//		new QueryBuilder().single("D2s").test(cache);
	}

	private void runRangeCriteriaQueries() {
		Random rnd = new Random(0);

		new QueryBuilder().range("Sparse1", 10).test();
		new QueryBuilder().range("Sparse2s", 10).test();
		new QueryBuilder().range("A1", 10).test();
		new QueryBuilder().range("A2s", 10).test();
		new QueryBuilder().range("B1", 10).test();
		new QueryBuilder().range("B2s", 10).test();
		new QueryBuilder().range("C1", 10).test();
		new QueryBuilder().range("C2s", 10).test();
		new QueryBuilder().range("D1", 10).test();
		new QueryBuilder().range("D2s", 10).test();
	}

	private void runComplexCriteriaQueries() {
		Random rnd = new Random(0);

		new QueryBuilder().single("D1").single("D2s").test();
		new QueryBuilder().single("A1").single("D1").test();
		new QueryBuilder().single("D1").single("A1").test();
		new QueryBuilder().single("A1").single("E1").test();
		new QueryBuilder().single("E1").single("A1").test();
		new QueryBuilder().single("B1").single("D1").test();
		new QueryBuilder().single("D1").single("B1").test();
		new QueryBuilder().single("B1").single("E1").test();
		new QueryBuilder().single("E1").single("B1").test();
		new QueryBuilder().single("C1").single("D1").test();
		new QueryBuilder().single("D1").single("C1").test();
		new QueryBuilder().single("C1").single("E1").test();
		new QueryBuilder().single("E1").single("C1").test();
		new QueryBuilder().single("D1").single("E1").test();
		new QueryBuilder().single("E1").single("D1").test();
		new QueryBuilder().single("E3").single("E4").test();
		new QueryBuilder().single("E1").single("E3").single("E4").test();
		
		new QueryBuilder()
			.single("D1").single("E1").single("E3").single("E4").test();
		new QueryBuilder()
			.single("E1").single("E3").single("E4").single("D1").test();
		
		new QueryBuilder()
			.range("A2s", 10000).single("E1").single("E3").single("E4").test();
		new QueryBuilder()
			.single("E1").single("E3").single("E4").range("A2s", 10000).test();

		new QueryBuilder()
			.multi("D1", 10).single("E1").single("E3").single("E4").test();
		new QueryBuilder()
			.single("E1").single("E3").single("E4").multi("D1", 10).test();
		
	}

	private void runComplexCriteriaQueries2() {
		Random rnd = new Random(0);

//		new QueryBuilder()
//			.range("A2s", 10000).single("E1").single("E3").single("E4").test(cache);
//		new QueryBuilder()
//			.single("E1").single("E3").single("E4").range("A2s", 10000).test(cache);
//
//		new QueryBuilder()
//			.multi("D1", 10).single("E1").single("E3").single("E4").test(cache);
//		new QueryBuilder()
//			.single("E1").single("E3").single("E4").multi("D1", 10).test(cache);
		
		new QueryBuilder()
			.single("H1").single("E1").single("E3").single("E4").test();
		new QueryBuilder()
			.single("E1").single("E3").single("E4").single("H1").test();		
	}

	private void testFilters(String experimentName, NamedCache cache, List<Filter> filters) {
		
		long totalTime = 0;
		long totalResultSet = 0;
		int count = 0;
		
		long deadline = System.currentTimeMillis() + 30000; 
		for(Filter filter: filters) {
			if (System.currentTimeMillis() > deadline) {
				break;
			}
			++count;
			long s = System.nanoTime();
			int r = evaluateFilter(cache, filter);
			long time = System.nanoTime() - s;
//			if (time > TimeUnit.MILLISECONDS.toNanos(200)) {
//				// throwing away GC pauses
//				continue;
//			}
			totalTime += time;
			totalResultSet += r;
		}
		
		String stat = testStats.get(experimentName);
		stat = stat == null ? String.format("%.3f", totalTime / 1000000d / count) : stat + "\t" + String.format("%.3f", totalTime / 1000000d / count);
		testStats.put(experimentName, stat);
		
		println("Filter stats: average time " 
				+ String.format("%.3fms", totalTime / 1000000d / count)
				+ " average result set "
				+ String.format("%.3f", totalResultSet * 1d / count)
		);		
	}

	protected int evaluateFilter(NamedCache cache, Filter filter) {
		int r = cache.keySet(filter).size();
		return r;
	}

	static class QueryCondition {
		String field;
		String[] terms;
		boolean rangeQuery;
		
		public QueryCondition(String field, String term) {
			this.field = field;
			this.terms = new String[]{term};
			this.rangeQuery = false;
		}

		public QueryCondition(String field, String[] terms) {
			this.field = field;
			this.terms = terms;
			this.rangeQuery = false;
		}
		
		public QueryCondition(String field, String low, String high) {
			this.field = field;
			this.terms = new String[]{low, high};
			this.rangeQuery = true;
		}
	}
	
	public class QueryBuilder {

		List<FilterFactory> filters = new ArrayList<BaseTestSequence.FilterFactory>();

		public double matchProbability() {
			double p = 1d;
			for(FilterFactory ff : filters) {
				p *= ff.matchProbability();
			}
			return p;
		}

		public QueryCondition[] generate(Random rnd) {
			QueryCondition[] qc = new QueryCondition[filters.size()];
			for(int i = 0; i != filters.size(); ++i) {
				qc[i] = filters.get(i).generate(rnd);
			}
			return qc;
		}
		
		public String describe() {
			StringBuffer buf = new StringBuffer();
			for(FilterFactory ff: filters) {
				if (buf.length() != 0) {
					buf.append(" & ");
				}
				buf.append(ff.describe());
			}
			double rs = matchProbability() * docGen.getDocCount();
			buf.append(". Expected result set size " + rs);
			return buf.toString();
		}
		
		public QueryBuilder single(String field) {
			filters.add(new SingleTermQuery(field));
			return this;
		}

		public QueryBuilder multi(String field, int n) {
			filters.add(new MultiTermQuery(field, n));
			return this;
		}

		public QueryBuilder range(String field, int n) {
			filters.add(new TermRangeQuery(field, n));
			return this;
		}
		
		public QueryBuilder test() {
	
			println("Query test: " + describe());
			
			Random rnd = new Random();
			List<Filter> filters = new ArrayList<Filter>();
			
			int n = 500;
			for(int i = 0; i != n; ++i) {
				filters.add(createFilter(generate(rnd)));
			}
			testFilters(describe(), cache, filters);
			return this;
		}
	}
	
	public interface FilterFactory {
		
		public double matchProbability();
		
		public QueryCondition generate(Random rnd);
		
		public String describe();
		
	}
	
	public class SingleTermQuery implements FilterFactory {

		private String fieldName;
		
		public SingleTermQuery(String fieldName) {
			this.fieldName = fieldName;
		}

		@Override
		public double matchProbability() {
			return docGen.getSelectivity(fieldName) / docGen.getDocCount();
		}

		@Override
		public QueryCondition generate(Random rnd) {
			String term = docGen.getRandomTerm(rnd, fieldName);
			return new QueryCondition(fieldName, term);
		}

		@Override
		public String describe() {
			return "Single term " + fieldName + "(C=" + docGen.getSelectivity(fieldName) + ")";
		}
	}
	
	private class MultiTermQuery implements FilterFactory {

		private String fieldName;
		private int termNumber;

		public MultiTermQuery(String fieldName, int termNumber) {
			this.fieldName = fieldName;
			this.termNumber = termNumber;
		}

		@Override
		public double matchProbability() {
			return termNumber * docGen.getSelectivity(fieldName) / docGen.getDocCount();
		}

		@Override
		public QueryCondition generate(Random rnd) {
			String[] terms = new String[termNumber];
			for(int i = 0; i != terms.length; ++i) {
				terms[i] = docGen.getRandomTerm(rnd, fieldName);
			}
			return new QueryCondition(fieldName, terms);
		}

		@Override
		public String describe() {
			return "Multi term " + fieldName + "(C=" + docGen.getSelectivity(fieldName) + "), " + termNumber + " terms";
		}
	}
	
	private class TermRangeQuery implements FilterFactory {

		private String fieldName;
		private int range;

		public TermRangeQuery(String fieldName, int range) {
			this.fieldName = fieldName;
			this.range = range;
		}

		@Override
		public double matchProbability() {
			return range * docGen.getSelectivity(fieldName) / docGen.getDocCount();
		}

		@Override
		public QueryCondition generate(Random rnd) {
			String[] terms = docGen.getRandomRange(rnd, fieldName, range);
			return new QueryCondition(fieldName, terms[0], terms[1]);
		}

		@Override
		public String describe() {
			return "Term range " + fieldName + "(C=" + docGen.getSelectivity(fieldName) + "), " + range + " terms";
		}
	}		
}
