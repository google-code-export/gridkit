package org.gridkit.coherence.search.comparation.subquery;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.memory.MemoryIndex;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.DocIdBitSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.Version;
import org.gridkit.util.formating.Formats;

public class SubQueryTest {

	private RAMDirectory directory = new RAMDirectory();
	private Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_33);
	
	private TextCorpusGenerator textGen = new TextCorpusGenerator() {{
		
		addWords(5, "A", 30);
		addWords(5, "B", 60);
		addWords(10, "C", 120);
		
		setWordsPerText(256);
		
	}};
	
	private static int HINT_SHARDS = 1;
	
	private Query[] hintedQueries = {createPat1(0), createPat1(1), createPat1(2), createPat1(3)};
	private int[] hintFreqs = new int[hintedQueries.length];
	
	public void generate(int count) throws IOException {
		
		IndexWriter iw = new IndexWriter(directory, analyzer, true, MaxFieldLength.UNLIMITED);
		
		System.out.format("[%s] Starting text generation\n", Formats.currentDatestamp());
		
		for(int i = 0; i != count; ++i) {
			addDoc(iw, i);
			
			if ((i + 1) % (count / 10) == 0) {
				System.out.format("[%s] Done %d\n", Formats.currentDatestamp(), i + 1);
			}
		}
		
		System.out.format("[%s] Text generation finished\n", Formats.currentDatestamp());
		System.out.format("[%s] Hint freqs %s\n", Formats.currentDatestamp(), Arrays.toString(hintFreqs));
		iw.commit();		
	}

	public void generate(int... ids) throws IOException {
		
		IndexWriter iw = new IndexWriter(directory, analyzer, true, MaxFieldLength.UNLIMITED);
		
		System.out.format("[%s] Starting text generation\n", Formats.currentDatestamp());
		
		for(int i : ids) {
			addDoc(iw, i);
			
		}
		
		System.out.format("[%s] Text generation finished\n", Formats.currentDatestamp());
		System.out.format("[%s] Hint freqs %s\n", Formats.currentDatestamp(), Arrays.toString(hintFreqs));
		iw.commit();		
	}

	boolean hintAll = false;
	
	private void addDoc(IndexWriter iw, int i) throws IOException, CorruptIndexException {
		String text = textGen.getText(i);
		Document doc = new Document();
		doc.add(new Field("text", text, Store.NO, Index.ANALYZED_NO_NORMS, TermVector.WITH_POSITIONS_OFFSETS));
		
		if (addMarkers(i, doc, text)) {		
			iw.addDocument(doc);
		}
	}
	
	private boolean addMarkers(int docId, Document doc, String text) throws IOException {
		
		MemoryIndex mi = new MemoryIndex();
		mi.addField("text", text, analyzer);		
		
		String hints = "";
		for(int n = 0; n != hintedQueries.length; ++n) {
			if (hintAll || mi.createSearcher().search(hintedQueries[n], 1).totalHits > 0) {
				hints += "H"+ n + "_" + (docId % HINT_SHARDS) + " ";
				++hintFreqs[n];
			}
		}
		
		if (hints.length() > 0) {
			doc.add(new Field("hint", hints, Store.NO, Index.ANALYZED_NO_NORMS, TermVector.NO));
		}
		
		return true;
	}

	private void testQueries() throws IOException {
				
//		test_A1_B1__proximity();
//		test_A1_B1_A2__proximity();
//		test_A1_B1_B2orB3__proximity();
//		test_A1_B1_C1orC2orC3__proximity();
	
		test_Pat1();
		test_DoublePat1();
	}
	
	
	private void test_Pat1() throws IOException {
		
		testQuery(createPat1(0), "Pat1[0]");
		testSpanTerms(createPat1(0), "Pat1[0]");
		
		testQuery(createPat1(0), "Pat1[0]", "H0");
		testQuery(createPat1(1), "Pat1[1]");
		testQuery(createPat1(1), "Pat1[1]", "H1");
		testQuery(createPat1(2), "Pat1[2]");
		testQuery(createPat1(2), "Pat1[2]", "H2");
		
	}

	private void test_DoublePat1() throws IOException {
		
		SpanQuery q0 = createPat1(0);
		SpanQuery q1 = createPat1(1);
		SpanQuery q2 = createPat1(2);
		SpanQuery q3 = createPat1(3);
		
		
		SpanQuery[] qq1 = {q0, q1};
		SpanQuery[] qq2 = {q1, q2};
		SpanQuery[] qq3 = {q2, q3};
		
		SpanNearQuery nq1 = new SpanNearQuery(qq1, 10, true);
		SpanNearQuery nq2 = new SpanNearQuery(qq2, 10, true);
		SpanNearQuery nq3 = new SpanNearQuery(qq3, 10, true);
		
		testQuery(nq1, "Pat1[0] next/10 Pat1[1]");
		testQuery(nq1, "Pat1[0] next/10 Pat1[1]", "H0", "H1");
		testQuery(nq2, "Pat1[1] next/10 Pat1[2]");
		testQuery(nq2, "Pat1[1] next/10 Pat1[2]", "H1", "H2");
		testQuery(nq3, "Pat1[2] next/10 Pat1[3]");
		testQuery(nq3, "Pat1[2] next/10 Pat1[3]", "H2", "H3");
		
	}

	private void test_A1_B1__proximity() throws IOException {
		
		SpanTermQuery qA1 =new SpanTermQuery(new Term("text", "A1"));
		SpanTermQuery qB1 =new SpanTermQuery(new Term("text", "B1"));
		
		SpanQuery[] spans = {qA1, qB1};
		
		SpanNearQuery span2 = new SpanNearQuery(spans, 2, false);
		SpanNearQuery span4 = new SpanNearQuery(spans, 4, false);
		SpanNearQuery span8 = new SpanNearQuery(spans, 8, false);
		
		
		testQuery(span2, "A1, B1 near/2");
		testQuery(span4, "A1, B1 near/4");
		testQuery(span8, "A1, B1 near/8");
		
	}

	private void test_A1_B1_A2__proximity() throws IOException {
		
		SpanTermQuery qA1 =new SpanTermQuery(new Term("text", "A1"));
		SpanTermQuery qB1 =new SpanTermQuery(new Term("text", "B1"));
		SpanTermQuery qA2 =new SpanTermQuery(new Term("text", "A2"));
		
		SpanQuery[] spans = {qA1, qB1, qA2};
		
		SpanNearQuery span2 = new SpanNearQuery(spans, 2, false);
		SpanNearQuery span4 = new SpanNearQuery(spans, 4, false);
		SpanNearQuery span8 = new SpanNearQuery(spans, 8, false);
		
		
		testQuery(span2, "A1, B1, A2 near/2");
		testQuery(span4, "A1, B1, A2 near/4");
		testQuery(span8, "A1, B1, A2 near/8");
		
	}

	private void test_A1_B1_B2orB3__proximity() throws IOException {
		
		SpanTermQuery qA1 =new SpanTermQuery(new Term("text", "A1"));
		SpanTermQuery qB1 =new SpanTermQuery(new Term("text", "B1"));
		SpanTermQuery qB2 =new SpanTermQuery(new Term("text", "B2"));
		SpanTermQuery qB3 =new SpanTermQuery(new Term("text", "B3"));
		
		SpanOrQuery qB2orB3 =new SpanOrQuery(qB2, qB3);
		
		SpanQuery[] spans = {qA1, qB1, qB2orB3};
		
		SpanNearQuery span2 = new SpanNearQuery(spans, 2, false);
		SpanNearQuery span4 = new SpanNearQuery(spans, 4, false);
		SpanNearQuery span8 = new SpanNearQuery(spans, 8, false);
		
		
		testQuery(span2, "A1, B1, (B2 or B3) near/2");
		testQuery(span4, "A1, B1, (B2 or B3) near/4");
		testQuery(span8, "A1, B1, (B2 or B3) near/8");
		
	}

	private void test_A1_B1_C1orC2orC3__proximity() throws IOException {
		
		SpanTermQuery qA1 =new SpanTermQuery(new Term("text", "A1"));
		SpanTermQuery qB1 =new SpanTermQuery(new Term("text", "B1"));
		
		SpanTermQuery qC1 =new SpanTermQuery(new Term("text", "C1"));
		SpanTermQuery qC2 =new SpanTermQuery(new Term("text", "C2"));
		SpanTermQuery qC3 =new SpanTermQuery(new Term("text", "C3"));
		
		SpanOrQuery qC1orC2orC3 =new SpanOrQuery(qC1, qC2, qC3);
		
		SpanQuery[] spans = {qA1, qB1, qC1orC2orC3};
		
		SpanNearQuery span2 = new SpanNearQuery(spans, 2, false);
		SpanNearQuery span4 = new SpanNearQuery(spans, 4, false);
		SpanNearQuery span8 = new SpanNearQuery(spans, 8, false);
		
		
		testQuery(span2, "A1, B1, (C1 or C2 or C3) near/2");
		testQuery(span4, "A1, B1, (C1 or C2 or C3) near/4");
		testQuery(span8, "A1, B1, (C1 or C2 or C3) near/8");
		
	}
	
	private SpanQuery createPat1(int mod, int... part) {
		
		SpanTermQuery qA1 =new SpanTermQuery(new Term("text", "A" + (1 + mod)));
		SpanTermQuery qB1 =new SpanTermQuery(new Term("text", "B" + (1 + mod)));
		SpanTermQuery qB2 =new SpanTermQuery(new Term("text", "B" + (2 + mod)));
		
		SpanTermQuery qC1 =new SpanTermQuery(new Term("text", "C" + (1 + 10 * mod)));
		SpanTermQuery qC2 =new SpanTermQuery(new Term("text", "C" + (2 + 10 * mod)));
		SpanTermQuery qC3 =new SpanTermQuery(new Term("text", "C" + (3 + 10 * mod)));
		SpanTermQuery qC4 =new SpanTermQuery(new Term("text", "C" + (4 + 10 * mod)));
		SpanTermQuery qC5 =new SpanTermQuery(new Term("text", "C" + (5 + 10 * mod)));
		SpanTermQuery qC6 =new SpanTermQuery(new Term("text", "C" + (6 + 10 * mod)));
		SpanTermQuery qC7 =new SpanTermQuery(new Term("text", "C" + (7 + 10 * mod)));
		SpanTermQuery qC8 =new SpanTermQuery(new Term("text", "C" + (8 + 10 * mod)));
		SpanTermQuery qC9 =new SpanTermQuery(new Term("text", "C" + (9 + 10 * mod)));
		SpanTermQuery qC10 =new SpanTermQuery(new Term("text", "C" + (10 + 10 * mod)));
		
		SpanOrQuery qG1 = new SpanOrQuery(qB1, qC1, qC2);
		SpanOrQuery qG2 = new SpanOrQuery(qB2, qC3, qC4);
		SpanOrQuery qG3 = new SpanOrQuery(qC5, qC6, qC7, qC8, qC9, qC10);
	
		if (part.length > 0) {
			switch(part[0]) {
			case 0: return qG1;
			case 1: return qG2;
			case 2: return qG3;
			}
		}
		
		SpanQuery[] qq = {qA1, qG1, qG2};
		SpanNearQuery qnear = new SpanNearQuery(qq, 8, false);

		if (part.length > 0) {
			switch(part[0]) {
			case 3: return qnear;
			}
		}

		SpanNotQuery fq = new SpanNotQuery(qnear, qG3);
		
		return fq;
	}

	private void testQuery(Query query, String name) throws IOException {
		
		
		IndexSearcher searcher = new IndexSearcher(directory);

		Query q = new ConstantScoreQuery(query);
//		Query q = query;

		if (!testOnce) {
			searcher.search(q, 100);
			searcher.search(q, 100);
			searcher.search(q, 100);
			searcher.search(q, 100);
		}
		
		long start = System.nanoTime();
		int count = 0;
		long deadline = start + TimeUnit.SECONDS.toNanos(5);
		
		int totalHits = 0;
		
		while(deadline > System.nanoTime()) {
			
			++count; 
			final int[] docCount = {0};
			searcher.search(q, new Collector() {
				
				@Override
				public void setScorer(Scorer scorer) throws IOException {
				}
				
				@Override
				public void setNextReader(IndexReader reader, int docBase)	throws IOException {
				}
				
				@Override
				public void collect(int doc) throws IOException {
					docCount[0]++;					
				}
				
				@Override
				public boolean acceptsDocsOutOfOrder() {
					return true;
				}
			});
			totalHits = docCount[0];
		
			if (testOnce) {
				break;
			}
		}
		
		long time = System.nanoTime() - start;
		long qtime = TimeUnit.NANOSECONDS.toMillis(time / count);
		
		System.out.format("Query %s (%d hits) - %dms\n", name, totalHits, qtime);
		
	}

	private boolean testOnce = false;
	
	private void testSpanTerms(Query query, String name) throws IOException {
		
		
		IndexSearcher searcher = new IndexSearcher(directory);
		
		Set<Term> terms = new TreeSet<Term>();
		query.extractTerms(terms);
		String[] termText = new String[terms.size()];
		int n = 0;
		for(Term term: terms) {
			termText[n++] = term.text();
		}
		
		
//		Query q = new ConstantScoreQuery(query);
//		Query q = query;
		Query q = new TermSetQuery("text", termText);
		
		searcher.search(q, 100);
		searcher.search(q, 100);
		searcher.search(q, 100);
		searcher.search(q, 100);
		
		long start = System.nanoTime();
		int count = 0;
		long deadline = start + TimeUnit.SECONDS.toNanos(5);
		
		int totalHits = 0;
		
		while(deadline > System.nanoTime()) {
			
			++count; 
			final int[] docCount = {0};
			searcher.search(q, new Collector() {
				
				@Override
				public void setScorer(Scorer scorer) throws IOException {
				}
				
				@Override
				public void setNextReader(IndexReader reader, int docBase)	throws IOException {
				}
				
				@Override
				public void collect(int doc) throws IOException {
					docCount[0]++;					
				}
				
				@Override
				public boolean acceptsDocsOutOfOrder() {
					return true;
				}
			});
			totalHits = docCount[0];
			
		}
		
		long time = System.nanoTime() - start;
		long qtime = TimeUnit.NANOSECONDS.toMillis(time / count);
		
		System.out.format("Term set %s (%d hits) - %dms\n", terms.toString(), totalHits, qtime);
		
	}

	private void testQuery(Query query, String name, String... hints) throws IOException {
		
		BooleanQuery bq =  new BooleanQuery();
		initHints(bq, hints);
		
//		bq.add(query, Occur.SHOULD);
//		bq.setMinimumNumberShouldMatch(hints.length + 1);
		
		IndexSearcher searcher = new IndexSearcher(directory);

		Term[] hterms = new Term[hints.length];
		for(int i = 0; i != hints.length; ++i) {
			hterms[i] = new Term("hint", hints[i] + "_0");
		}
		
		Query q = new ConstantScoreQuery(query);
		q = new FilteredQuery(q, hterms);

//		final OpenBitSet mask = new OpenBitSet(searcher.maxDoc());
//		for(int i = 0; i != mask.size()/1000; ++i) {
//			mask.fastSet(1000 * i);
//		}
		
		if (!testOnce) {
			searcher.search(q, 100);
			searcher.search(q, 100);
			searcher.search(q, 100);
			searcher.search(q, 100);
		}
		
		long start = System.nanoTime();
		int count = 0;
		long deadline = start + TimeUnit.SECONDS.toNanos(5);
		
		int totalHits = 0;
		
		while(deadline > System.nanoTime()) {
			
			++count; 

			TopDocs td = searcher.search(q, 1);
			totalHits = td.totalHits;
			
			if (testOnce) {
				break;
			}
		}
		
		long time = System.nanoTime() - start;
		long qtime = TimeUnit.NANOSECONDS.toMillis(time / count);
		
		System.out.format("Query %s (%d hits) - %dms (Hints %s)\n", name, totalHits, qtime, Arrays.toString(hints));

		BooleanQuery ho =  new BooleanQuery();
		initHints(ho, hints);
		ho.setMinimumNumberShouldMatch(hints.length);

		if (!testOnce) {
			testQuery(new FilteredQuery(ho, hterms), String.format("Hints only %s", Arrays.toString(hints)));
		}
	}

	private void initHints(BooleanQuery bq, String... hints) {
		for(String hint : hints) {
			for(int i = 0; i != HINT_SHARDS; ++i) {
				bq.add(new TermQuery(new Term("hint", hint + "_" + i)), Occur.SHOULD);
			}
		}
		bq.setMinimumNumberShouldMatch(hints.length);
	}
	

	public static class Strat1 {
		
		public static void main(String[] args) throws IOException {
			
			SubQueryTest test = new SubQueryTest();
			test.generate(10000);
			
			test.testQueries();
		}
	}

	public static class Strat2 {
		
		public static void main(String[] args) throws IOException {
			
			System.out.println("100 doc, debug setup");
			
			SubQueryTest test = new SubQueryTest();
			test.testOnce = true;
			test.hintAll = true;
//			test.generate(100);
//			test.generate(13,28,30);
			test.generate(13, 13);
			
			test.testQuery(test.createPat1(0), "Pat1[0]", "H0");
			test.testQuery(test.createPat1(0), "Pat1[0]");
		}
	}

	
	public static class TermSetQuery extends MultiTermQuery {
		
		private static final long serialVersionUID = 20110827L;
		
		private String field;
		private String[] terms;
		
		public TermSetQuery(String field, String[] terms) {
			this.field = field;
			this.terms = terms;
		}

		@Override
		protected FilteredTermEnum getEnum(IndexReader reader)	throws IOException {
			
			return new FilteredTermEnum() {
				
				int pos = 0;
				
				@Override
				public boolean next() throws IOException {
					++pos;
					return pos < terms.length;
				}

				@Override
				public Term term() {
					return new Term(field, terms[pos]);
				}

				@Override
				protected boolean termCompare(Term term) {
					return true;
				}
				
				@Override
				protected boolean endEnum() {
					return pos >= terms.length;
				}
				
				@Override
				public float difference() {
					return 0;
				}
			};
		}

		@Override
		public String toString(String field) {
			return "TermSetQuery(" + field + ", " + Arrays.toString(terms) + ")";
		}
	}
}
