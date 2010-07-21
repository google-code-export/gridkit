package org.gridkit.coherence.search.lucene;

import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.gridkit.coherence.search.IndexEngineConfig;
import org.gridkit.coherence.search.IndexInvocationContext;
import org.gridkit.coherence.search.IndexUpdateEvent;
import org.gridkit.coherence.search.PlugableSearchIndex;

public class LuceneSearchPlugin implements PlugableSearchIndex<LuceneInMemoryIndex, LuceneAnalyzerProvider, Query> {

	@Override
	public Object createIndexCompatibilityToken(LuceneAnalyzerProvider indexConfig) {
		return new GenericLuceneIndexToken();
	}

	@Override
	public void configure(IndexEngineConfig config) {
		config.setOldValueOnUpdateEnabled(false);
	}

	@Override
	public LuceneInMemoryIndex createIndexInstance(LuceneAnalyzerProvider indexConfig) {
		Analyzer analyzer = indexConfig.getAnalyzer(); 
		return new LuceneInMemoryIndex(analyzer);
	}

	@Override
	public void updateIndexEntries(LuceneInMemoryIndex index, Map<Object, IndexUpdateEvent> events, IndexInvocationContext context) {
		index.update(events, context);
	}

	@Override
	public boolean applyIndex(LuceneInMemoryIndex index, Query query, final Set<Object> keySet, IndexInvocationContext context) {
		index.applyIndex(query, keySet, context);
		return false;
	}

	@Override
	public int calculateEffectiveness(LuceneInMemoryIndex index, Query query, Set<Object> keySet, IndexInvocationContext context) {
		// lucene index does not support evaluate method, so index should be used in all cases
		return 1;
	}

	@Override
	public boolean evaluate(Query query, Object document) {
		return false;
	}
}
