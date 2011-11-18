package org.gridkit.coherence.search.comparation;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.junit.Before;
import org.junit.Test;

public abstract class TestIndexUpdateSpeed {

	private static final int TEST_STEP = 1000;
	private static final int DOC_COUNT = 1000000;
	private TestDocumentGenerator gen;
	
	abstract LuceneSearchEngine getSearchEngine();
	
	@Test
	public void progressiveTest() throws IOException {
		
		TestDocExtractor ex= new TestDocExtractor();
		LuceneSearchEngine engine = getSearchEngine();
		
		for(int n = 0; n != DOC_COUNT / TEST_STEP; ++n) {
			for(int i = 0; i != TEST_STEP; ++i) {
				int id = TEST_STEP * n + i;
				engine.insertDocument(Integer.valueOf(id), new SimpleIndexableDocument(getDocument(id)));
			}
			engine.releaseSearcher(engine.acquireSearcher());

			Sampler getIndexTime = new Sampler();
			getIndexTime.setScale(1d / TimeUnit.MICROSECONDS.toNanos(1));

			Sampler query1Time = new Sampler();
			query1Time.setScale(1d / TimeUnit.MICROSECONDS.toNanos(1));

			Sampler query4Time = new Sampler();
			query4Time.setScale(1d / TimeUnit.MICROSECONDS.toNanos(1));
			
			for(int i = 0; i != TEST_STEP; ++i) {
				int id = TEST_STEP * n + i;
				Query query1 = getQuery1(id);
				Query query4 = getQuery4(id);
				Document document = getDocument(id);

				long start = System.nanoTime();
				engine.updateDocument(Integer.valueOf(id), new SimpleIndexableDocument(document));
				IndexSearcher searcher = engine.acquireSearcher();
				long time = System.nanoTime() - start;
				getIndexTime.add(time);

				TopDocs td;

				start = System.nanoTime();
				td = searcher.search(query1, 4);
				if (td.totalHits < 1) {
					System.err.println("Document is not found #" + id);
					System.exit(0);
				}
				time = System.nanoTime() - start;
				query1Time.add(time);

				start = System.nanoTime();
				td = searcher.search(query1, 4);
				if (td.totalHits < 1) {
					System.err.println("Document is not found #" + id / 2);
					System.exit(0);
				}				
				time = System.nanoTime() - start;
				query1Time.add(time);

				start = System.nanoTime();
				td = searcher.search(query4, 4);
				if (td.totalHits < 1) {
					System.err.println("Document is not found #" + id);
					System.exit(0);
				}
				time = System.nanoTime() - start;
				query4Time.add(time);
				start = System.nanoTime();
				td = searcher.search(query4, 4);
				if (td.totalHits < 1) {
					System.err.println("Document is not found #" + id/2);
					System.exit(0);
				}
				time = System.nanoTime() - start;
				query4Time.add(time);
				
				engine.releaseSearcher(searcher);
				
			}
			
			System.out.println(engine.toString());
			IndexSearcher searcher = engine.acquireSearcher();
			System.out.println(searcher.maxDoc() + " documents");
			engine.releaseSearcher(searcher);
			System.out.println("Docs " + (n * TEST_STEP) + " - update & getIndex: " + getIndexTime.asString());
			System.out.println("Docs " + (n * TEST_STEP) + " - query1:   " + query1Time.asString());
			System.out.println("Docs " + (n * TEST_STEP) + " - query4:   " + query4Time.asString());
		}		
	}

	@Before
	public void initDocGen() {
		gen = new TestDocumentGenerator();
		gen.setDocCount(DOC_COUNT);
		gen.addField("alfa", 0.1);
		gen.addField("beta", 1);
		gen.addField("gamma", 10);
		gen.addField("theta-1", 100);
		gen.addField("theta-2", 100);
		gen.addField("theta-3", 100);
		gen.addField("omega", 10000);
	}
	
	public Document getDocument(int docId) {
		Document doc = new Document();
		Map<String, String> map = gen.getDoc(docId);
		for(Map.Entry<String, String> entry: map.entrySet()) {
			Field f = new Field(entry.getKey(), entry.getValue(), Store.NO, Index.NOT_ANALYZED);
			doc.add(f);
		}
		return doc;
	}
	
	public Query getQuery4(int docId) {
		Map<String, String> map = gen.getDoc(docId);
		Term t1 = new Term("theta-1", map.get("theta-1"));
		Term t2 = new Term("theta-2", map.get("theta-2"));
		Term t3 = new Term("theta-3", map.get("theta-3"));
		BooleanQuery bq = new BooleanQuery();
		bq.add(new TermQuery(t1), Occur.MUST);
		bq.add(new TermQuery(t2), Occur.MUST);
		bq.add(new TermQuery(t3), Occur.MUST);
		return bq;
	}	

	public Query getQuery1(int docId) {
		Map<String, String> map = gen.getDoc(docId);
		Term t1 = new Term("alfa", map.get("alfa"));
		return new TermQuery(t1);
	}	
}
