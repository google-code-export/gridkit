package org.gridkit.coherence.search.comparation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.store.instantiated.InstantiatedIndex;
import org.apache.lucene.store.instantiated.InstantiatedIndexWriter;
import org.apache.lucene.util.Version;
import org.gridkit.lucene.memsegment.MemIndexSegment;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.tangosol.util.extractor.MultiExtractor;

public class MemIndexMergeTest {

	
	TestDocumentGenerator docGen = new TestDocumentGenerator();
	Map<Integer, List<Long>> statistics = new TreeMap<Integer, List<Long>>();
	
	@Before
	public void initDocGen() {
		docGen.setDocCount(1 << 16);
		
		docGen.addField("alfa", 0.1);
		docGen.addField("beta", 1);
		docGen.addField("gamma", 10);
		docGen.addField("thetta", 100);
	}
	
	@Test
	public void testMerge() throws IOException {
		
		runMergeTest(1 << 16);
		
		statistics.clear();
		
		for(int i = 1; i != 16; ++i) {			
			int testCount = (16 - i) * 3;
			for(int n = 0; n != testCount; ++n) { 
				runMergeTest(1 << i);
			}
		}
		
		for(Map.Entry<Integer, List<Long>> entry: statistics.entrySet()) {
			System.out.println(String.format("Merge %d - %.3fms", entry.getKey(), avg(entry.getValue(), 1d / TimeUnit.MILLISECONDS.toNanos(1))));
		}
		
	}

	private void runMergeTest(int maxSize) throws IOException {
		System.out.println("Merge test ..." + maxSize);
		
		new MultiExtractor(new M)
		
		int n = 0;
		Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_33);
		MemIndexSegment l = new MemIndexSegment();
		InstantiatedIndexWriter lw = l.indexWriterFactory(analyzer, true);
		lw.addDocument(docGen.getLuceneDoc(n++));
		lw.commit();
		IndexReader lr = l.indexReaderFactory();
		
		while(lr.numDocs() < maxSize) {
		
			int ndocs = lr.numDocs();
			InstantiatedIndex r = new InstantiatedIndex();
			InstantiatedIndexWriter rw = r.indexWriterFactory(analyzer, true);
			for(int i = 0; i != ndocs; ++i) {
				rw.addDocument(docGen.getLuceneDoc(n++));
			}
			rw.commit();
			IndexReader rr = r.indexReaderFactory();
			
			long start = System.nanoTime();
			l = new InstantiatedIndex(new MultiReader(lr, rr){
				@Override
				public boolean isOptimized() {
					return true;
				}});
			lr = l.indexReaderFactory();
			long time = System.nanoTime() - start;
			
			addSample(ndocs, time);
			if (time > TimeUnit.MILLISECONDS.toNanos(10)) {
				System.out.println("Merge " + ndocs + " x " + ndocs + String.format(" : %.3fms", 1d * time / TimeUnit.MILLISECONDS.toNanos(1)));
			}
		}
	}
	
	private void addSample(int size, long time) {
		List<Long> list = statistics.get(size);
		if (list == null) {
			list = new ArrayList<Long>();
			statistics.put(size, list);
		}
		list.add(time);
	}

	private double avg(List<Long> samples, double scale) {
		long total = 0;
		for (Long sample: samples) {
			total += sample;
		}
		
		return scale * total / samples.size();
	}
}
