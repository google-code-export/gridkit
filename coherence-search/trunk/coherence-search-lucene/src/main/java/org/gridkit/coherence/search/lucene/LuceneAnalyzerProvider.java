package org.gridkit.coherence.search.lucene;

import org.apache.lucene.analysis.Analyzer;

/**
 * This interface need to avoid potential problems with particular Lucene
 * analyzer instance not being serializable.
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public interface LuceneAnalyzerProvider {

	public Analyzer getAnalyzer();
	
}
