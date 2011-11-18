package org.gridkit.coherence.search.lucene;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.gridkit.coherence.search.lucene.util.ScoredTop;
import org.gridkit.coherence.search.lucene.util.ScoredTopMerger;
import org.gridkit.coherence.search.lucene.util.TopEntriesContainer;

@SuppressWarnings("rawtypes")
public class LuceneTopDocSearch implements DistributedLuceneIndexProcessor<ScoredTop, ScoredEntries> {

	private Query query;
	private int docLimit;
	
	public LuceneTopDocSearch(Query query, int docLimit) {
		this.query = query;
		this.docLimit = docLimit;
	}
	
	@Override
	public ScoredTop executeOnIndex(final org.gridkit.coherence.search.lucene.DistributedLuceneIndexProcessor.IndexAggregationContext context) throws IOException {
		IndexSearcher searcher = context.getIndexSearcher();
		TopDocs docs = searcher.search(query, docLimit);
		ScoredTop top = new ScoredTop(docs, context);
		return top;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public ScoredEntries executeOnResults(Collection<ScoredTop> nodeResults) {
		
		ScoredTopMerger merger = new ScoredTopMerger(ScoredTopMerger.SCORE_COMPARATOR);
		int totalHits = 0;
		int docCount = 0;
		float maxScore = 0f;
		for (ScoredTop block: nodeResults) {
			totalHits += block.getTotalHits();
			if (maxScore < block.getMaxScore()) {
				maxScore = block.getMaxScore();
			}
			docCount += block.size();
			
			merger.addBlock(block);
		}
		
		int topSize = Math.min(docLimit, docCount);
		TopEntriesContainer top = new TopEntriesContainer(null, topSize);
		Object[] inKey = new Object[1];
		float[] inScore = new float[1];
		for(int i = 0; i != topSize; ++i) {
			if (merger.getNext(inKey, inScore)) {
				top.append(inKey[0], inScore[0]);
			}
		}
		return top;
	}
}
